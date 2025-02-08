package com.example.exerciselog.ui.logexercise

import java.time.ZonedDateTime

sealed interface ExerciseLogDetailUIEvent {
    data class UpdateExerciseType(val newType: String): ExerciseLogDetailUIEvent
    data class UpdateExerciseDuration(val hour: Int, val min: Int): ExerciseLogDetailUIEvent
    data class UpdateExerciseCalories(val newCalories: Int): ExerciseLogDetailUIEvent
    data class UpdateExerciseDate(val date: ZonedDateTime): ExerciseLogDetailUIEvent
    data class UpdateExerciseStartTime(val hour: Int, val min: Int): ExerciseLogDetailUIEvent
    data object OnSaveNewExercise: ExerciseLogDetailUIEvent
}