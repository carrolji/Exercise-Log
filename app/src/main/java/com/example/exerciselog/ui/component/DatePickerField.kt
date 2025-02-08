package com.example.exerciselog.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.exerciselog.R
import com.example.exerciselog.utils.getDateInUTC
import com.example.exerciselog.utils.toZoneDateTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime

@OptIn(ExperimentalMaterial3Api::class)
object PastOrPresentSelectableDates : SelectableDates {
    private val now = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000

    @ExperimentalMaterial3Api
    override fun isSelectableDate(utcTimeMillis: Long): Boolean {
        return utcTimeMillis <= now
    }

    @ExperimentalMaterial3Api
    override fun isSelectableYear(year: Int): Boolean {
        return year <= LocalDate.now().year
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    onDateUpdate: (date: ZonedDateTime) -> Unit
) {

    val datePickerState = rememberDatePickerState(
        selectableDates = PastOrPresentSelectableDates
    )
    var showDialog by remember { mutableStateOf(false) }
    Text(
        modifier = Modifier
            .padding(vertical = 10.dp)
            .clickable(onClick = {
                showDialog = true
            }),
        text = if (datePickerState.selectedDateMillis == null) {
            stringResource(R.string.select_date)
        } else {
            getDateInUTC(datePickerState.selectedDateMillis!!)
        },
        color = if (datePickerState.selectedDateMillis == null) Color.Blue else Color.Black
    )
    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = {
                showDialog = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onDateUpdate(datePickerState.selectedDateMillis!!.toZoneDateTime())
                    }
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                    }
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
            },
            colors = DatePickerDefaults.colors()
        ) {
            DatePicker(
                state = datePickerState
            )
        }
    }
}