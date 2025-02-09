package com.example.exerciselog.ui.exerciseloglist

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.changes.Change
import androidx.health.connect.client.changes.UpsertionChange
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exerciselog.SyncTimeDataStore
import com.example.exerciselog.data.ExerciseType
import com.example.exerciselog.data.HealthConnectAvailability
import com.example.exerciselog.data.HealthConnectManager
import com.example.exerciselog.data.LogType
import com.example.exerciselog.domain.ExerciseLog
import com.example.exerciselog.domain.ExerciseLogRepository
import com.example.exerciselog.utils.formatAsDate
import com.example.exerciselog.utils.toLocalTimeFormat
import com.example.exerciselog.utils.toZoneDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class ExerciseLogViewModel(
    private val exerciseLogRepository: ExerciseLogRepository,
    private val healthConnectManager: HealthConnectManager,
    private val syncPreferences: SyncTimeDataStore,
) : ViewModel() {

    private val _exerciseLogsUiState = MutableStateFlow(ExerciseLogsUIState(isLoading = true))
    val exerciseLogUiState = _exerciseLogsUiState.asStateFlow()

    private val _sideEffectChannel = Channel<SideEffect>(capacity = Channel.BUFFERED)
    val sideEffectFlow: Flow<SideEffect>
        get() = _sideEffectChannel.receiveAsFlow()

    val permissions = setOf(
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
    )

    val permissionsLauncher = healthConnectManager.requestPermissionsActivityContract()

    private var changesToken: MutableState<String?> = mutableStateOf(null)

    init {
        viewModelScope.launch {
            checkHealthConnectAvailability()
        }
    }

    private suspend fun loadAllExerciseLog() {
        val exerciseRecords = exerciseLogRepository.getAllExerciseLogs()
        val exerciseMap = exerciseRecords.groupBy { log ->
            log.startTime.formatAsDate()
        }
        _exerciseLogsUiState.update {
            it.copy(
                isLoading = false,
                exerciseLogsMap = exerciseMap,
            )
        }
    }

    private fun syncExerciseSessionFromHealthConnect() = viewModelScope.launch(Dispatchers.IO) {
        val lastSyncInstant = Instant.ofEpochMilli(syncPreferences.getLastSyncTime())

        val currentTime = Instant.now()
        val exerciseRecords =
            healthConnectManager.readExerciseSessions(lastSyncInstant, currentTime)
        val caloriesRecords =
            healthConnectManager.readCaloriesBurnedRecord(lastSyncInstant, currentTime)
        val exerciseSyncLogs = combinedExerciseAndCaloriesRecords(exerciseRecords, caloriesRecords)

        syncPreferences.saveLastSyncTime(currentTime.toEpochMilli())

        //If we've already sync the latest time, check to see if there's any changes incoming
        //Get past updated records before sync time
        getUpdatedHealthConnectRecords(lastSyncInstant).collectLatest { oldLogs ->
            exerciseSyncLogs.addAll(oldLogs)
        }

        addAndDetectLogsConflict(exerciseSyncLogs)
    }

    private fun addAndDetectLogsConflict(logs: List<ExerciseLog>) {
        viewModelScope.launch {
            exerciseLogRepository.syncExerciseLogs(logs).collectLatest { updatedLogs ->
                val exerciseMap = updatedLogs.groupBy { log ->
                    log.startTime.formatAsDate()
                }
                _exerciseLogsUiState.update {
                    it.copy(
                        isLoading = false,
                        exerciseLogsMap = exerciseMap,
                    )
                }
            }
            _sideEffectChannel.send(SideEffect.ShowToast("Sync Successful"))
        }
    }

    private fun resolveLogsConflict(exerciseId: String) = viewModelScope.launch(Dispatchers.IO) {
        exerciseLogRepository.resolveLogs(exerciseId)
        loadAllExerciseLog()
    }

    fun onAction(event: ExerciseLogsUIEvent) {
        when (event) {
            ExerciseLogsUIEvent.OnCheckPermissions -> {
                viewModelScope.launch {
                    checkHealthConnectAvailability()
                }
            }

            ExerciseLogsUIEvent.OnLoadExerciseLogs -> {
                viewModelScope.launch {
                    loadAllExerciseLog()
                }
            }

            ExerciseLogsUIEvent.OnSyncExerciseSessions -> {
                syncExerciseSessionFromHealthConnect()
            }

            is ExerciseLogsUIEvent.OnDeleteExerciseLog -> {
                viewModelScope.launch {
                    exerciseLogRepository.deleteExerciseLog(event.exerciseId)
                    loadAllExerciseLog()
                }
            }

            is ExerciseLogsUIEvent.OnKeepExerciseLog -> {
                resolveLogsConflict(event.exerciseId)
            }
        }
    }

    private suspend fun checkHealthConnectAvailability() {
        healthConnectManager.checkAvailability().collectLatest { availability ->
            _exerciseLogsUiState.update {
                it.copy(
                    isHealthConnectAvailable = availability
                )
            }
            if (availability == HealthConnectAvailability.INSTALLED) {
                val hasAllPermissions = healthConnectManager.hasAllPermissions(permissions)
                _exerciseLogsUiState.update {
                    it.copy(
                        permissionGranted = hasAllPermissions
                    )
                }
                if (hasAllPermissions && changesToken.value == null) {
                    changesToken.value = healthConnectManager.getChangesToken()
                } else if(!hasAllPermissions) {
                    changesToken.value = null
                }
            }
        }
    }

    private fun getUpdatedHealthConnectRecords(lastSyncTime: Instant): Flow<List<ExerciseLog>> =
        healthConnectManager.getChanges(changesToken.value ?: "").map { message ->
            when (message) {
                is HealthConnectManager.ChangesMessage.ChangeList -> {
                    val newChanges = getChangesInExerciseAndCalories(lastSyncTime, message.changes)
                    newChanges
                }

                is HealthConnectManager.ChangesMessage.NoMoreChanges -> {
                    changesToken.value = message.nextChangesToken
                    emptyList()
                }
            }
        }

    private fun getChangesInExerciseAndCalories(
        lastSyncTime: Instant,
        changes: List<Change>
    ): List<ExerciseLog> {
        val exerciseRecords = mutableListOf<ExerciseSessionRecord>()
        val caloriesRecords = mutableListOf<TotalCaloriesBurnedRecord>()
        changes.forEach { change ->
            if (change is UpsertionChange) {
                when (change.record) {
                    is ExerciseSessionRecord -> {
                        val activity = change.record as ExerciseSessionRecord
                        if (activity.startTime < lastSyncTime) {
                            exerciseRecords.add(activity)
                        }
                    }

                    is TotalCaloriesBurnedRecord -> {
                        val calories = change.record as TotalCaloriesBurnedRecord
                        if (calories.startTime < lastSyncTime) {
                            caloriesRecords.add(calories)
                        }
                    }
                }
            }
        }
        return combinedExerciseAndCaloriesRecords(exerciseRecords, caloriesRecords)
    }

    fun combinedExerciseAndCaloriesRecords(
        exerciseRecords: List<ExerciseSessionRecord>,
        caloriesRecords: List<TotalCaloriesBurnedRecord>
    ): MutableList<ExerciseLog> {
        val caloriesMap = caloriesRecords.associateBy { it.startTime }

        return exerciseRecords.map { session ->
            val sessionStartTime = ZonedDateTime.ofInstant(
                session.startTime,
                session.startZoneOffset ?: ZoneId.systemDefault()
            )
            val sessionEndTime = ZonedDateTime.ofInstant(
                session.endTime,
                session.endZoneOffset ?: ZoneId.systemDefault()
            )
            val duration = Duration.between(sessionStartTime, sessionEndTime).toMinutes()
            val exerciseType = ExerciseType.entries.firstOrNull { it.value == session.exerciseType }
                ?: ExerciseType.OTHER_WORKOUT
            val caloriesBurned =
                caloriesMap[session.startTime]?.energy?.inKilocalories?.toInt() ?: 0
            ExerciseLog(
                exerciseId = session.metadata.id,
                type = exerciseType,
                duration = duration,
                caloriesBurned = caloriesBurned,
                startTime = sessionStartTime,
                endTime = sessionEndTime,
                logType = LogType.SYNC_DATA,
                isConflict = false,
            )
        }.toMutableList()
    }
}