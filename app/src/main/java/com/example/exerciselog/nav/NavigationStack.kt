package com.example.exerciselog.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.exerciselog.ui.exerciseloglist.ExerciseLogListScreenCore
import com.example.exerciselog.ui.logexercise.LogNewExerciseScreenCore

@Composable
fun NavigationStack() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.ExerciseLogScreen.route) {
        composable(route = Screen.ExerciseLogScreen.route) {
            ExerciseLogListScreenCore {
                navController.navigate(route = Screen.LogNewExerciseScreen.route)
            }
        }
        composable(route = Screen.LogNewExerciseScreen.route) {
            LogNewExerciseScreenCore {
                navController.popBackStack()
            }
        }
    }
}