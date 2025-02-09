/*
 * Copyright 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.exerciselog.data

import android.content.Context
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.HealthConnectClient.Companion.SDK_AVAILABLE
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.changes.Change
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ChangesTokenRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.IOException

// The minimum android level that can use Health Connect
const val MIN_SUPPORTED_SDK = Build.VERSION_CODES.O_MR1

/**
 * Demonstrates reading and writing from Health Connect.
 */
class HealthConnectManager(private val context: Context) {
  private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(context) }

  fun checkAvailability() : Flow<HealthConnectAvailability> = flow {
    val state = when {
      HealthConnectClient.getSdkStatus(context) == SDK_AVAILABLE -> HealthConnectAvailability.INSTALLED
      isSupported() -> HealthConnectAvailability.NOT_INSTALLED
      else -> HealthConnectAvailability.NOT_SUPPORTED
    }
    emit(state)
  }

  /**
   * Determines whether all the specified permissions are already granted. It is recommended to
   * call [PermissionController.getGrantedPermissions] first in the permissions flow, as if the
   * permissions are already granted then there is no need to request permissions via
   * [PermissionController.createRequestPermissionResultContract].
   */
  suspend fun hasAllPermissions(permissions: Set<String>): Boolean {
    return healthConnectClient.permissionController.getGrantedPermissions().containsAll(permissions)
  }

  fun requestPermissionsActivityContract(): ActivityResultContract<Set<String>, Set<String>> {
    return PermissionController.createRequestPermissionResultContract()
  }

  /**
   * Obtains a list of [ExerciseSessionRecord]s in a specified time frame.
   */
  suspend fun readExerciseSessions(start: Instant, end: Instant): List<ExerciseSessionRecord> {
    val request = ReadRecordsRequest(
      recordType = ExerciseSessionRecord::class,
      timeRangeFilter = TimeRangeFilter.between(start, end)
    )
    val response = healthConnectClient.readRecords(request)
    return response.records
  }

  /**
   * Obtains a list of [ExerciseSessionRecord]s in a specified time frame.
   */
  suspend fun readCaloriesBurnedRecord(start: Instant, end: Instant): List<TotalCaloriesBurnedRecord> {
    val request = ReadRecordsRequest(
      recordType = TotalCaloriesBurnedRecord::class,
      timeRangeFilter = TimeRangeFilter.between(start, end)
    )
    val response = healthConnectClient.readRecords(request)
    return response.records
  }

  private fun isSupported() = Build.VERSION.SDK_INT >= MIN_SUPPORTED_SDK

  /**
   * Obtains a changes token for Health Connect Differential Changes API.
   */
  suspend fun getChangesToken(): String {
    return healthConnectClient.getChangesToken(
      ChangesTokenRequest(
        setOf(
          ExerciseSessionRecord::class,
          TotalCaloriesBurnedRecord::class,
        )
      )
    )
  }

  /**
   * Retrieve changes from a changes token.
   */
  fun getChanges(token: String): Flow<ChangesMessage> = flow {
    var nextChangesToken = token
    do {
      val response = healthConnectClient.getChanges(nextChangesToken)
      if (response.changesTokenExpired) {
        // As described here: https://developer.android.com/guide/health-and-fitness/health-connect/data-and-data-types/differential-changes-api
        // tokens are only valid for 30 days. It is important to check whether the token has
        // expired. As well as ensuring there is a fallback to using the token (for example
        // importing data since a certain date), more importantly, the app should ensure
        // that the changes API is used sufficiently regularly that tokens do not expire.
        throw IOException("Changes token has expired")
      }
      emit(ChangesMessage.ChangeList(response.changes))
      nextChangesToken = response.nextChangesToken
    } while (response.hasMore)
    emit(ChangesMessage.NoMoreChanges(nextChangesToken))
  }

  // Represents the two types of messages that can be sent in a Changes flow.
  sealed class ChangesMessage {
    data class NoMoreChanges(val nextChangesToken: String) : ChangesMessage()
    data class ChangeList(val changes: List<Change>) : ChangesMessage()
  }
}

/**
 * Health Connect requires that the underlying Health Connect APK is installed on the device.
 * [HealthConnectAvailability] represents whether this APK is indeed installed, whether it is not
 * installed but supported on the device, or whether the device is not supported (based on Android
 * version).
 */
enum class HealthConnectAvailability {
  INSTALLED,
  NOT_INSTALLED,
  NOT_SUPPORTED,
}
