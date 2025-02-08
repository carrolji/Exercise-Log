package com.example.exerciselog.ui.exerciseloglist

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.health.connect.client.HealthConnectFeatures
import androidx.health.connect.client.feature.ExperimentalFeatureAvailabilityApi
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exerciselog.data.ExerciseType
import com.example.exerciselog.data.HealthConnectManager
import com.example.exerciselog.domain.ExerciseLog
import com.example.exerciselog.domain.ExerciseLogRepository
import com.example.exerciselog.ui.ExerciseLogUIEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class ExerciseLogListViewModel(
    private val exerciseLogRepository: ExerciseLogRepository,
    private val healthConnectManager: HealthConnectManager
) : ViewModel() {

    private val _exerciseLogUiState = MutableStateFlow(ExerciseLogUIState(isLoading = true))
    val exerciseLogUiState = _exerciseLogUiState.asStateFlow()

    val permissions = setOf(
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
    )

    var permissionsGranted = mutableStateOf(false)
        private set

    val permissionsLauncher = healthConnectManager.requestPermissionsActivityContract()

    //    val backgroundReadPermissions = setOf(PERMISSION_READ_HEALTH_DATA_IN_BACKGROUND)

//    var backgroundReadAvailable = mutableStateOf(false)
//        private set

//    var backgroundReadGranted = mutableStateOf(false)
//        private set


    init {
        _exerciseLogUiState.update {
            it.copy(
                isHealthConnectAvailable = healthConnectManager.availability.value
            )
        }
    }

    private fun loadAllExerciseLog() {
        viewModelScope.launch {
            _exerciseLogUiState.update {
                it.copy(
                    isLoading = false,
                    exerciseLogs = exerciseLogRepository.getAllExerciseLogs(),
                )
            }
        }
    }

    private fun syncExerciseSessionFromHealthConnect() = viewModelScope.launch {
        val startOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val now = Instant.now()
        val healthConnectSessions =
            healthConnectManager.readExerciseSessions(startOfDay.toInstant(), now)
        val exerciseLogListSync = healthConnectSessions.map { session ->
            val startTime = ZonedDateTime.ofInstant(session.startTime, session.startZoneOffset)
            val endTime = ZonedDateTime.ofInstant(session.endTime, session.endZoneOffset)
            val duration = Duration.between(startTime, endTime)
            val type = session.exerciseType
            ExerciseLog(
                type = ExerciseType.entries.firstOrNull { it.value == type }
                    ?: ExerciseType.OTHER_WORKOUT,
                duration = duration.toMinutes(),
                caloriesBurned = 0,
                startTime = startTime,
                endTime = endTime,
            )
        }
        Log.d("YARRRR", "new exercise log list $exerciseLogListSync")
        exerciseLogRepository.addExerciseLogs(exerciseLogListSync)
        _exerciseLogUiState.update {
            it.copy(
                exerciseLogs = exerciseLogRepository.getAllExerciseLogs(),
            )
        }
    }

    fun onAction(event: ExerciseLogUIEvent) {
        when (event) {
            ExerciseLogUIEvent.OnCheckPermissions -> {
                viewModelScope.launch {
                    checkPermissions()
                }
            }

            ExerciseLogUIEvent.OnLoadExerciseLogs -> loadAllExerciseLog()
            ExerciseLogUIEvent.OnSyncExerciseSessions -> {
                syncExerciseSessionFromHealthConnect()
            }
        }
    }

    private suspend fun checkPermissions() {
        permissionsGranted.value = healthConnectManager.hasAllPermissions(permissions)
    }

//    fun initialLoad() {
//        viewModelScope.launch {
//            tryWithPermissionsCheck {
//                //readExerciseSessions()
//            }
//        }
//    }
//
//    @OptIn(ExperimentalFeatureAvailabilityApi::class)
//    private suspend fun tryWithPermissionsCheck(block: suspend () -> Unit) {
//        permissionsGranted.value = healthConnectManager.hasAllPermissions(permissions)
////        backgroundReadAvailable.value = healthConnectManager.isFeatureAvailable(
////            HealthConnectFeatures.FEATURE_READ_HEALTH_DATA_IN_BACKGROUND
////        )
////        backgroundReadGranted.value = healthConnectManager.hasAllPermissions(backgroundReadPermissions)
//        if (permissionsGranted.value) {
//            block()
//        }
//    }
}