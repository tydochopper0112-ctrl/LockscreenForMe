package com.fold.lockscreen.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationItem(
    val key: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val postTime: Long = System.currentTimeMillis()
)

object NotificationRepository {
    private val _notifications = MutableStateFlow<List<NotificationItem>>(
        listOf(
            NotificationItem(
                key = "demo_1",
                packageName = "com.zing.zalo",
                appName = "Zalo",
                title = "Nguyễn Văn A",
                text = "Hẹn họp lúc 14:00 chiều nay nhé!"
            ),
            NotificationItem(
                key = "demo_2",
                packageName = "com.google.android.gm",
                appName = "Gmail",
                title = "GitHub Security",
                text = "New release published: v1.0.0"
            ),
            NotificationItem(
                key = "demo_3",
                packageName = "org.telegram.messenger",
                appName = "Telegram",
                title = "Nhóm Công nghệ",
                text = "Đã cập nhật firmware Samsung Galaxy Z Fold 7."
            )
        )
    )
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    fun updatePermissionStatus(granted: Boolean) {
        _isPermissionGranted.value = granted
    }

    fun setNotifications(list: List<NotificationItem>) {
        _notifications.value = list
    }

    fun addOrUpdateNotification(item: NotificationItem) {
        val current = _notifications.value.toMutableList()
        val index = current.indexOfFirst { it.key == item.key }
        if (index >= 0) {
            current[index] = item
        } else {
            current.add(0, item)
        }
        _notifications.value = current.take(20)
    }

    fun removeNotification(key: String) {
        _notifications.value = _notifications.value.filterNot { it.key == key }
    }

    fun clearAll() {
        _notifications.value = emptyList()
    }
}
