package com.example.exerciselog.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    indices = [Index(value = ["exerciseId"], unique = true)],
)
data class ExerciseLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "exerciseId")
    val exerciseId: String = UUID.randomUUID().toString(),
    val type: String,
    val duration: Long, // in minutes
    val caloriesBurned: Int,
    val startTime: String,
    val endTime: String,
)