package com.example.exerciselog.ui.logexercise

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.exerciselog.R
import com.example.exerciselog.data.ExerciseType
import com.example.exerciselog.ui.component.DatePickerField
import com.example.exerciselog.ui.component.DropDownOptions
import com.example.exerciselog.ui.component.TimePickerInput
import com.example.exerciselog.utils.TestTags
import org.koin.androidx.compose.koinViewModel

@Composable
fun LogNewExerciseScreenCore(
    viewModel: LogNewExerciseViewModel = koinViewModel(),
    onSave: () -> Unit
) {
    LogNewExerciseScreen(
        state = viewModel.state,
        onAction = viewModel::onAction,
        onSave = onSave
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogNewExerciseScreen(
    state: ExerciseLogDetailUIState,
    onAction: (ExerciseLogDetailUIEvent) -> Unit,
    onSave: () -> Unit,
) {
    var onDateSelected by remember { mutableStateOf(false) }
    var onTimeSelected by remember { mutableStateOf(false) }
    var onDurationSelected by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.new_exercise_title),
                        fontSize = 25.sp
                    )
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .fillMaxSize(),
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            DropDownOptions(
                options = ExerciseType.entries.toList(),
                label = stringResource(R.string.exercise_type_input),
                selectedValue = state.type.name,
                onValueChangedEvent = {
                    onAction(ExerciseLogDetailUIEvent.UpdateExerciseType(it))
                }
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp)
                    .testTag(TestTags.CALORIES_TEXT_FIELD),
                value = if(state.caloriesBurned != null) state.caloriesBurned.toString() else "",
                label = {
                    Text(text = stringResource(R.string.calories_burned_input))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                onValueChange = {
                    if(it != "") {
                        onAction(ExerciseLogDetailUIEvent.UpdateExerciseCalories(it.toInt()))
                    } else {
                        onAction(ExerciseLogDetailUIEvent.UpdateExerciseCalories(null))
                    }
                }
            )
            Row {
                Text(
                    modifier = Modifier.padding(top = 10.dp, end = 10.dp),
                    text = stringResource(R.string.start_time_input)
                )
                DatePickerField {
                    onDateSelected = true
                    onAction(ExerciseLogDetailUIEvent.UpdateExerciseDate(it))
                }
                TimePickerInput(
                    isDuration = false
                ) { hour, min ->
                    onTimeSelected = true
                    onAction(ExerciseLogDetailUIEvent.UpdateExerciseStartTime(hour, min))
                }
            }

            Row {
                Text(
                    modifier = Modifier.padding(top = 10.dp, end = 10.dp),
                    text = stringResource(R.string.duration_input)
                )
                TimePickerInput { hour, min ->
                    onDurationSelected = true
                    onAction(ExerciseLogDetailUIEvent.UpdateExerciseDuration(hour, min))
                }
            }

            Button(
                enabled = onTimeSelected && onDateSelected && onDurationSelected,
                modifier = Modifier.padding(top = 10.dp).testTag(TestTags.SAVE_EXERCISE_LOG),
                onClick = {
                    onAction(ExerciseLogDetailUIEvent.OnSaveNewExercise)
                    onSave()
                }
            ) {
                Text(text = stringResource(R.string.save))
            }
        }
    }
}