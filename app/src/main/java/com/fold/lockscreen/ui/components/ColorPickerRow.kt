package com.fold.lockscreen.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fold.lockscreen.ui.theme.*

val PRESET_COLORS = listOf(
    0xFFFFFFFF to "Trắng tinh khiết",
    0xFF00E5FF to "Xanh Cyan Neon",
    0xFF00E676 to "Xanh Lục Emerald",
    0xFFFFAB00 to "Vàng Cam Amber",
    0xFFFF1744 to "Đỏ Crimson",
    0xFFD500F9 to "Tím Neon",
    0xFF2979FF to "Xanh Điện Electric",
    0xFFFFD600 to "Vàng Chanh",
    0xFFB0BEC5 to "Xám Bạc Titanium"
)

@Composable
fun ColorPickerRow(
    title: String,
    selectedColor: Long,
    onColorSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = PureWhite,
            fontSize = 14.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(PRESET_COLORS) { (colorLong, _) ->
                val isSelected = selectedColor == colorLong
                val composeColor = Color(colorLong)

                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(composeColor)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) CyanAccent else Color(0x40FFFFFF),
                            shape = CircleShape
                        )
                        .clickable { onColorSelected(colorLong) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = if (colorLong == 0xFFFFFFFFL || colorLong == 0xFFFFD600L) Color.Black else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
