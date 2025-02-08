package com.example.exerciselog.nav

sealed class Screen(val route: String) {
    data object ExerciseLogScreen: Screen("exercise_list_screen")
    data object LogNewExerciseScreen: Screen("log_new_exercise_screen")
}