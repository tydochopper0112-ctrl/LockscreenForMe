package com.fold.lockscreen.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.fold.lockscreen.LockscreenApp
import com.fold.lockscreen.data.LockscreenSettings
import com.fold.lockscreen.data.NotificationRepository
import com.fold.lockscreen.service.LockscreenNotificationListenerService
import com.fold.lockscreen.ui.screens.MainSettingsScreen
import com.fold.lockscreen.ui.theme.LockscreenTheme

class MainActivity : ComponentActivity() {

    private val preferencesManager by lazy {
        (application as LockscreenApp).preferencesManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LockscreenTheme {
                val settings by preferencesManager.settingsFlow.collectAsState(initial = LockscreenSettings())
                val notifications by NotificationRepository.notifications.collectAsState()
                val isPermissionGranted by NotificationRepository.isPermissionGranted.collectAsState()

                MainSettingsScreen(
                    preferencesManager = preferencesManager,
                    settings = settings,
                    isNotificationPermissionGranted = isPermissionGranted,
                    notifications = notifications
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val granted = LockscreenNotificationListenerService.isPermissionGranted(this)
        NotificationRepository.updatePermissionStatus(granted)
    }
}
