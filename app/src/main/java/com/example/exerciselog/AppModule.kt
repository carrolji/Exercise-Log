package com.example.exerciselog

import androidx.room.Room
import com.example.exerciselog.data.ExerciseLogDao
import com.example.exerciselog.data.ExerciseLogDatabase
import com.example.exerciselog.data.ExerciseLogRepositoryImpl
import com.example.exerciselog.data.HealthConnectManager
import com.example.exerciselog.domain.ExerciseLogRepository
import com.example.exerciselog.ui.exerciseloglist.ExerciseLogViewModel
import com.example.exerciselog.ui.logexercise.LogNewExerciseViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

private const val ROOM_DB: String = "exercise_log_db"

val appModule = module {

    single {
        Room.databaseBuilder(
            androidApplication(),
            ExerciseLogDatabase::class.java,
            ROOM_DB
        ).build()
    }

    single<ExerciseLogDao> {
        val database = get<ExerciseLogDatabase>()
        database.exerciseLogDao
    }

    single<ExerciseLogRepository> {
        ExerciseLogRepositoryImpl(get())
    }

    single<HealthConnectManager> {
        HealthConnectManager(get())
    }

    single<SyncTimeDataStore> {
        SyncTimeDataStore(androidApplication())
    }

    viewModel {
        ExerciseLogViewModel(get(), get(), get())
    }

    viewModel {
        LogNewExerciseViewModel(get())
    }
}