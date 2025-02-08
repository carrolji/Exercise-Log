package com.example.exerciselog.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ExerciseLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExerciseLog(item: ExerciseLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExerciseLogs(item: List<ExerciseLogEntity>)

    @Query("SELECT * FROM ExerciseLogEntity WHERE id = :id")
    suspend fun getExerciseLogById(id: Long): ExerciseLogEntity

    @Query("SELECT * FROM ExerciseLogEntity")
    suspend fun getAllExerciseLogs(): List<ExerciseLogEntity>

    @Query("DELETE FROM ExerciseLogEntity WHERE id = :id")
    suspend fun deleteExerciseLog(id: Long)
}