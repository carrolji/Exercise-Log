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

    @Query("SELECT * FROM ExerciseLogEntity WHERE exerciseId = :exerciseId")
    suspend fun getExerciseLogById(exerciseId: String): ExerciseLogEntity

    @Query("SELECT * FROM ExerciseLogEntity")
    suspend fun getAllExerciseLogs(): List<ExerciseLogEntity>

    @Query("DELETE FROM ExerciseLogEntity WHERE exerciseId = :exerciseId")
    suspend fun deleteExerciseLog(exerciseId: String)

    @Query(
        "SELECT * \n" +
            "FROM ExerciseLogEntity a\n" +
            "JOIN (SELECT type, startTime, COUNT(*)\n" +
            "FROM ExerciseLogEntity \n" +
            "GROUP BY type, startTime\n" +
            "HAVING count(*) > 1 ) b\n" +
            "ON a.type = b.type\n" +
            "AND a.startTime = b.startTime\n" +
            "ORDER BY startTime"
    )
    suspend fun findConflictLogs(): List<ExerciseLogEntity>

    @Query("UPDATE ExerciseLogEntity SET isConflict = :isConflict WHERE exerciseId = :exerciseId")
    fun updateRecordConflict(isConflict: Boolean, exerciseId: String)
}