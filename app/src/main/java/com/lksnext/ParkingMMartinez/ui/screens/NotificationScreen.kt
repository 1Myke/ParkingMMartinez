package com.lksnext.ParkingMMartinez.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.NotificationImportant
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lksnext.ParkingMMartinez.R
import com.lksnext.ParkingMMartinez.ui.theme.LksOrange
import com.lksnext.ParkingMMartinez.ui.theme.automaticRed
import com.lksnext.ParkingMMartinez.ui.viewmodel.NotificationViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun NotificationScreen(viewModel: NotificationViewModel) {
    val context = LocalContext.current
    val alerts = viewModel.notifications
    val isBatteryOptimized = viewModel.isBatteryOptimized

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.initViewModel(context)
        viewModel.markAllAsRead()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp, start = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.notification_system_alerts),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Cubo de basura general
            if (alerts.isNotEmpty()) {
                IconButton(onClick = { viewModel.deleteAllNotifications() }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Borrar todas las notificaciones",
                        tint = Color.Red.copy(alpha = 0.7f)
                    )
                }
            }
        }

        if (isBatteryOptimized) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .clickable {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = automaticRed),
                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.notification_unrestricted_battery),
                        color = Color.Red,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (alerts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EmptyNotificationsView()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(alerts, key = { it.id }) { notification ->
                    val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(notification.timestamp ?: java.util.Date())

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = automaticRed),
                        border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationImportant,
                                contentDescription = null,
                                tint = Color.Red,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))

                            // Contenido de la notificación
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = notification.title,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Red
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = notification.body,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                            }

                            IconButton(
                                onClick = { viewModel.deleteNotification(notification.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Borrar notificación",
                                    tint = Color.Red.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyNotificationsView() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.NotificationsActive,
            contentDescription = stringResource(R.string.notification_screen_icon_desc),
            tint = LksOrange,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.notification_screen_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.notification_screen_body),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
    }
}