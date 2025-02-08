package com.example.exerciselog.utils

import android.util.Log
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun ZonedDateTime.toLocalTimeFormat() : String {
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy - h:mm a")
    return this.toInstant().atZone(ZoneId.systemDefault()).format(formatter)
}

fun getLocalRangeFormat(statTime: ZonedDateTime, endTime: ZonedDateTime) : String {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy | h:mm a")
    val formatter = DateTimeFormatter.ofPattern("h:mm a")
    val start = statTime.toInstant().atZone(ZoneId.systemDefault()).format(dateFormatter)
    val end = endTime.toInstant().atZone(ZoneId.systemDefault()).format(formatter)
    return "$start - $end"
}

fun formattedTimeInHourAndMin(hour: Int, minute: Int): String {
    val formatter = DateTimeFormatter.ofPattern("H:m")
    val time = LocalTime.of(hour, minute).format(formatter)
    return "${hour}h ${minute}m"//time
}

fun formattedTime(hour: Int, minute: Int): String {
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
    val time = LocalTime.of(hour, minute).format(formatter)
    return time
}

fun Long.formattedMinToTime() : String {
    val hour = this / 60
    val minute = this % 60
    return if(hour == 0L) {
        "${minute} min"
    } else "${hour}h ${minute}m"
}

fun getDateInUTC(milliSecond: Long) : String {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(milliSecond), ZoneOffset.UTC)
    return date.format(dateFormatter)
}

fun Long.toZoneDateTime() : ZonedDateTime {
    return ZonedDateTime.ofInstant(
        Instant.ofEpochMilli(this),
        ZoneOffset.UTC)
}

fun LocalDateTime.formatAsDate() : String {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    return this.format(dateFormatter)
}

fun ZonedDateTime.formatAsDate() : String {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    return this.format(dateFormatter)
}

fun formatMillisToDate(milliSecond: Long) : String {
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val dateTime = ZonedDateTime.ofInstant(
        Instant.ofEpochMilli(milliSecond),
        ZoneId.systemDefault())
    return dateTime.format(dateFormatter)
}

