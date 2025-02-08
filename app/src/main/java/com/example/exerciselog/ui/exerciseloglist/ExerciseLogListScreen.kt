package com.example.exerciselog.ui.exerciseloglist

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.example.exerciselog.R
import com.example.exerciselog.ui.theme.PurpleGrey80
import org.koin.androidx.compose.koinViewModel

@Composable
fun ExerciseLogListScreenCore(
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    viewModel: ExerciseLogListViewModel = koinViewModel(),
    onLogNewExercise: () -> Unit,
) {
    val uiState by viewModel.exerciseLogUiState.collectAsState()
    val context = LocalContext.current
    LaunchedEffect(true) {
        viewModel.onAction(ExerciseLogsUIEvent.OnLoadExerciseLogs)
    }

    LaunchedEffect(viewModel.sideEffectFlow) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.sideEffectFlow.collect { sideEffect ->
                when (sideEffect) {
                    is SideEffect.ShowToast -> Toast.makeText(context, sideEffect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onAction(ExerciseLogsUIEvent.OnCheckPermissions)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val permissionsGranted by viewModel.permissionsGranted
    val permissions = viewModel.permissions

    val permissionsLauncher =
        rememberLauncherForActivityResult(viewModel.permissionsLauncher) {
            viewModel.onAction(ExerciseLogsUIEvent.OnCheckPermissions)
        }

    ExerciseLogListScreen(
        permissionsGranted = permissionsGranted,
        state = uiState,
        onSyncData = {
            viewModel.onAction(ExerciseLogsUIEvent.OnSyncExerciseSessions)
        },
        onLogNewExercise = onLogNewExercise,
        onLaunchPermissions = {
            permissionsLauncher.launch(permissions)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExerciseLogListScreen(
    permissionsGranted: Boolean,
    state: ExerciseLogUIState,
    onSyncData: () -> Unit,
    onLogNewExercise: () -> Unit,
    onLaunchPermissions: () -> Unit,
) {
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.exercise_log),
                            fontSize = 25.sp
                        )
                    }
                )
                HealthConnectComponent(
                    permissionsGranted = permissionsGranted,
                    healthConnectAvailability = state.isHealthConnectAvailable,
                    onPermissionsLaunch = onLaunchPermissions,
                    onSyncData = onSyncData
                )
                HorizontalDivider(thickness = 1.dp)
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onLogNewExercise() }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Exercise Log"
                )
            }
        }

    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 10.dp)
                .fillMaxSize(),
        ) {

            if (state.isLoading) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.exerciseLogs.isEmpty()) {
                Text(
                    modifier = Modifier.padding(top = 20.dp, start = 5.dp),
                    text = stringResource(R.string.no_exercise_log)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .padding(5.dp)
                        .fillMaxSize()
                ) {
                    state.exerciseLogsMap.forEach { (date, exerciseLogs) ->
                        stickyHeader {
                            Text(
                                text = date,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(
                                        color = PurpleGrey80,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 10.dp)
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                        }

                        items(exerciseLogs) { exerciseLog ->
                            ExerciseLogItem(log = exerciseLog)
                            Spacer(modifier = Modifier.height(15.dp))
                        }
                    }
                }
            }
        }
    }
}