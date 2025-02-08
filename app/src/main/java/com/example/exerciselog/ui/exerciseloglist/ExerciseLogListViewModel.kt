package com.example.exerciselog.ui.exerciseloglist

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exerciselog.data.ExerciseType
import com.example.exerciselog.data.HealthConnectAvailability
import com.example.exerciselog.data.HealthConnectManager
import com.example.exerciselog.domain.ExerciseLog
import com.example.exerciselog.domain.ExerciseLogRepository
import com.example.exerciselog.utils.formatAsDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class ExerciseLogListViewModel(
    private val exerciseLogRepository: ExerciseLogRepository,
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    private val _exerciseLogUiState = MutableStateFlow(ExerciseLogUIState(isLoading = true))
    val exerciseLogUiState = _exerciseLogUiState.asStateFlow()

    private val _sideEffectChannel = Channel<SideEffect>(capacity = Channel.BUFFERED)
    val sideEffectFlow: Flow<SideEffect>
        get() = _sideEffectChannel.receiveAsFlow()

    val permissions = setOf(
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
    )

    var permissionsGranted = mutableStateOf(false)
        private set

    val permissionsLauncher = healthConnectManager.requestPermissionsActivityContract()

    init {
        viewModelScope.launch {
            checkHealthConnectAvailability()
        }
    }

    private fun loadAllExerciseLog() {
        viewModelScope.launch {
            val exerciseRecords = exerciseLogRepository.getAllExerciseLogs()
            val exerciseMap = exerciseRecords.groupBy { log ->
                log.startTime.formatAsDate()
            }
            _exerciseLogUiState.update {
                it.copy(
                    isLoading = false,
                    exerciseLogs = exerciseRecords,
                    exerciseLogsMap = exerciseMap,
                )
            }
        }
    }

    private fun syncExerciseSessionFromHealthConnect() = viewModelScope.launch(Dispatchers.IO) {
        val startOfDay = ZonedDateTime.now().minusDays(7).truncatedTo(ChronoUnit.DAYS)
        val currentTime = Instant.now()
        val exerciseRecords =
            healthConnectManager.readExerciseSessions(startOfDay.toInstant(), currentTime)
        val caloriesRecords =
            healthConnectManager.readCaloriesBurnedRecord(startOfDay.toInstant(), currentTime)

        val caloriesMap = caloriesRecords.associateBy { it.startTime }
        Log.d("YARRRR", "caloriesRecord $caloriesMap")
        val exerciseLogListSync = exerciseRecords.map { session ->
            Log.d("YARRRR", "session $session")
            val startTime = ZonedDateTime.ofInstant(
                session.startTime,
                session.startZoneOffset ?: ZoneId.systemDefault()
            )
            val endTime = ZonedDateTime.ofInstant(
                session.endTime,
                session.endZoneOffset ?: ZoneId.systemDefault()
            )
            val duration = Duration.between(startTime, endTime).toMinutes()
            val exerciseType = ExerciseType.entries.firstOrNull { it.value == session.exerciseType }
                ?: ExerciseType.OTHER_WORKOUT
            val caloriesBurned =
                caloriesMap[session.startTime]?.energy?.inKilocalories?.toInt() ?: 0
            ExerciseLog(
                exerciseId = session.metadata.id,
                type = exerciseType,
                duration = duration,
                caloriesBurned = caloriesBurned,
                startTime = startTime,
                endTime = endTime,
            )
        }
        Log.d("YARRRR", "new exercise log list $exerciseLogListSync")

        addLog(exerciseLogListSync)
    }

    private fun addLog(logs: List<ExerciseLog>) {
        viewModelScope.launch {
            exerciseLogRepository.addExerciseLogs(logs)
            loadAllExerciseLog()
            _sideEffectChannel.send(SideEffect.ShowToast("Sync Successful"))
        }
    }

    fun onAction(event: ExerciseLogsUIEvent) {
        when (event) {
            ExerciseLogsUIEvent.OnCheckPermissions -> {
                viewModelScope.launch {
                    checkHealthConnectAvailability()
                }
            }

            ExerciseLogsUIEvent.OnLoadExerciseLogs -> loadAllExerciseLog()
            ExerciseLogsUIEvent.OnSyncExerciseSessions -> {
                syncExerciseSessionFromHealthConnect()
            }
        }
    }

    private suspend fun checkHealthConnectAvailability() {
        healthConnectManager.checkAvailability().collectLatest { availability ->
            Log.d("exerciseLog", "check if health connect app is available: $availability")
            _exerciseLogUiState.update {
                it.copy(
                    isHealthConnectAvailable = availability
                )
            }
            if (availability == HealthConnectAvailability.INSTALLED) {
                permissionsGranted.value = healthConnectManager.hasAllPermissions(permissions)
            }
        }
    }
}