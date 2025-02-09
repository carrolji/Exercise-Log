package com.example.exerciselog.ui.exerciseloglist

import androidx.health.connect.client.records.ExerciseSessionRecord
import com.example.exerciselog.MainCoroutineRule
import com.example.exerciselog.SyncTimeDataStore
import com.example.exerciselog.data.ExerciseType
import com.example.exerciselog.data.HealthConnectManager
import com.example.exerciselog.data.LogType
import com.example.exerciselog.domain.ExerciseLog
import com.example.exerciselog.domain.ExerciseLogRepository
import com.example.exerciselog.utils.formatAsDate
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.Duration
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID
import kotlin.random.Random

class ExerciseLogViewModelTest {
    private lateinit var viewModel: ExerciseLogViewModel
    private var repository: ExerciseLogRepository = mockk<ExerciseLogRepository>()
    private lateinit var mockHealthConnectManager: HealthConnectManager
    private var syncPreferences: SyncTimeDataStore = mockk<SyncTimeDataStore>()

    val exerciseLogs = listOf(
        ExerciseLog(
            exerciseId = UUID.randomUUID().toString(),
            type = ExerciseType.RUNNING,
            duration = 30,
            caloriesBurned = 100,
            startTime = ZonedDateTime.now().minusMinutes(30),
            endTime = ZonedDateTime.now(),
            logType = LogType.MANUAL_INPUT,
            isConflict = true,
        ),
        ExerciseLog(
            exerciseId = UUID.randomUUID().toString(),
            type = ExerciseType.BOXING,
            duration = 30,
            caloriesBurned = 100,
            startTime = ZonedDateTime.now().minusMinutes(30),
            endTime = ZonedDateTime.now(),
            logType = LogType.SYNC_DATA,
            isConflict = false,
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setUp() {
        mockHealthConnectManager = mockk(relaxed = true)
        viewModel = ExerciseLogViewModel(repository, mockHealthConnectManager, syncPreferences)
    }

    @Test
    fun `get all exercise logs - returns empty log`() {
        coEvery { repository.getAllExerciseLogs() } returns listOf()
        viewModel.onAction(ExerciseLogsUIEvent.OnCheckPermissions)
        viewModel.onAction(ExerciseLogsUIEvent.OnLoadExerciseLogs)

        val resultMap = viewModel.exerciseLogUiState.value.exerciseLogsMap

        coVerify { repository.getAllExerciseLogs() }
        assertEquals(resultMap, emptyMap<String, List<ExerciseLog>>())
    }

    @Test
    fun `get all exercise logs`() {
        coEvery { repository.getAllExerciseLogs() } returns exerciseLogs

        viewModel.onAction(ExerciseLogsUIEvent.OnLoadExerciseLogs)

        val resultMap = viewModel.exerciseLogUiState.value.exerciseLogsMap
        val expectedMap = exerciseLogs.groupBy { log ->
            log.startTime.formatAsDate()
        }
        coVerify { repository.getAllExerciseLogs() }
        assertEquals(resultMap, expectedMap)
    }

    @Test
    fun `on delete exercise log - successful`() {
        //BEFORE
        val deleteExerciseLog = exerciseLogs[0]
        coEvery { repository.getAllExerciseLogs() } returns exerciseLogs
        coEvery { repository.deleteExerciseLog(deleteExerciseLog.exerciseId) }
        coEvery { repository.getAllExerciseLogs() } returns listOf(exerciseLogs[1])

        //ACTION
        viewModel.onAction(ExerciseLogsUIEvent.OnLoadExerciseLogs)
        viewModel.onAction(ExerciseLogsUIEvent.OnDeleteExerciseLog(exerciseId = deleteExerciseLog.exerciseId))

        //AFTER
        val resultMap = viewModel.exerciseLogUiState.value.exerciseLogsMap
        val expectedMap = listOf(exerciseLogs[1]).groupBy { log ->
            log.startTime.formatAsDate()
        }

        coVerify { repository.deleteExerciseLog(deleteExerciseLog.exerciseId) }
        coVerify { repository.getAllExerciseLogs() }
        assertEquals(resultMap, expectedMap)
    }

    @Test
    fun `on resolving exercise log`() {
        val resolvingLog = exerciseLogs[0]
        val afterLogs = listOf(
            ExerciseLog(
                exerciseId = UUID.randomUUID().toString(),
                type = ExerciseType.RUNNING,
                duration = 30,
                caloriesBurned = 100,
                startTime = ZonedDateTime.now().minusMinutes(30),
                endTime = ZonedDateTime.now(),
                logType = LogType.MANUAL_INPUT,
                isConflict = false,
            ),
            ExerciseLog(
                exerciseId = UUID.randomUUID().toString(),
                type = ExerciseType.BOXING,
                duration = 30,
                caloriesBurned = 100,
                startTime = ZonedDateTime.now().minusMinutes(30),
                endTime = ZonedDateTime.now(),
                logType = LogType.SYNC_DATA,
                isConflict = false,
            )
        )
        coEvery { repository.getAllExerciseLogs() } returns exerciseLogs
        coEvery { repository.resolveLogs(resolvingLog.exerciseId) }
        coEvery { repository.getAllExerciseLogs() } returns afterLogs

        viewModel.onAction(ExerciseLogsUIEvent.OnLoadExerciseLogs)
        viewModel.onAction(ExerciseLogsUIEvent.OnKeepExerciseLog(resolvingLog.exerciseId))

        val resultMap = viewModel.exerciseLogUiState.value.exerciseLogsMap
        val expectedMap = afterLogs.groupBy { log ->
            log.startTime.formatAsDate()
        }
        val resultLog = resultMap[resolvingLog.startTime.formatAsDate()]?.first()?.isConflict
        coVerify { repository.getAllExerciseLogs() }
        coVerify { repository.resolveLogs(resolvingLog.exerciseId) }
        assertEquals(resultMap, expectedMap)
        assertEquals(resultLog, false)
    }

    @Test
    fun `sync data from health connect - return exercise session`() {
        val startOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
        val latestStartOfSession = ZonedDateTime.now().minusMinutes(30)
        val offset = Random.nextDouble()
        val startOfSession = startOfDay.plusSeconds(
            (Duration.between(startOfDay, latestStartOfSession).seconds * offset).toLong()
        )
        val endOfSession = startOfSession.plusMinutes(30)
        val exerciseSession = ExerciseSessionRecord(
            startTime = startOfSession.toInstant(),
            startZoneOffset = startOfSession.offset,
            endTime = endOfSession.toInstant(),
            endZoneOffset = endOfSession.offset,
            exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_HIKING,
            title = "My Run #${Random.nextInt(0, 60)}"
        )
        val syncLogs = viewModel.combinedExerciseAndCaloriesRecords(listOf(exerciseSession), emptyList())

        coEvery { repository.getAllExerciseLogs() } returns exerciseLogs
        coEvery { mockHealthConnectManager.readExerciseSessions(Instant.now(),Instant.now()) } returns listOf(exerciseSession)
        coEvery { repository.syncExerciseLogs(syncLogs) } returns flow { syncLogs.addAll(exerciseLogs) }
        syncLogs.addAll(exerciseLogs)
        coEvery { repository.getAllExerciseLogs() } returns syncLogs

        viewModel.onAction(ExerciseLogsUIEvent.OnLoadExerciseLogs)
        viewModel.onAction(ExerciseLogsUIEvent.OnSyncExerciseSessions)

        val resultMap = viewModel.exerciseLogUiState.value.exerciseLogsMap
        val expectedMap = syncLogs.groupBy { log ->
            log.startTime.formatAsDate()
        }
        coVerify { repository.getAllExerciseLogs() }
        assertEquals(resultMap, expectedMap)
    }

    @Test
    fun `sync data from health connect - return empty exercise session`() {
        coEvery { repository.getAllExerciseLogs() } returns exerciseLogs
        coEvery { mockHealthConnectManager.readExerciseSessions(Instant.now(),Instant.now()) } returns listOf()
        coEvery { repository.syncExerciseLogs(emptyList()) } returns flow { exerciseLogs }

        viewModel.onAction(ExerciseLogsUIEvent.OnLoadExerciseLogs)
        viewModel.onAction(ExerciseLogsUIEvent.OnSyncExerciseSessions)

        val resultMap = viewModel.exerciseLogUiState.value.exerciseLogsMap
        val expectedMap = exerciseLogs.groupBy { log ->
            log.startTime.formatAsDate()
        }

        coVerify { repository.getAllExerciseLogs() }
        assertEquals(resultMap, expectedMap)
    }

}