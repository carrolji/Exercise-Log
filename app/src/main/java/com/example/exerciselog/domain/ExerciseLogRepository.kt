package com.example.exerciselog.domain

import kotlinx.coroutines.flow.Flow

interface ExerciseLogRepository {
    suspend fun getAllExerciseLogs(): List<ExerciseLog>
    suspend fun addExerciseLog(log: ExerciseLog)
    suspend fun deleteExerciseLog(exerciseId: String)
    suspend fun syncExerciseLogs(logs: List<ExerciseLog>): Flow<List<ExerciseLog>>
    suspend fun resolveLogs(exerciseId: String)
}