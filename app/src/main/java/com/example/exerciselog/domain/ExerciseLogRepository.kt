package com.example.exerciselog.domain

import java.time.ZonedDateTime

interface ExerciseLogRepository {
    suspend fun getAllExerciseLogs(): List<ExerciseLog>
    suspend fun getExerciseLogsByDate(dateTimeUtc: ZonedDateTime): List<ExerciseLog>
    suspend fun getExerciseLog(logId: Long): ExerciseLog
    suspend fun addExerciseLog(log: ExerciseLog)
    suspend fun addExerciseLogs(logs: List<ExerciseLog>)
    suspend fun deleteExerciseLog(exerciseId: String)
}