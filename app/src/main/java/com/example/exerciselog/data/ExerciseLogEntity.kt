package com.example.exerciselog.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ExerciseLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String,
    val duration: Long, // in minutes
    val caloriesBurned: Int,
    val startTime: String,
    val endTime: String,
)