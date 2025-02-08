package com.example.exerciselog.ui.exerciseloglist

sealed interface ExerciseLogsUIEvent {
    data object OnCheckPermissions: ExerciseLogsUIEvent
    data object OnLoadExerciseLogs: ExerciseLogsUIEvent
    data object OnSyncExerciseSessions: ExerciseLogsUIEvent
}

sealed interface SideEffect {
    data class ShowToast(val message: String) : SideEffect
}