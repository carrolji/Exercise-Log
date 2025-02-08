package com.example.exerciselog.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ExerciseLogDao {

    @Upsert
    suspend fun upsertExerciseLog(item: ExerciseLogEntity)

    @Upsert
    suspend fun upsertExerciseLogs(item: List<ExerciseLogEntity>)

    @Query("SELECT * FROM ExerciseLogEntity WHERE id = :id")
    suspend fun getExerciseLogById(id: Long): ExerciseLogEntity

    @Query("SELECT * FROM ExerciseLogEntity")
    suspend fun getAllExerciseLogs(): List<ExerciseLogEntity>

    @Query("DELETE FROM ExerciseLogEntity WHERE id = :id")
    suspend fun deleteExerciseLog(id: Long)
}