package com.example.exerciselog

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class ExerciseLogApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@ExerciseLogApp)
            modules(appModule)
        }
    }
}