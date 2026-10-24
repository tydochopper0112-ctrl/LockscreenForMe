package com.fold.lockscreen.ui.screens

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.fold.lockscreen.data.PreferencesManager
import com.fold.lockscreen.service.LockscreenWallpaperService
import com.fold.lockscreen.ui.components.InteractiveWysiwygPreview
import com.fold.lockscreen.ui.components.WallpaperGalleryRow
import com.fold.lockscreen.ui.theme.*
import kotlinx.coroutines.launch

val QUICK_LAUNCH_PRESETS = listOf(
    "com.sec.android.app.clockpackage" to "Đồng hồ & Báo thức (Samsung)",
    "com.samsung.android.calendar" to "Lịch (Samsung Calendar)",
    "com.google.android.deskclock" to "Google Clock"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainSettingsScreen(
    preferencesManager: PreferencesManager,
    settings: LockscreenSettings,
    isNotificationPermissionGranted: Boolean,
    notifications: List<NotificationItem>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var isCoverScreenMode by remember { mutableStateOf(true) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Lockscreen for me",
                                color = PureWhite,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CyanAccent
                            ) {
                                Text(
                                    text = "v1.1.0",
                                    color = Color.Black,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Kéo-thả trực quan WYSIWYG • Samsung Galaxy Z Fold 7",
                            color = CyanAccent,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        bottomBar = {
            Surface(
                color = DarkSurface,
                tonalElevation = 8.dp,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = { openLiveWallpaperPicker(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Wallpaper,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đặt làm Hình nền Lockscreen",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(bottom = 20.dp)
        ) {
            // 1. Interactive WYSIWYG Canvas (Drag & Drop + Pinch to zoom)
            InteractiveWysiwygPreview(
                settings = settings,
                isCoverScreen = isCoverScreenMode,
                notifications = notifications,
                onScreenModeChanged = { isCoverScreenMode = it },
                onClockLayoutChanged = { x, y, scale ->
                    coroutineScope.launch {
                        if (isCoverScreenMode) {
                            preferencesManager.updateCoverClock(x, y, scale)
                        } else {
                            preferencesManager.updateMainClock(x, y, scale)
                        }
                    }
                },
                onNotificationLayoutChanged = { x, y ->
                    coroutineScope.launch {
                        if (isCoverScreenMode) {
                            preferencesManager.updateCoverNotification(x, y)
                        } else {
                            preferencesManager.updateMainNotification(x, y)
                        }
                    }
                },
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Horizontal Wallpaper Gallery
            WallpaperGalleryRow(
                selectedWallpaperPath = settings.selectedWallpaperPath,
                onWallpaperSelected = { path ->
                    coroutineScope.launch {
                        preferencesManager.updateWallpaperPath(path)
                    }
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(22.dp))

            // 3. Notification Mode & System Bridge
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0x30FFFFFF))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kiểu hiển thị thông báo",
                            color = PureWhite,
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Permission status chip
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isNotificationPermissionGranted) Color(0xFF142E1F) else Color(0xFF332010),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isNotificationPermissionGranted) Color(0xFF00E676) else Color(0xFFFFAB00)
                            )
                        ) {
                            Text(
                                text = if (isNotificationPermissionGranted) "Đã cấp quyền" else "Chưa cấp quyền",
                                color = if (isNotificationPermissionGranted) Color(0xFF00E676) else Color(0xFFFFAB00),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (!isNotificationPermissionGranted) {
                        Spacer(modifier = Modifier.height(8.dp))
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
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Cấp quyền đọc thông báo trong Cài đặt", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Segmented selection for notification mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        NotificationDisplayMode.values().forEach { mode ->
                            val isSelected = settings.notificationMode == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) CyanAccent else DarkSurfaceVariant)
                                    .clickable {
                                        coroutineScope.launch { preferencesManager.updateNotificationMode(mode) }
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.displayName,
                                    color = if (isSelected) Color.Black else PureWhite,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Quick Toggles (Seconds & Double Tap Launch)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0x30FFFFFF))
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Show seconds switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Kim giây chuyển động (Vivid Red)",
                                color = PureWhite,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Kim giây màu đỏ nổi bật theo phong cách Live It",
                                color = Color(0xFFAAAAAA),
                                fontSize = 11.5.sp
                            )
                        }
                        Switch(
                            checked = settings.showSeconds,
                            onCheckedChange = {
                                coroutineScope.launch { preferencesManager.updateShowSeconds(it) }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = CyanAccent
                            )
                        )
                    }

                    Divider(
                        color = Color(0x20FFFFFF),
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    // Double Tap App selector
                    Text(
                        text = "Chạm đúp (Double-Tap) để mở ứng dụng",
                        color = PureWhite,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Chạm đúp vào đồng hồ hoặc thông báo sẽ rung và mở ứng dụng",
                        color = Color(0xFFAAAAAA),
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        QUICK_LAUNCH_PRESETS.forEach { (pkg, name) ->
                            val isSelected = settings.doubleTapPackage == pkg
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) DarkSurfaceVariant else Color.Transparent)
                                    .border(
                                        width = if (isSelected) 1.dp else 0.5.dp,
                                        color = if (isSelected) CyanAccent else Color(0x20FFFFFF),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        coroutineScope.launch { preferencesManager.updateDoubleTapPackage(pkg) }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        coroutineScope.launch { preferencesManager.updateDoubleTapPackage(pkg) }
                                    },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = CyanAccent,
                                        unselectedColor = Color(0xFF666666)
                                    ),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = name,
                                    color = PureWhite,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun openLiveWallpaperPicker(context: Context) {
    try {
        val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
            putExtra(
                WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                ComponentName(context, LockscreenWallpaperService::class.java)
            )
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        try {
            val fallback = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }
}
