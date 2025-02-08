package com.example.exerciselog.ui.exerciseloglist

import com.example.exerciselog.data.HealthConnectAvailability
import com.example.exerciselog.domain.ExerciseLog

data class ExerciseLogUIState(
    val isLoading: Boolean = false,
    val exerciseLogs: List<ExerciseLog> = emptyList(),
    val isHealthConnectAvailable: HealthConnectAvailability = HealthConnectAvailability.NOT_INSTALLED
)

//data class HealthConnectUIState(
//    val isLoading: Boolean = false,
//    val isGranted: Boolean = false,
//    val isError: Boolean = false,
//    val permissionsGranted: Boolean  = false,
//    val backgroundReadAvailable: Boolean = false,
//    val backgroundReadGranted: Boolean = false,
//    val isHealthConnectAvailable: HealthConnectAvailability = HealthConnectAvailability.NOT_INSTALLED
//)