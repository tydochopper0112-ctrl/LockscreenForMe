package com.fold.lockscreen.ui.screens

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fold.lockscreen.data.LockscreenSettings
import com.fold.lockscreen.data.NotificationItem
import com.fold.lockscreen.data.PreferencesManager
import com.fold.lockscreen.service.LockscreenWallpaperService
import com.fold.lockscreen.ui.components.PreviewCanvas
import com.fold.lockscreen.ui.theme.CyanAccent
import com.fold.lockscreen.ui.theme.DarkBackground
import com.fold.lockscreen.ui.theme.DarkSurface
import com.fold.lockscreen.ui.theme.PureWhite
import kotlinx.coroutines.launch

enum class MainTab(val title: String, val icon: ImageVector) {
    PREVIEW("Xem trước", Icons.Default.Visibility),
    CLOCK("Đồng hồ", Icons.Default.Schedule),
    FOLD_LAYOUT("Màn gập", Icons.Default.StayCurrentPortrait),
    NOTIFICATIONS("Thông báo", Icons.Default.Notifications)
}

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
    var selectedTab by remember { mutableStateOf(MainTab.PREVIEW) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Lockscreen for me",
                            color = PureWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tối ưu chuyên biệt Samsung Galaxy Z Fold 7",
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
            ) {
                // Apply Wallpaper Action Button
                Button(
                    onClick = {
                        openLiveWallpaperPicker(context)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanAccent,
                        contentColor = Color.Black
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Wallpaper,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đặt làm Hình nền Lockscreen",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                // Tab Bar
                NavigationBar(
                    containerColor = DarkSurface,
                    tonalElevation = 0.dp
                ) {
                    MainTab.values().forEach { tab ->
                        val isSelected = selectedTab == tab
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectedTab = tab },
                            icon = {
                                Icon(
                                    imageVector = tab.icon,
                                    contentDescription = tab.title
                                )
                            },
                            label = {
                                Text(
                                    text = tab.title,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                selectedTextColor = CyanAccent,
                                indicatorColor = CyanAccent,
                                unselectedIconColor = Color(0xFF888888),
                                unselectedTextColor = Color(0xFF888888)
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                MainTab.PREVIEW -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Mô phỏng Màn hình kép Z Fold 7",
                            color = CyanAccent,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Mọi thay đổi cài đặt sẽ lập tức cập nhật thời gian thực vào hình nền Canvas.",
                            color = Color(0xFFAAAAAA),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )
                        PreviewCanvas(
                            settings = settings,
                            notifications = notifications
                        )
                    }
                }
                MainTab.CLOCK -> {
                    ClockSettingsTab(
                        settings = settings,
                        onClockStyleChanged = {
                            coroutineScope.launch { preferencesManager.updateClockStyle(it) }
                        },
                        onFontStyleChanged = {
                            coroutineScope.launch { preferencesManager.updateFontStyle(it) }
                        },
                        onClockSizeChanged = {
                            coroutineScope.launch { preferencesManager.updateClockSize(it) }
                        },
                        onClockColorChanged = {
                            coroutineScope.launch {
                                preferencesManager.updateColors(it, settings.accentColor, settings.backgroundColor)
                            }
                        },
                        onAccentColorChanged = {
                            coroutineScope.launch {
                                preferencesManager.updateColors(settings.clockColor, it, settings.backgroundColor)
                            }
                        },
                        onBackgroundColorChanged = {
                            coroutineScope.launch {
                                preferencesManager.updateColors(settings.clockColor, settings.accentColor, it)
                            }
                        },
                        onTimeFormatChanged = { is24Hour, showSecs ->
                            coroutineScope.launch { preferencesManager.updateTimeFormat(is24Hour, showSecs) }
                        }
                    )
                }
                MainTab.FOLD_LAYOUT -> {
                    FoldPositioningTab(
                        settings = settings,
                        onCoverLayoutChanged = { cx, cy, nx, ny ->
                            coroutineScope.launch { preferencesManager.updateCoverLayout(cx, cy, nx, ny) }
                        },
                        onMainLayoutChanged = { cx, cy, nx, ny, pad ->
                            coroutineScope.launch { preferencesManager.updateMainLayout(cx, cy, nx, ny, pad) }
                        },
                        onDoubleTapPackageChanged = {
                            coroutineScope.launch { preferencesManager.updateDoubleTapPackage(it) }
                        }
                    )
                }
                MainTab.NOTIFICATIONS -> {
                    NotificationSettingsTab(
                        settings = settings,
                        isPermissionGranted = isNotificationPermissionGranted,
                        notifications = notifications,
                        onNotificationModeChanged = {
                            coroutineScope.launch { preferencesManager.updateNotificationMode(it) }
                        }
                    )
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
