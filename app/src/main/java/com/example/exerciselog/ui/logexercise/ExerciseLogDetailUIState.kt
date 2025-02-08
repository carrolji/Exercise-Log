package com.example.exerciselog.ui.logexercise

import com.example.exerciselog.data.ExerciseType
import java.time.ZonedDateTime

data class ExerciseLogDetailUIState(
    val type: ExerciseType = ExerciseType.OTHER_WORKOUT,
    val caloriesBurned: Int = 0,
    val startTime: ZonedDateTime = ZonedDateTime.now(),
    val endTime: ZonedDateTime = ZonedDateTime.now().plusMinutes(30),
    val duration: Long = 0L, // in minutes
)