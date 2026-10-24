package com.fold.lockscreen.service

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.TextUtils
import com.fold.lockscreen.data.NotificationItem
import com.fold.lockscreen.data.NotificationRepository

class LockscreenNotificationListenerService : NotificationListenerService() {

    override fun onListenerConnected() {
        super.onListenerConnected()
        NotificationRepository.updatePermissionStatus(true)
        refreshActiveNotifications()
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        NotificationRepository.updatePermissionStatus(false)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let {
            if (shouldIncludeNotification(it)) {
                val item = extractNotificationItem(it)
                NotificationRepository.addOrUpdateNotification(item)
            }
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn?.let {
            NotificationRepository.removeNotification(it.key)
        }
    }

    private fun refreshActiveNotifications() {
        try {
            val sbns = activeNotifications ?: return
            val items = sbns
                .filter { shouldIncludeNotification(it) }
                .sortedByDescending { it.postTime }
                .map { extractNotificationItem(it) }
            NotificationRepository.setNotifications(items)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shouldIncludeNotification(sbn: StatusBarNotification): Boolean {
        // Filter out ongoing background notifications (e.g. download services, ongoing calls) if preferred
        val notification = sbn.notification ?: return false
        val isOngoing = (notification.flags and Notification.FLAG_ONGOING_EVENT) != 0
        if (isOngoing) return false

        // Filter system systemui status bar noise
        val pkg = sbn.packageName
        if (pkg == "android" || pkg == "com.android.systemui") return false

        return true
    }

    private fun extractNotificationItem(sbn: StatusBarNotification): NotificationItem {
        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString()
            ?: ""

        val pm = applicationContext.packageManager
        val appName = try {
            val appInfo = pm.getApplicationInfo(sbn.packageName, PackageManager.GET_META_DATA)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            sbn.packageName.substringAfterLast('.')
        }

        return NotificationItem(
            key = sbn.key,
            packageName = sbn.packageName,
            appName = appName,
            title = title,
            text = text,
            postTime = sbn.postTime
        )
    }

    companion object {
        fun isPermissionGranted(context: Context): Boolean {
            val pkgName = context.packageName
            val flat = Settings.Secure.getString(
                context.contentResolver,
                "enabled_notification_listeners"
            )
            if (!TextUtils.isEmpty(flat)) {
                val names = flat.split(":").toTypedArray()
                for (name in names) {
                    val cn = ComponentName.unflattenFromString(name)
                    if (cn != null && TextUtils.equals(pkgName, cn.packageName)) {
                        return true
                    }
                }
            }
            return false
        }
    }
}
