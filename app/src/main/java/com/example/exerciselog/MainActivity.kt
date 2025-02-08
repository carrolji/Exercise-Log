package com.example.exerciselog

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.exerciselog.data.HealthConnectManager
import com.example.exerciselog.nav.NavigationStack
import com.example.exerciselog.ui.theme.ExerciseLogTheme

class MainActivity : ComponentActivity() {

//    private val viewModel: ExerciseLogListViewModel by viewModel()

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val healthConnectManager = HealthConnectManager(applicationContext)
        val availability = healthConnectManager.availability
        Log.d("YARRRR", "is health connect available? $availability")

        enableEdgeToEdge()
        setContent {
            ExerciseLogTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    NavigationStack(healthConnectManager)
                }
            }
        }
    }
}