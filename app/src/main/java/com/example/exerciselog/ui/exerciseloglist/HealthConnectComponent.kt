package com.example.exerciselog.ui.exerciseloglist

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.exerciselog.R
import com.example.exerciselog.data.HealthConnectAvailability

@Composable
fun HealthConnectComponent(
    permissionsGranted: Boolean,
    healthConnectAvailability: HealthConnectAvailability,
    onSyncData: () -> Unit,
    onPermissionsLaunch: () -> Unit = {},
) {
    when (healthConnectAvailability) {
        HealthConnectAvailability.INSTALLED -> {

            if (!permissionsGranted) {
                InformationField(
                    info = stringResource(R.string.permissions_required)
                )
                Button(
                    modifier = Modifier.padding(start = 10.dp, bottom = 5.dp),
                    onClick = {
                        onPermissionsLaunch()
                    }
                ) {
                    Text(text = stringResource(R.string.request_permissions))
                }
            } else {
                InformationField(
                    info = stringResource(R.string.health_connect_ready)
                )
                Button(
                    modifier = Modifier.padding(start = 10.dp, bottom = 5.dp),
                    onClick = {
                        onSyncData()
                    }
                ) {
                    Text(text = stringResource(R.string.sync_data))
                }
            }

        }

        HealthConnectAvailability.NOT_INSTALLED -> {
            InformationField(
                info = stringResource(R.string.health_connect_installation_required)
            )
        }

        HealthConnectAvailability.NOT_SUPPORTED -> {
            InformationField(
                info = stringResource(R.string.health_connect_not_supported)
            )
        }
    }
}

@Composable
fun InformationField(info: String) {
    Text(
        modifier = Modifier.padding(start = 15.dp),
        text = info,
        fontSize = 12.sp,
        fontStyle = FontStyle.Italic
    )
}