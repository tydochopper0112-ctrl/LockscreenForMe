package com.fold.lockscreen.ui.components

import android.content.Context
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.fold.lockscreen.canvas.*
import com.fold.lockscreen.data.*
import com.fold.lockscreen.ui.theme.CyanAccent
import com.fold.lockscreen.ui.theme.DarkSurfaceVariant
import java.util.Calendar

class LockscreenPreviewView(context: Context) : View(context) {

    var settings: LockscreenSettings = LockscreenSettings()
        set(value) {
            field = value
            invalidate()
        }

    var isCoverScreen: Boolean = true
        set(value) {
            field = value
            invalidate()
        }

    var notifications: List<NotificationItem> = emptyList()
        set(value) {
            field = value
            invalidate()
        }

    private val digitalClock = DigitalClockRenderer()
    private val dotMatrixClock = DotMatrixClockRenderer()
    private val analogClock = AnalogClockRenderer()
    private val flipClock = FlipClockRenderer()
    private val notificationRenderer = NotificationRenderer()

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width
        val h = height
        if (w <= 0 || h <= 0) return

        // Background
        canvas.drawColor(settings.backgroundColor.toInt())

        val calendar = Calendar.getInstance()
        val density = resources.displayMetrics.density

        val layout = if (isCoverScreen) settings.coverLayout else settings.mainLayout

        val taskbarPaddingPx = if (!isCoverScreen) {
            layout.taskbarPaddingBottomDp * density * 0.4f // scaled for preview
        } else 0f

        val effectiveHeight = (h - taskbarPaddingPx).coerceAtLeast(50f)
        val clockCenterX = w * layout.clockXPercent
        val clockCenterY = effectiveHeight * layout.clockYPercent
        val notifCenterX = w * layout.notificationXPercent
        val notifCenterY = effectiveHeight * layout.notificationYPercent

        val renderer: ClockRenderer = when (settings.clockStyle) {
            ClockStyle.DIGITAL_MINIMAL -> digitalClock
            ClockStyle.DOT_MATRIX -> dotMatrixClock
            ClockStyle.ANALOG -> analogClock
            ClockStyle.FLIP_CLOCK -> flipClock
        }

        renderer.draw(
            canvas = canvas,
            width = w,
            height = effectiveHeight.toInt(),
            centerX = clockCenterX,
            centerY = clockCenterY,
            settings = settings,
            calendar = calendar
        )

        notificationRenderer.draw(
            canvas = canvas,
            width = w,
            height = effectiveHeight.toInt(),
            centerX = notifCenterX,
            centerY = notifCenterY,
            settings = settings,
            notifications = notifications
        )
    }
}

@Composable
fun PreviewCanvas(
    settings: LockscreenSettings,
    notifications: List<NotificationItem>,
    modifier: Modifier = Modifier
) {
    var previewIsCover by remember { mutableStateOf(true) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Toggle Fold Screen Mockup
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            SegmentedButton(
                selected = previewIsCover,
                onClick = { previewIsCover = true },
                shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = CyanAccent,
                    activeContentColor = Color.Black,
                    inactiveContainerColor = DarkSurfaceVariant,
                    inactiveContentColor = Color.White
                )
            ) {
                Text("Cover Screen (22:9)")
            }

            SegmentedButton(
                selected = !previewIsCover,
                onClick = { previewIsCover = false },
                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = CyanAccent,
                    activeContentColor = Color.Black,
                    inactiveContainerColor = DarkSurfaceVariant,
                    inactiveContentColor = Color.White
                )
            ) {
                Text("Main Screen (6:5)")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Device Frame Container
        val frameWidth = if (previewIsCover) 190.dp else 290.dp
        val frameHeight = 360.dp
        val cornerRadius = if (previewIsCover) 24.dp else 16.dp

        Box(
            modifier = Modifier
                .width(frameWidth)
                .height(frameHeight)
                .clip(RoundedCornerShape(cornerRadius))
                .background(Color(0xFF0F1014))
                .border(2.5.dp, Color(0xFF333842), RoundedCornerShape(cornerRadius))
                .padding(4.dp)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(cornerRadius - 4.dp)),
                factory = { ctx ->
                    LockscreenPreviewView(ctx).apply {
                        this.settings = settings
                        this.isCoverScreen = previewIsCover
                        this.notifications = notifications
                    }
                },
                update = { view ->
                    view.settings = settings
                    view.isCoverScreen = previewIsCover
                    view.notifications = notifications
                }
            )

            // Mockup Camera punch-hole
            Box(
                modifier = Modifier
                    .size(if (previewIsCover) 8.dp else 7.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(Color.Black)
                    .align(Alignment.TopCenter)
                    .offset(y = 6.dp)
            )
        }
    }
}
