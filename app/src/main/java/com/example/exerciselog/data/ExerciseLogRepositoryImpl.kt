package com.example.exerciselog.data

import com.example.exerciselog.domain.ExerciseLog
import com.example.exerciselog.domain.ExerciseLogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ExerciseLogRepositoryImpl(
    private val dao: ExerciseLogDao
) : ExerciseLogRepository {
    override suspend fun getAllExerciseLogs(): List<ExerciseLog> {
        return dao.getAllExerciseLogs().map { it.toModel() }.sortedBy { it.startTime }
    }

    override suspend fun addExerciseLog(log: ExerciseLog) {
        dao.upsertExerciseLog(log.toExerciseLogEntity())
    }

    override suspend fun deleteExerciseLog(exerciseId: String) {
        dao.deleteExerciseLog(exerciseId)
    }

    override suspend fun syncExerciseLogs(logs: List<ExerciseLog>): Flow<List<ExerciseLog>> = flow {
        dao.upsertExerciseLogs(logs.map { it.toExerciseLogEntity() })
        val conflict = dao.findConflictLogs()
        val records = conflict.map {
            it.copy(
                isConflict = true
            )
        }
        dao.upsertExerciseLogs(records)
        emit(dao.getAllExerciseLogs().map { it.toModel() }.sortedBy { it.startTime })
    }

    override suspend fun resolveLogs(exerciseId: String) {
        dao.updateRecordConflict(false, exerciseId)
    }
}