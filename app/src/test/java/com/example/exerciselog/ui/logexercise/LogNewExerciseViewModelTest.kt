package com.example.exerciselog.ui.logexercise

import com.example.exerciselog.MainCoroutineRule
import com.example.exerciselog.data.ExerciseLogRepositoryImpl
import com.example.exerciselog.data.ExerciseType
import com.example.exerciselog.data.LogType
import com.example.exerciselog.domain.ExerciseLog
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.UUID

class LogNewExerciseViewModelTest {

    private lateinit var viewModel: LogNewExerciseViewModel
    private var mockRepository: ExerciseLogRepositoryImpl = mockk<ExerciseLogRepositoryImpl>()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    val exerciseLog = ExerciseLog(
        exerciseId = UUID.randomUUID().toString(),
        type = ExerciseType.RUNNING,
        duration = 30,
        caloriesBurned = 100,
        startTime = ZonedDateTime.now().minusMinutes(30),
        endTime = ZonedDateTime.now(),
        logType = LogType.MANUAL_INPUT,
        isConflict = false,
    )

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Before
    fun setup() {
        viewModel = LogNewExerciseViewModel(mockRepository)
    }

    @Test
    fun `action update exercise type`() = runTest {
       val exerciseType = ExerciseType.RUNNING

        viewModel.onAction(ExerciseLogDetailUIEvent.UpdateExerciseType(exerciseType.name))

        assertEquals(viewModel.state.type, exerciseType)
        assertNotEquals(viewModel.state.type, null)
    }

    @Test
    fun `action update calories type`() = runTest {
        val calories = 100

        viewModel.onAction(ExerciseLogDetailUIEvent.UpdateExerciseCalories(calories))

        assertEquals(viewModel.state.caloriesBurned, calories)
    }

    @Test
    fun `action update exercise duration`() = runTest {

        viewModel.onAction(ExerciseLogDetailUIEvent.UpdateExerciseDuration(0, 30))

        assertEquals(viewModel.state.duration, 30)
    }

    @Test
    fun `action update exercise start time`() = runTest {
        val hour = 3
        val min = 30
        val today = ZonedDateTime.now()
        val parse = ZonedDateTime.of(
            today.toLocalDate(),
            LocalTime.of(hour, min),
            ZoneId.systemDefault()
        )

        viewModel.onAction(ExerciseLogDetailUIEvent.UpdateExerciseStartTime(hour, min))

        assertEquals(viewModel.state.startTime, parse)
    }

    @Test
    fun `create new exercise log - success`() {
        coEvery { mockRepository.addExerciseLog(exerciseLog) }

        viewModel.onAction(ExerciseLogDetailUIEvent.OnSaveNewExercise)

        coEvery { mockRepository.addExerciseLog(exerciseLog) }
    }
}