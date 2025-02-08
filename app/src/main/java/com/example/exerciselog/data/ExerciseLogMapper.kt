package com.example.exerciselog.data

import com.example.exerciselog.domain.ExerciseLog
import java.time.Instant
import java.time.ZoneId

fun ExerciseLogEntity.toModel() : ExerciseLog = ExerciseLog(
    exerciseId = exerciseId,
    type = ExerciseType.valueOf(type),
    duration = duration,
    caloriesBurned = caloriesBurned,
    startTime = Instant.parse(startTime).atZone(ZoneId.of("UTC")),
    endTime = Instant.parse(endTime).atZone(ZoneId.of("UTC"))
)

fun ExerciseLog.toExerciseLogEntity() : ExerciseLogEntity = ExerciseLogEntity(
    exerciseId = exerciseId,
    type = type.name,
    duration = duration,
    caloriesBurned = caloriesBurned,
    startTime = startTime.toInstant().toString(),
    endTime = endTime.toInstant().toString()
)