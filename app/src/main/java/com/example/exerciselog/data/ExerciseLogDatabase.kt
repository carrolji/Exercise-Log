package com.example.exerciselog.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ExerciseLogEntity::class],
    version = 1
)
abstract class ExerciseLogDatabase : RoomDatabase() {
    abstract val exerciseLogDao: ExerciseLogDao
}