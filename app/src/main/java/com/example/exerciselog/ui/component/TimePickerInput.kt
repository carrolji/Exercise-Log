package com.example.exerciselog.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.exerciselog.R
import com.example.exerciselog.utils.TestTags
import com.example.exerciselog.utils.TestTags.SELECTING_DURATION
import com.example.exerciselog.utils.TestTags.SELECTING_TIME
import com.example.exerciselog.utils.formattedTime
import com.example.exerciselog.utils.formattedTimeInHourAndMin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerInput(
    isDuration: Boolean = true,
    onTimeUpdate: (hour: Int, min: Int) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val timeState = rememberTimePickerState(
        is24Hour = isDuration
    )
    var timeSelected by remember { mutableStateOf("") }
    val source = remember { MutableInteractionSource() }
    if (source.collectIsPressedAsState().value) {
        showDialog = true
    }
    val testTag = if (isDuration) SELECTING_DURATION else SELECTING_TIME
    Column {
        Text(
            modifier = Modifier
                .padding(10.dp)
                .clickable(onClick = {
                    showDialog = true
                })
                .testTag(testTag),
            text = if (timeSelected.isEmpty()) {
                stringResource(R.string.select_time)
            } else timeSelected,
            color = if (timeSelected.isEmpty()) Color.Blue else Color.Black
        )
        if (showDialog) {
            Dialog(
                onDismissRequest = { showDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = true)
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.extraLarge
                        ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    shape = MaterialTheme.shapes.extraLarge
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                    ) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            text = stringResource(R.string.select_time)
                        )
                        TimeInput(
                            state = timeState
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Button(
                                modifier = Modifier.padding(end = 8.dp),
                                onClick = { showDialog = false }
                            ) {
                                Text(
                                    text = stringResource(R.string.cancel)
                                )
                            }
                            Button(
                                modifier = Modifier.padding(start = 8.dp).testTag(TestTags.OK_BUTTON),
                                onClick = {
                                    timeSelected = if (isDuration) {
                                        formattedTimeInHourAndMin(timeState.hour, timeState.minute)
                                    } else {
                                        formattedTime(timeState.hour, timeState.minute)
                                    }
                                    showDialog = false
                                    onTimeUpdate(timeState.hour, timeState.minute)
                                }
                            ) {
                                Text(text = stringResource(R.string.ok))
                            }
                        }
                    }
                }
            }
        }
    }
}


