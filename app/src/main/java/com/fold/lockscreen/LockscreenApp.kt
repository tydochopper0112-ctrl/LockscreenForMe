package com.fold.lockscreen

import android.app.Application
import com.fold.lockscreen.data.NotificationRepository
import com.fold.lockscreen.data.PreferencesManager
import com.fold.lockscreen.service.LockscreenNotificationListenerService

class LockscreenApp : Application() {

    lateinit var preferencesManager: PreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        preferencesManager = PreferencesManager(this)

        // Check initial notification listener permission state
        val hasPermission = LockscreenNotificationListenerService.isPermissionGranted(this)
        NotificationRepository.updatePermissionStatus(hasPermission)
    }

    companion object {
        lateinit var instance: LockscreenApp
            private set
    }
}
