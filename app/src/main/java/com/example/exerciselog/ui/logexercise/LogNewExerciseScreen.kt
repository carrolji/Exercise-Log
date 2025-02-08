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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.exerciselog.data.ExerciseType
import com.example.exerciselog.ui.component.DatePickerField
import com.example.exerciselog.ui.component.DropDownOptions
import com.example.exerciselog.ui.component.TimePickerInput
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
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New Exercise Log",
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
                label = "Exercise Type",
                selectedValue = state.type.name,
                onValueChangedEvent = {
                    onAction(ExerciseLogDetailUIEvent.UpdateExerciseType(it))
                }
            )
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                value = state.caloriesBurned.toString(),
                label = {
                    Text(text = "Calories Burned in Cal")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                onValueChange = {
                    onAction(ExerciseLogDetailUIEvent.UpdateExerciseCalories(it.toIntOrNull() ?: 0))
                }
            )
            Row {
                Text(
                    modifier = Modifier.padding(top = 10.dp, end = 10.dp),
                    text = "Start Time: "
                )
                DatePickerField {
                    onAction(ExerciseLogDetailUIEvent.UpdateExerciseDate(it))
                }
                TimePickerInput(
                    isDuration = false
                ) { hour, min ->
                    onAction(ExerciseLogDetailUIEvent.UpdateExerciseStartTime(hour, min))
                }
            }

            Row {
                Text(
                    modifier = Modifier.padding(top = 10.dp, end = 10.dp),
                    text = "Duration: "
                )
                TimePickerInput { hour, min ->
                    onAction(ExerciseLogDetailUIEvent.UpdateExerciseDuration(hour, min))
                }
            }

            Button(
                modifier = Modifier.padding(top = 10.dp),
                onClick = {
                    onAction(ExerciseLogDetailUIEvent.OnSaveNewExercise)
                    onSave()
                }
            ) {
                Text(text = "Save")
            }
        }
    }
}