package com.example.exerciselog.ui.exerciseloglist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RunCircle
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.exerciselog.R
import com.example.exerciselog.data.ExerciseType
import com.example.exerciselog.domain.ExerciseLog
import com.example.exerciselog.ui.theme.GrayLight
import com.example.exerciselog.utils.formattedMinToTime
import com.example.exerciselog.utils.getLocalRangeFormat
import java.time.ZonedDateTime
import java.util.UUID

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExerciseLogItem(
    log: ExerciseLog
) {
    var isDropDownOptionShowing by rememberSaveable {
        mutableStateOf(false)
    }

    Box {
        ElevatedCard(
            shape = RoundedCornerShape(22.dp),
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 4.dp
            ),
            modifier = Modifier
                .height(150.dp)
                .combinedClickable(
                    onLongClick = {
                        //Drop down item
                        isDropDownOptionShowing = !isDropDownOptionShowing
                    },
                    onClick = {

                    }
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(GrayLight) //TODO: add highlight here
                    .padding(horizontal = 18.dp, vertical = 12.dp),
            ) {
                Row(modifier = Modifier.padding(top = 5.dp)) {
                    Icon(
                        imageVector = Icons.Default.RunCircle,
                        contentDescription = "Exercise Type Icon"
                    )
                    Text(
                        modifier = Modifier.padding(start = 5.dp),
                        text = log.type.name.replace("_", " "),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 16.sp,
                    )
                }
                Text(
                    modifier = Modifier.padding(top = 5.dp),
                    text = getLocalRangeFormat(log.startTime, log.endTime),
                    fontSize = 14.sp,
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExerciseInfo(
                    name = stringResource(R.string.duration),
                    value = log.duration.formattedMinToTime(),
                )

                ExerciseInfo(
                    name = stringResource(R.string.calories_burned),
                    value = log.caloriesBurned.toString(),
                    unit = stringResource(R.string.unit_cal)
                )
            }
        }

        DropdownMenu(
            expanded = isDropDownOptionShowing,
            onDismissRequest = { isDropDownOptionShowing = false },
            offset = DpOffset(30.dp, 0.dp)
        ) {
            DropdownMenuItem(
                text = { Text(text = stringResource(R.string.delete)) },
                onClick = {}
            )
        }
    }
}

@Composable
fun ExerciseInfo(
    name: String,
    value: String,
    unit: String = "",
) {
    Row {
        Text(
            text = "$name : ",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "$value $unit",
            fontSize = 14.sp,
        )
    }
}

@Preview
@Composable
fun ExerciseLogItemPreview() {
    val exerciseLog = ExerciseLog(
        exerciseId = UUID.randomUUID().toString(),
        type = ExerciseType.OTHER_WORKOUT,
        duration = 60,
        caloriesBurned = 100,
        startTime = ZonedDateTime.now().minusMinutes(30),
        endTime = ZonedDateTime.now()
    )
    ExerciseLogItem(exerciseLog)
}