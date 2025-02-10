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

    @Query("SELECT * FROM ExerciseLogEntity")
    suspend fun getAllExerciseLogs(): List<ExerciseLogEntity>

    @Query("DELETE FROM ExerciseLogEntity WHERE exerciseId = :exerciseId")
    suspend fun deleteExerciseLog(exerciseId: String)

    @Query(
        "SELECT *\n" +
        "FROM ExerciseLogEntity a, ExerciseLogEntity b\n" +
        "WHERE a.startTime < b.endTime AND a.endTime > b.startTime AND a.type = b.type AND a.exerciseId <> b.exerciseId"
    )
    suspend fun findConflictLogs(): List<ExerciseLogEntity>

    @Query("UPDATE ExerciseLogEntity SET isConflict = :isConflict WHERE exerciseId = :exerciseId")
    fun updateRecordConflict(isConflict: Boolean, exerciseId: String)
}