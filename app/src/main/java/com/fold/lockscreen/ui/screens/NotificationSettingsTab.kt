package com.fold.lockscreen.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fold.lockscreen.data.LockscreenSettings
import com.fold.lockscreen.data.NotificationDisplayMode
import com.fold.lockscreen.data.NotificationItem
import com.fold.lockscreen.ui.theme.CyanAccent
import com.fold.lockscreen.ui.theme.DarkSurface
import com.fold.lockscreen.ui.theme.DarkSurfaceVariant
import com.fold.lockscreen.ui.theme.PureWhite

@Composable
fun NotificationSettingsTab(
    settings: LockscreenSettings,
    isPermissionGranted: Boolean,
    notifications: List<NotificationItem>,
    onNotificationModeChanged: (NotificationDisplayMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Hệ thống Thông báo Lockscreen",
            style = MaterialTheme.typography.titleMedium,
            color = CyanAccent,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Sử dụng NotificationListenerService để cập nhật tức thì thông báo lên hình nền Canvas.",
            color = Color(0xFFAAAAAA),
            fontSize = 12.5.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
        )

        // Permission Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isPermissionGranted) Color(0xFF14241B) else Color(0xFF2B1D12)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(
                    if (isPermissionGranted) Color(0xFF00E676) else Color(0xFFFFAB00)
                )
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isPermissionGranted) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (isPermissionGranted) Color(0xFF00E676) else Color(0xFFFFAB00),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isPermissionGranted) "Quyền đọc thông báo: ĐÃ CẤP" else "Quyền đọc thông báo: CHƯA CẤP",
                        color = PureWhite,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (isPermissionGranted) {
                        "Dịch vụ nền LockscreenNotificationListenerService đang hoạt động và đồng bộ thời gian thực."
                    } else {
                        "Để hiển thị thông báo trên màn hình khoá, bạn cần cho phép ứng dụng đọc thông báo trong cài đặt hệ thống."
                    },
                    color = Color(0xFFCCCCCC),
                    fontSize = 12.5.sp
                )

                if (!isPermissionGranted) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFAB00),
                            contentColor = Color.Black
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cấp quyền trong Cài đặt", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Display Mode Selection
        Text(
            text = "Kiểu hiển thị thông báo trên hình nền",
            style = MaterialTheme.typography.titleMedium,
            color = CyanAccent,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            NotificationDisplayMode.values().forEach { mode ->
                val isSelected = settings.notificationMode == mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) DarkSurfaceVariant else DarkSurface)
                        .border(
                            width = if (isSelected) 1.5.dp else 0.5.dp,
                            color = if (isSelected) CyanAccent else Color(0x30FFFFFF),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onNotificationModeChanged(mode) }
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = { onNotificationModeChanged(mode) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = CyanAccent,
                            unselectedColor = Color(0xFF888888)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = mode.displayName,
                            color = PureWhite,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                        val desc = when (mode) {
                            NotificationDisplayMode.HIDDEN -> "Không vẽ bất kỳ thông báo nào lên Canvas."
                            NotificationDisplayMode.COUNTER -> "Hiển thị viên nang đếm số lượng thông báo chưa đọc."
                            NotificationDisplayMode.COMPACT_LIST -> "Hiển thị tối đa 3 thông báo mới nhất kèm Tên App và Tiêu đề."
                        }
                        Text(text = desc, color = Color(0xFFAAAAAA), fontSize = 11.5.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Current Active Notifications List
        Text(
            text = "Thông báo đang hoạt động (${notifications.size})",
            style = MaterialTheme.typography.titleMedium,
            color = CyanAccent,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Không có thông báo nào đang hoạt động.", color = Color(0xFF888888), fontSize = 13.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                notifications.take(5).forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.appName,
                                    color = CyanAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = item.title.ifBlank { "Không có tiêu đề" },
                                    color = PureWhite,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                if (item.text.isNotBlank()) {
                                    Text(
                                        text = item.text,
                                        color = Color(0xFFAAAAAA),
                                        fontSize = 12.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}
