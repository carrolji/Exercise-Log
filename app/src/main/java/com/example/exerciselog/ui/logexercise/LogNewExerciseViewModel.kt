package com.example.exerciselog.ui.logexercise

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.exerciselog.data.ExerciseType
import com.example.exerciselog.domain.ExerciseLog
import com.example.exerciselog.domain.ExerciseLogRepository
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

class LogNewExerciseViewModel(
    private val exerciseLogRepository: ExerciseLogRepository
) : ViewModel() {

    var state by mutableStateOf(ExerciseLogDetailUIState())
        private set

    val map = mutableMapOf<String, List<String>>()

    private fun addExerciseLog() = viewModelScope.launch {
        val durationInHour = state.duration / 60L
        val durationInMin = state.duration % 60L
        val endTime = state.startTime.plusHours(durationInHour).plusMinutes(durationInMin)
        val newExercise = ExerciseLog(
            exerciseId = UUID.randomUUID().toString(),
            type = state.type,
            duration = state.duration,
            caloriesBurned = state.caloriesBurned,
            startTime = state.startTime,
            endTime = endTime,
        )
        exerciseLogRepository.addExerciseLog(newExercise)
    }

    fun onAction(event: ExerciseLogDetailUIEvent) {
        when (event) {
            is ExerciseLogDetailUIEvent.UpdateExerciseType -> {
                state = state.copy(
                    type = ExerciseType.valueOf(event.newType)
                )
            }

            is ExerciseLogDetailUIEvent.UpdateExerciseCalories -> {
                state = state.copy(
                    caloriesBurned = event.newCalories
                )
            }

            is ExerciseLogDetailUIEvent.UpdateExerciseDuration -> {
                calculateDurationAndEndTime(event.hour, event.min)
            }

            is ExerciseLogDetailUIEvent.UpdateExerciseDate -> {
                val parse = ZonedDateTime.of(
                    event.date.toLocalDate(),
                    state.startTime.toLocalTime(),
                    ZoneId.systemDefault()
                )
                state = state.copy(
                    startTime = parse
                )
            }

            is ExerciseLogDetailUIEvent.UpdateExerciseStartTime -> {

                val parse = ZonedDateTime.of(
                    state.startTime.toLocalDate(),
                    LocalTime.of(event.hour, event.min),
                    ZoneId.systemDefault()
                )
                Log.d("YARRR", "date ${state.startTime.toLocalDate()} - ${LocalTime.of(event.hour, event.min)}, $parse")
                state = state.copy(
                    startTime = parse
                )
            }

            ExerciseLogDetailUIEvent.OnSaveNewExercise -> addExerciseLog()

        }
    }

    private fun calculateDurationAndEndTime(hour: Int, minute: Int) {
        val totalDuration = (hour * 60) + minute
        state = state.copy(
            duration = totalDuration.toLong(),
        )
    }
}