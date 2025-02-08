package com.example.exerciselog.ui.exerciseloglist

import com.example.exerciselog.data.HealthConnectAvailability
import com.example.exerciselog.domain.ExerciseLog

data class ExerciseLogUIState(
    val isLoading: Boolean = false,
    val exerciseLogs: List<ExerciseLog> = emptyList(),
    val exerciseLogsMap: Map<String, List<ExerciseLog>> = emptyMap(),
    val isHealthConnectAvailable: HealthConnectAvailability = HealthConnectAvailability.NOT_INSTALLED,
)