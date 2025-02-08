package com.example.exerciselog.data

import com.example.exerciselog.domain.ExerciseLog
import com.example.exerciselog.domain.ExerciseLogRepository
import java.time.ZonedDateTime

class ExerciseLogRepositoryImpl(
    private val dao: ExerciseLogDao
) : ExerciseLogRepository {
    override suspend fun getAllExerciseLogs(): List<ExerciseLog> {
        return dao.getAllExerciseLogs().map { it.toModel() }.sortedBy { it.startTime }
    }

    override suspend fun getExerciseLogsByDate(dateTimeUtc: ZonedDateTime): List<ExerciseLog> {
        return dao.getAllExerciseLogs().map { it.toModel() }
            .filter {
                it.startTime.dayOfMonth == dateTimeUtc.dayOfMonth
                    && it.startTime.month == dateTimeUtc.month
                    && it.startTime.year == dateTimeUtc.year
            }
    }

    override suspend fun getExerciseLog(logId: Long): ExerciseLog {
        return dao.getExerciseLogById(logId).toModel()
    }

    override suspend fun addExerciseLog(log: ExerciseLog) {
        dao.upsertExerciseLog(log.toExerciseLogEntity())
    }

    override suspend fun addExerciseLogs(logs: List<ExerciseLog>) {
        dao.upsertExerciseLogs(logs.map { it.toExerciseLogEntity() })
    }
}