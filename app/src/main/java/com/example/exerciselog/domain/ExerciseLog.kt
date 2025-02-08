package com.example.exerciselog.domain

import com.example.exerciselog.data.ExerciseType
import java.time.ZonedDateTime

data class ExerciseLog(
    val exerciseId: Long? = null,
    val type: ExerciseType,
    val duration: Long, // in minutes
    val caloriesBurned: Int,
    val startTime: ZonedDateTime, //utc
    val endTime: ZonedDateTime, //utc
)