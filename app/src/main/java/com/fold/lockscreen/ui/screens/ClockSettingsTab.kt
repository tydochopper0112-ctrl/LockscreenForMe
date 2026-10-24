package com.fold.lockscreen.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fold.lockscreen.data.ClockStyle
import com.fold.lockscreen.data.FontStyle
import com.fold.lockscreen.data.LockscreenSettings
import com.fold.lockscreen.ui.components.ColorPickerRow
import com.fold.lockscreen.ui.theme.CyanAccent
import com.fold.lockscreen.ui.theme.DarkSurface
import com.fold.lockscreen.ui.theme.DarkSurfaceVariant
import com.fold.lockscreen.ui.theme.PureWhite

@Composable
fun ClockSettingsTab(
    settings: LockscreenSettings,
    onClockStyleChanged: (ClockStyle) -> Unit,
    onFontStyleChanged: (FontStyle) -> Unit,
    onClockSizeChanged: (Float) -> Unit,
    onClockColorChanged: (Long) -> Unit,
    onAccentColorChanged: (Long) -> Unit,
    onBackgroundColorChanged: (Long) -> Unit,
    onTimeFormatChanged: (is24Hour: Boolean, showSeconds: Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        // Section: Clock Style Selection (4 types)
        Text(
            text = "Kiểu đồng hồ Canvas (4 loại)",
            style = MaterialTheme.typography.titleMedium,
            color = CyanAccent,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ClockStyle.values().take(2).forEach { style ->
                ClockStyleCard(
                    style = style,
                    isSelected = settings.clockStyle == style,
                    onClick = { onClockStyleChanged(style) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ClockStyle.values().drop(2).forEach { style ->
                ClockStyleCard(
                    style = style,
                    isSelected = settings.clockStyle == style,
                    onClick = { onClockStyleChanged(style) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Font Selection (4 styles)
        Text(
            text = "Font chữ hiển thị (4 loại)",
            style = MaterialTheme.typography.titleMedium,
            color = CyanAccent,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FontStyle.values().forEach { font ->
                val isSelected = settings.fontStyle == font
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
                        .clickable { onFontStyleChanged(font) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = font.displayName,
                        color = PureWhite,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: Clock Size Slider
        Text(
            text = "Kích thước đồng hồ: ${(settings.clockSizeScale * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            color = CyanAccent,
            fontWeight = FontWeight.Bold
        )
        Slider(
            value = settings.clockSizeScale,
            onValueChange = onClockSizeChanged,
            valueRange = 0.6f..1.8f,
            colors = SliderDefaults.colors(
                thumbColor = CyanAccent,
                activeTrackColor = CyanAccent,
                inactiveTrackColor = DarkSurfaceVariant
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Section: Format Options
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Định dạng 24 giờ (24h format)", color = PureWhite, fontSize = 14.sp)
            Switch(
                checked = settings.is24Hour,
                onCheckedChange = { onTimeFormatChanged(it, settings.showSeconds) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = CyanAccent
                )
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Hiển thị kim giây / số giây", color = PureWhite, fontSize = 14.sp)
            Switch(
                checked = settings.showSeconds,
                onCheckedChange = { onTimeFormatChanged(settings.is24Hour, it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = CyanAccent
                )
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Section: Color Customization
        ColorPickerRow(
            title = "Màu số đồng hồ / Kim giờ",
            selectedColor = settings.clockColor,
            onColorSelected = onClockColorChanged
        )

        Spacer(modifier = Modifier.height(16.dp))

        ColorPickerRow(
            title = "Màu điểm nhấn (Accent / Ngày / Giây)",
            selectedColor = settings.accentColor,
            onColorSelected = onAccentColorChanged
        )

        Spacer(modifier = Modifier.height(16.dp))

        ColorPickerRow(
            title = "Màu nền Canvas (Mặc định đen AMOLED #000000)",
            selectedColor = settings.backgroundColor,
            onColorSelected = onBackgroundColorChanged
        )

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun ClockStyleCard(
    style: ClockStyle,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(72.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkSurfaceVariant else DarkSurface
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isSelected) CyanAccent else Color(0x30FFFFFF)
            )
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = style.displayName,
                color = if (isSelected) CyanAccent else PureWhite,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
