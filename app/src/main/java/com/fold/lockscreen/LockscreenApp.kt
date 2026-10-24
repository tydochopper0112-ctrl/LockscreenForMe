package com.fold.lockscreen

import android.app.Application
import android.content.Intent
import android.util.Log
import com.fold.lockscreen.data.NotificationRepository
import com.fold.lockscreen.data.PreferencesManager
import com.fold.lockscreen.service.LockscreenNotificationListenerService
import com.fold.lockscreen.ui.CrashActivity

class LockscreenApp : Application() {

    lateinit var preferencesManager: PreferencesManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        // 1. Global Crash Handler: catches unhandled exceptions and shows CrashActivity
        setupGlobalCrashHandler()

        // 2. Safe initialization of PreferencesManager
        try {
            preferencesManager = PreferencesManager(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // 3. Safe check of Notification Listener Permission
        try {
            val hasPermission = LockscreenNotificationListenerService.isPermissionGranted(this)
            NotificationRepository.updatePermissionStatus(hasPermission)
        } catch (e: Exception) {
            e.printStackTrace()
            NotificationRepository.updatePermissionStatus(false)
        }
    }

    private fun setupGlobalCrashHandler() {
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("LockscreenApp", "FATAL CRASH DETECTED", throwable)
                val intent = Intent(applicationContext, CrashActivity::class.java).apply {
                    putExtra(CrashActivity.EXTRA_ERROR_MESSAGE, throwable.localizedMessage ?: throwable.toString())
                    putExtra(CrashActivity.EXTRA_STACK_TRACE, Log.getStackTraceString(throwable))
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                applicationContext.startActivity(intent)
            } catch (e: Exception) {
                e.printStackTrace()
                defaultHandler?.uncaughtException(thread, throwable)
            }
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(10)
        }
    }

    companion object {
        lateinit var instance: LockscreenApp
            private set
    }
}
