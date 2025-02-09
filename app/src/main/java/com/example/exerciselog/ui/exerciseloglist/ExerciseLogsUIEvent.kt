package com.example.exerciselog.ui.exerciseloglist

sealed interface ExerciseLogsUIEvent {
    data object OnCheckPermissions: ExerciseLogsUIEvent
    data object OnLoadExerciseLogs: ExerciseLogsUIEvent
    data object OnSyncExerciseSessions: ExerciseLogsUIEvent
    data class OnDeleteExerciseLog(val exerciseId: String): ExerciseLogsUIEvent
    data class OnKeepExerciseLog(val exerciseId: String): ExerciseLogsUIEvent
}

sealed interface SideEffect {
    data class ShowToast(val message: String) : SideEffect
}