package com.fold.lockscreen.ui.components

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fold.lockscreen.data.WallpaperHelper
import com.fold.lockscreen.data.WallpaperItem
import com.fold.lockscreen.ui.theme.CyanAccent
import com.fold.lockscreen.ui.theme.DarkSurface
import com.fold.lockscreen.ui.theme.DarkSurfaceVariant
import com.fold.lockscreen.ui.theme.PureWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun WallpaperGalleryRow(
    selectedWallpaperPath: String,
    onWallpaperSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Media picker launcher for custom wallpapers
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val copiedPath = WallpaperHelper.copyUriToCustomWallpaper(context, uri)
            if (copiedPath != null) {
                onWallpaperSelected(copiedPath)
            }
        }
    }

    // Build wallpaper list including custom if active
    val wallpapers = remember(selectedWallpaperPath) {
        val list = WallpaperHelper.PRESET_WALLPAPERS.toMutableList()
        if (selectedWallpaperPath.startsWith("file:")) {
            list.add(
                0,
                WallpaperItem(
                    id = "custom_current",
                    title = "Ảnh từ máy",
                    path = selectedWallpaperPath,
                    isCustom = true
                )
            )
        }
        list
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Kho Ảnh Nền Z Fold 7",
                style = MaterialTheme.typography.titleMedium,
                color = CyanAccent,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tự động Center-Crop",
                color = Color(0xFF888888),
                fontSize = 11.5.sp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
        ) {
            // Button to Pick image from Gallery
            item {
                Card(
                    modifier = Modifier
                        .width(96.dp)
                        .height(130.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            try {
                                pickMediaLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(CyanAccent)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "Chọn ảnh từ máy",
                                tint = CyanAccent,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Chọn ảnh từ máy",
                            color = PureWhite,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            // Thumbnail cards for preset wallpapers
            items(wallpapers) { item ->
                val isSelected = selectedWallpaperPath == item.path
                WallpaperThumbnailCard(
                    item = item,
                    isSelected = isSelected,
                    onClick = { onWallpaperSelected(item.path) }
                )
            }
        }
    }
}

@Composable
private fun WallpaperThumbnailCard(
    item: WallpaperItem,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    var bitmap by remember(item.path) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(item.path) {
        withContext(Dispatchers.IO) {
            val bmp = WallpaperHelper.loadBitmap(context, item.path, targetMaxDim = 256)
            bitmap = bmp
        }
    }

    Card(
        modifier = Modifier
            .width(96.dp)
            .height(130.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (isSelected) CyanAccent else Color(0x30FFFFFF)
            )
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val currentBmp = bitmap
            if (currentBmp != null && !currentBmp.isRecycled) {
                try {
                    Image(
                        bitmap = currentBmp.asImageBitmap(),
                        contentDescription = item.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } catch (e: Exception) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                )
            }

            // Bottom gradient overlay for title
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color(0xCC000000))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = item.title,
                    color = PureWhite,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
            }

            // Selected Checkmark Badge
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Đã chọn",
                    tint = CyanAccent,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(20.dp)
                )
            }
        }
    }
}
