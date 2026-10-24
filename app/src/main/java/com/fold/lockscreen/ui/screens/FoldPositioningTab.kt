package com.fold.lockscreen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fold.lockscreen.data.FoldScreenType
import com.fold.lockscreen.data.LockscreenSettings
import com.fold.lockscreen.ui.theme.CyanAccent
import com.fold.lockscreen.ui.theme.DarkSurface
import com.fold.lockscreen.ui.theme.DarkSurfaceVariant
import com.fold.lockscreen.ui.theme.PureWhite

val QUICK_APP_PRESETS = listOf(
    "com.sec.android.app.clockpackage" to "Đồng hồ & Báo thức (Samsung)",
    "com.samsung.android.calendar" to "Lịch Samsung Calendar",
    "com.google.android.deskclock" to "Google Clock",
    "com.sec.android.app.popupcalculator" to "Máy tính Samsung Calculator"
)

@Composable
fun FoldPositioningTab(
    settings: LockscreenSettings,
    onCoverLayoutChanged: (clockX: Float, clockY: Float, notifX: Float, notifY: Float) -> Unit,
    onMainLayoutChanged: (clockX: Float, clockY: Float, notifX: Float, notifY: Float, taskbarPadding: Int) -> Unit,
    onDoubleTapPackageChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedScreen by remember { mutableStateOf(FoldScreenType.COVER) }
    val scrollState = rememberScrollState()

    var customPackageInput by remember(settings.doubleTapPackage) {
        mutableStateOf(settings.doubleTapPackage)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Tối ưu hoá Màn hình gập Z Fold 7",
            style = MaterialTheme.typography.titleMedium,
            color = CyanAccent,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Cấu hình toạ độ độc lập để hình nền tự thích ứng chuẩn xác khi gập/mở máy mà không bị đè lên Widget hoặc One UI Taskbar.",
            color = Color(0xFFAAAAAA),
            fontSize = 12.5.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // Screen Selector: Cover vs Main
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            SegmentedButton(
                selected = selectedScreen == FoldScreenType.COVER,
                onClick = { selectedScreen = FoldScreenType.COVER },
                shape = RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = CyanAccent,
                    activeContentColor = Color.Black,
                    inactiveContainerColor = DarkSurface,
                    inactiveContentColor = PureWhite
                )
            ) {
                Text("Màn ngoài (Cover)")
            }

            SegmentedButton(
                selected = selectedScreen == FoldScreenType.MAIN,
                onClick = { selectedScreen = FoldScreenType.MAIN },
                shape = RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = CyanAccent,
                    activeContentColor = Color.Black,
                    inactiveContainerColor = DarkSurface,
                    inactiveContentColor = PureWhite
                )
            ) {
                Text("Màn trong (Main)")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (selectedScreen == FoldScreenType.COVER) {
            // Configuration for Cover Screen
            val cfg = settings.coverLayout

            CoordinateSliderCard(
                title = "Toạ độ Đồng hồ trên Màn hình ngoài",
                xPercent = cfg.clockXPercent,
                yPercent = cfg.clockYPercent,
                onXChanged = {
                    onCoverLayoutChanged(it, cfg.clockYPercent, cfg.notificationXPercent, cfg.notificationYPercent)
                },
                onYChanged = {
                    onCoverLayoutChanged(cfg.clockXPercent, it, cfg.notificationXPercent, cfg.notificationYPercent)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            CoordinateSliderCard(
                title = "Toạ độ Thông báo trên Màn hình ngoài",
                xPercent = cfg.notificationXPercent,
                yPercent = cfg.notificationYPercent,
                onXChanged = {
                    onCoverLayoutChanged(cfg.clockXPercent, cfg.clockYPercent, it, cfg.notificationYPercent)
                },
                onYChanged = {
                    onCoverLayoutChanged(cfg.clockXPercent, cfg.clockYPercent, cfg.notificationXPercent, it)
                }
            )
        } else {
            // Configuration for Main Inner Screen
            val cfg = settings.mainLayout

            CoordinateSliderCard(
                title = "Toạ độ Đồng hồ trên Màn hình trong",
                xPercent = cfg.clockXPercent,
                yPercent = cfg.clockYPercent,
                onXChanged = {
                    onMainLayoutChanged(it, cfg.clockYPercent, cfg.notificationXPercent, cfg.notificationYPercent, cfg.taskbarPaddingBottomDp)
                },
                onYChanged = {
                    onMainLayoutChanged(cfg.clockXPercent, it, cfg.notificationXPercent, cfg.notificationYPercent, cfg.taskbarPaddingBottomDp)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            CoordinateSliderCard(
                title = "Toạ độ Thông báo trên Màn hình trong",
                xPercent = cfg.notificationXPercent,
                yPercent = cfg.notificationYPercent,
                onXChanged = {
                    onMainLayoutChanged(cfg.clockXPercent, cfg.clockYPercent, it, cfg.notificationYPercent, cfg.taskbarPaddingBottomDp)
                },
                onYChanged = {
                    onMainLayoutChanged(cfg.clockXPercent, cfg.clockYPercent, cfg.notificationXPercent, it, cfg.taskbarPaddingBottomDp)
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Taskbar Protection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(Color(0x30FFFFFF))
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Bảo vệ vùng One UI Taskbar: ${cfg.taskbarPaddingBottomDp} dp",
                            color = PureWhite,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "Tự động chừa khoảng trống phía dưới màn hình trong để thông báo không bị Taskbar của Samsung che khuất.",
                        color = Color(0xFFAAAAAA),
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                    )
                    Slider(
                        value = cfg.taskbarPaddingBottomDp.toFloat(),
                        onValueChange = {
                            onMainLayoutChanged(cfg.clockXPercent, cfg.clockYPercent, cfg.notificationXPercent, cfg.notificationYPercent, it.toInt())
                        },
                        valueRange = 0f..120f,
                        colors = SliderDefaults.colors(
                            thumbColor = CyanAccent,
                            activeTrackColor = CyanAccent,
                            inactiveTrackColor = DarkSurfaceVariant
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Double Tap Quick Launch
        Text(
            text = "Hành động Chạm đúp (Double Tap)",
            style = MaterialTheme.typography.titleMedium,
            color = CyanAccent,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Khi chạm đúp 2 lần vào vùng Đồng hồ hoặc Thông báo trên màn hình khoá, máy sẽ lập tức mở ứng dụng này kèm rung phản hồi haptic.",
            color = Color(0xFFAAAAAA),
            fontSize = 12.5.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            QUICK_APP_PRESETS.forEach { (pkg, name) ->
                val isSelected = settings.doubleTapPackage == pkg
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) DarkSurfaceVariant else DarkSurface)
                        .border(
                            width = if (isSelected) 1.5.dp else 0.5.dp,
                            color = if (isSelected) CyanAccent else Color(0x30FFFFFF),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onDoubleTapPackageChanged(pkg) }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = if (isSelected) CyanAccent else Color(0xFF888888),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = name, color = PureWhite, fontSize = 13.5.sp, fontWeight = FontWeight.Medium)
                        Text(text = pkg, color = Color(0xFF777777), fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Custom package input
        OutlinedTextField(
            value = customPackageInput,
            onValueChange = { customPackageInput = it },
            label = { Text("Tên gói tuỳ biến (Package Name)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                TextButton(onClick = { onDoubleTapPackageChanged(customPackageInput.trim()) }) {
                    Text("Lưu", color = CyanAccent)
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyanAccent,
                unfocusedBorderColor = Color(0x40FFFFFF),
                focusedTextColor = PureWhite,
                unfocusedTextColor = PureWhite
            )
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun CoordinateSliderCard(
    title: String,
    xPercent: Float,
    yPercent: Float,
    onXChanged: (Float) -> Unit,
    onYChanged: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0x30FFFFFF))
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                color = PureWhite,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))

            // X Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Vị trí ngang (Toạ độ X)", color = Color(0xFFAAAAAA), fontSize = 12.sp)
                Text("${(xPercent * 100).toInt()}%", color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = xPercent,
                onValueChange = onXChanged,
                valueRange = 0.1f..0.9f,
                colors = SliderDefaults.colors(
                    thumbColor = CyanAccent,
                    activeTrackColor = CyanAccent,
                    inactiveTrackColor = DarkSurfaceVariant
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Y Slider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Vị trí dọc (Toạ độ Y)", color = Color(0xFFAAAAAA), fontSize = 12.sp)
                Text("${(yPercent * 100).toInt()}%", color = CyanAccent, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            Slider(
                value = yPercent,
                onValueChange = onYChanged,
                valueRange = 0.1f..0.9f,
                colors = SliderDefaults.colors(
                    thumbColor = CyanAccent,
                    activeTrackColor = CyanAccent,
                    inactiveTrackColor = DarkSurfaceVariant
                )
            )
        }
    }
}
