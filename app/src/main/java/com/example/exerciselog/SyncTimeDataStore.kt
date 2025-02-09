package com.example.exerciselog

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.ZonedDateTime

const val PREFERENCES_NAME = "sync_time_preferences"

class SyncTimeDataStore(private val context: Context) {
    private val Context.dataStore by preferencesDataStore(name = PREFERENCES_NAME)

    val lastSevenDays = ZonedDateTime.now().minusDays(7).toInstant().toEpochMilli()

    private val LAST_SYNC_KEY = longPreferencesKey("last_sync_time")

    suspend fun saveLastSyncTime(time: Long) {
        context.dataStore.edit { it[LAST_SYNC_KEY] = time }
    }

    suspend fun getLastSyncTime(): Long {
        return context.dataStore.data.map { it[LAST_SYNC_KEY] ?: lastSevenDays }.first()
    }
}