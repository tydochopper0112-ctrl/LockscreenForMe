package com.fold.lockscreen.ui.components

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.fold.lockscreen.canvas.LifeClockRenderer
import com.fold.lockscreen.canvas.NotificationRenderer
import com.fold.lockscreen.data.*
import com.fold.lockscreen.ui.theme.CyanAccent
import com.fold.lockscreen.ui.theme.DarkSurface
import com.fold.lockscreen.ui.theme.DarkSurfaceVariant
import com.fold.lockscreen.ui.theme.PureWhite
import java.util.Calendar
import kotlin.math.abs

enum class DragTarget {
    NONE, CLOCK, NOTIFICATION
}

class WysiwygInteractiveView(context: Context) : View(context) {

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

    var onClockChanged: ((x: Float, y: Float, scale: Float) -> Unit)? = null
    var onNotificationChanged: ((x: Float, y: Float) -> Unit)? = null

    private val lifeClock = LifeClockRenderer()
    private val notificationRenderer = NotificationRenderer()

    private var activeTarget = DragTarget.NONE
    private var lastTouchX = 0f
    private var lastTouchY = 0f

    private var currentClockX = 0.5f
    private var currentClockY = 0.35f
    private var currentClockScale = 1.0f
    private var currentNotifX = 0.5f
    private var currentNotifY = 0.65f

    private var isSnappedX = false
    private var isSnappedY = false

    private val guidelinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.CYAN
        style = Paint.Style.STROKE
        strokeWidth = 2.5f
        pathEffect = DashPathEffect(floatArrayOf(12f, 8f), 0f)
    }

    private val snapTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.CYAN
        textSize = 24f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val selectionBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(140, 0, 229, 255)
        style = Paint.Style.STROKE
        strokeWidth = 2f
        pathEffect = DashPathEffect(floatArrayOf(8f, 6f), 0f)
    }

    private val taskbarGuidePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.argb(80, 255, 255, 255)
        style = Paint.Style.FILL
    }

    private val scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            if (activeTarget == DragTarget.CLOCK) {
                currentClockScale = (currentClockScale * detector.scaleFactor).coerceIn(0.5f, 2.2f)
                invalidate()
                return true
            }
            return false
        }
    })

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0 || h <= 0) return

        val cfg = if (isCoverScreen) settings.coverLayout else settings.mainLayout

        // Sync local positions if not actively dragging
        if (activeTarget == DragTarget.NONE) {
            currentClockX = cfg.clockXPercent
            currentClockY = cfg.clockYPercent
            currentClockScale = cfg.clockScale
            currentNotifX = cfg.notificationXPercent
            currentNotifY = cfg.notificationYPercent
        }

        // 1. Draw Wallpaper with Center-Crop
        val wallpaperBitmap = WallpaperHelper.loadBitmap(context, settings.selectedWallpaperPath, maxOf(width, height))
        if (wallpaperBitmap != null && !wallpaperBitmap.isRecycled) {
            WallpaperHelper.drawCenterCrop(canvas, wallpaperBitmap, w, h)
        } else {
            canvas.drawColor(android.graphics.Color.BLACK)
        }

        val density = resources.displayMetrics.density
        val taskbarPx = if (!isCoverScreen) (cfg.taskbarPaddingBottomDp * density * 0.45f) else 0f
        val effectiveH = (h - taskbarPx).coerceAtLeast(100f)

        // Draw Taskbar safe-zone hint on Main screen
        if (!isCoverScreen && taskbarPx > 0) {
            canvas.drawRect(0f, h - taskbarPx, w, h, taskbarGuidePaint)
        }

        // 2. Draw Snap Guidelines if active
        if (isSnappedX) {
            val midX = w * 0.5f
            canvas.drawLine(midX, 0f, midX, h, guidelinePaint)
            canvas.drawText("TÂM DỌC 50%", midX, 36f, snapTextPaint)
        }
        if (isSnappedY) {
            val midY = effectiveH * 0.5f
            canvas.drawLine(0f, midY, w, midY, guidelinePaint)
            canvas.drawText("TÂM NGANG 50%", w * 0.5f, midY - 10f, snapTextPaint)
        }

        val clockCx = w * currentClockX
        val clockCy = effectiveH * currentClockY

        val notifCx = w * currentNotifX
        val notifCy = effectiveH * currentNotifY

        val calendar = Calendar.getInstance()

        // 3. Render Clock
        lifeClock.draw(
            canvas = canvas,
            width = width,
            height = effectiveH.toInt(),
            centerX = clockCx,
            centerY = clockCy,
            settings = settings,
            calendar = calendar,
            scaleMultiplier = currentClockScale
        )

        val clockBounds = lifeClock.getBounds(
            width = width,
            height = effectiveH.toInt(),
            centerX = clockCx,
            centerY = clockCy,
            settings = settings,
            scaleMultiplier = currentClockScale
        )

        // Draw selection box around clock when dragging
        if (activeTarget == DragTarget.CLOCK) {
            canvas.drawRoundRect(clockBounds, 12f, 12f, selectionBoxPaint)
        }

        // 4. Render Notifications
        notificationRenderer.draw(
            canvas = canvas,
            width = width,
            height = effectiveH.toInt(),
            centerX = notifCx,
            centerY = notifCy,
            settings = settings,
            notifications = notifications,
            scaleMultiplier = 0.95f
        )

        val notifBounds = notificationRenderer.getBounds()
        if (activeTarget == DragTarget.NOTIFICATION) {
            canvas.drawRoundRect(notifBounds, 12f, 12f, selectionBoxPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        val w = width.toFloat()
        val h = height.toFloat()
        val density = resources.displayMetrics.density
        val cfg = if (isCoverScreen) settings.coverLayout else settings.mainLayout
        val taskbarPx = if (!isCoverScreen) (cfg.taskbarPaddingBottomDp * density * 0.45f) else 0f
        val effectiveH = (h - taskbarPx).coerceAtLeast(100f)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y

                val clockBounds = lifeClock.getBounds(
                    width = width,
                    height = effectiveH.toInt(),
                    centerX = w * currentClockX,
                    centerY = effectiveH * currentClockY,
                    settings = settings,
                    scaleMultiplier = currentClockScale
                )

                val notifBounds = notificationRenderer.getBounds()

                // Hit test with padding for easy finger grab
                val paddedClock = RectF(clockBounds).apply { inset(-24f, -24f) }
                val paddedNotif = RectF(notifBounds).apply { inset(-24f, -24f) }

                if (paddedClock.contains(event.x, event.y)) {
                    activeTarget = DragTarget.CLOCK
                } else if (paddedNotif.contains(event.x, event.y)) {
                    activeTarget = DragTarget.NOTIFICATION
                } else {
                    // Grab whichever is closest
                    val distClock = abs(event.y - (effectiveH * currentClockY))
                    val distNotif = abs(event.y - (effectiveH * currentNotifY))
                    activeTarget = if (distClock < distNotif) DragTarget.CLOCK else DragTarget.NOTIFICATION
                }
                parent?.requestDisallowInterceptTouchEvent(true)
                invalidate()
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    lastTouchX = event.x
                    lastTouchY = event.y

                    if (activeTarget == DragTarget.CLOCK) {
                        var newX = currentClockX + (dx / w)
                        var newY = currentClockY + (dy / effectiveH)

                        // Snap-to-center logic
                        isSnappedX = abs(newX - 0.5f) < 0.045f
                        if (isSnappedX) newX = 0.5f

                        isSnappedY = abs(newY - 0.5f) < 0.035f
                        if (isSnappedY) newY = 0.5f

                        currentClockX = newX.coerceIn(0.1f, 0.9f)
                        currentClockY = newY.coerceIn(0.12f, 0.88f)
                    } else if (activeTarget == DragTarget.NOTIFICATION) {
                        var newX = currentNotifX + (dx / w)
                        var newY = currentNotifY + (dy / effectiveH)

                        isSnappedX = abs(newX - 0.5f) < 0.045f
                        if (isSnappedX) newX = 0.5f

                        currentNotifX = newX.coerceIn(0.1f, 0.9f)
                        currentNotifY = newY.coerceIn(0.15f, 0.90f)
                    }
                    invalidate()
                }
                return true
            }

            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isSnappedX = false
                isSnappedY = false
                if (activeTarget == DragTarget.CLOCK) {
                    onClockChanged?.invoke(currentClockX, currentClockY, currentClockScale)
                } else if (activeTarget == DragTarget.NOTIFICATION) {
                    onNotificationChanged?.invoke(currentNotifX, currentNotifY)
                }
                activeTarget = DragTarget.NONE
                parent?.requestDisallowInterceptTouchEvent(false)
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun resetToCenter() {
        if (isCoverScreen) {
            currentClockX = 0.5f
            currentClockY = 0.32f
            currentClockScale = 1.0f
            currentNotifX = 0.5f
            currentNotifY = 0.62f
        } else {
            currentClockX = 0.5f
            currentClockY = 0.36f
            currentClockScale = 1.15f
            currentNotifX = 0.5f
            currentNotifY = 0.64f
        }
        onClockChanged?.invoke(currentClockX, currentClockY, currentClockScale)
        onNotificationChanged?.invoke(currentNotifX, currentNotifY)
        invalidate()
    }
}

@Composable
fun InteractiveWysiwygPreview(
    settings: LockscreenSettings,
    isCoverScreen: Boolean,
    notifications: List<NotificationItem>,
    onScreenModeChanged: (Boolean) -> Unit,
    onClockLayoutChanged: (x: Float, y: Float, scale: Float) -> Unit,
    onNotificationLayoutChanged: (x: Float, y: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var previewViewRef by remember { mutableStateOf<WysiwygInteractiveView?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Fold Screen Mode Switcher
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            SegmentedButton(
                selected = isCoverScreen,
                onClick = { onScreenModeChanged(true) },
                shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = CyanAccent,
                    activeContentColor = Color.Black,
                    inactiveContainerColor = DarkSurfaceVariant,
                    inactiveContentColor = PureWhite
                )
            ) {
                Text("Màn ngoài (Cover 22:9)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            SegmentedButton(
                selected = !isCoverScreen,
                onClick = { onScreenModeChanged(false) },
                shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = CyanAccent,
                    activeContentColor = Color.Black,
                    inactiveContainerColor = DarkSurfaceVariant,
                    inactiveContentColor = PureWhite
                )
            ) {
                Text("Màn trong (Main 6:5)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        // Gesture Guidance Pill
        Row(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0x3300E5FF))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "👆 Chạm & Kéo vị trí • 🤏 2 ngón tay chỉnh to/nhỏ • 🧲 Tự hít vào tâm",
                color = CyanAccent,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Device Frame Container
        val frameWidth = if (isCoverScreen) 195.dp else 305.dp
        val frameHeight = 390.dp
        val cornerRadius = if (isCoverScreen) 26.dp else 18.dp

        Box(
            modifier = Modifier
                .width(frameWidth)
                .height(frameHeight)
                .clip(RoundedCornerShape(cornerRadius))
                .background(Color(0xFF0F1014))
                .border(2.5.dp, Color(0xFF383D48), RoundedCornerShape(cornerRadius))
                .padding(4.dp)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(cornerRadius - 4.dp)),
                factory = { ctx ->
                    WysiwygInteractiveView(ctx).apply {
                        this.settings = settings
                        this.isCoverScreen = isCoverScreen
                        this.notifications = notifications
                        this.onClockChanged = onClockLayoutChanged
                        this.onNotificationChanged = onNotificationLayoutChanged
                        previewViewRef = this
                    }
                },
                update = { view ->
                    view.settings = settings
                    view.isCoverScreen = isCoverScreen
                    view.notifications = notifications
                    view.onClockChanged = onClockLayoutChanged
                    view.onNotificationChanged = onNotificationLayoutChanged
                    previewViewRef = view
                }
            )

            // Front camera punch-hole
            Box(
                modifier = Modifier
                    .size(if (isCoverScreen) 8.dp else 7.dp)
                    .clip(CircleShape)
                    .background(Color.Black)
                    .align(Alignment.TopCenter)
                    .offset(y = 6.dp)
            )

            // Quick Reset Button at top-right
            IconButton(
                onClick = { previewViewRef?.resetToCenter() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0x80000000))
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = "Đặt lại vị trí",
                    tint = PureWhite,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}
