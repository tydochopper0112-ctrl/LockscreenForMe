package com.fold.lockscreen.service

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.RectF
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.service.wallpaper.WallpaperService
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.fold.lockscreen.canvas.LifeClockRenderer
import com.fold.lockscreen.canvas.NotificationRenderer
import com.fold.lockscreen.data.*
import kotlinx.coroutines.*
import java.util.Calendar

class LockscreenWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return LockscreenEngine()
    }

    inner class LockscreenEngine : Engine() {

        private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
        private var tickerJob: Job? = null

        private val preferencesManager by lazy { PreferencesManager(applicationContext) }
        private var currentSettings = LockscreenSettings()
        private var currentNotifications = listOf<NotificationItem>()

        private var surfaceWidth = 0
        private var surfaceHeight = 0
        private var isCoverScreen = true
        private var isVisible = false

        private val lifeClock = LifeClockRenderer()
        private val notificationRenderer = NotificationRenderer()

        private var clockBounds = RectF()
        private var notifBounds = RectF()

        private val gestureDetector by lazy {
            GestureDetector(applicationContext, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDoubleTap(e: MotionEvent): Boolean {
                    return handleDoubleTap(e.x, e.y)
                }

                override fun onDown(e: MotionEvent): Boolean = true
            })
        }

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)

            // Listen to settings changes in real-time via DataStore Flow
            serviceScope.launch {
                preferencesManager.settingsFlow.collect { settings ->
                    currentSettings = settings
                    drawFrame()
                }
            }

            // Listen to notification changes in real-time
            serviceScope.launch {
                NotificationRepository.notifications.collect { notifs ->
                    currentNotifications = notifs
                    drawFrame()
                }
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            isVisible = visible
            if (visible) {
                startTicker()
                drawFrame()
            } else {
                stopTicker()
            }
        }

        override fun onSurfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
            super.onSurfaceChanged(holder, format, width, height)
            surfaceWidth = width
            surfaceHeight = height

            // Aspect ratio check specialized for Samsung Galaxy Z Fold 7
            // width / height < 0.6 is Cover Screen (~22:9 aspect ratio)
            // width / height > 0.7 is Main Inner Screen (~6:5 aspect ratio)
            val ratio = if (height > 0) width.toFloat() / height.toFloat() else 0.5f
            isCoverScreen = ratio < 0.6f

            drawFrame()
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            isVisible = false
            stopTicker()
        }

        override fun onDestroy() {
            super.onDestroy()
            serviceScope.cancel()
        }

        override fun onTouchEvent(event: MotionEvent?) {
            super.onTouchEvent(event)
            event?.let {
                gestureDetector.onTouchEvent(it)
            }
        }

        private fun startTicker() {
            stopTicker()
            tickerJob = serviceScope.launch {
                while (isActive && isVisible) {
                    drawFrame()
                    val delayMs = if (currentSettings.showSeconds) 100L else 1000L
                    delay(delayMs)
                }
            }
        }

        private fun stopTicker() {
            tickerJob?.cancel()
            tickerJob = null
        }

        private fun drawFrame() {
            val holder = surfaceHolder ?: return
            if (!holder.surface.isValid) return

            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    renderOnCanvas(canvas, surfaceWidth, surfaceHeight)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                canvas?.let {
                    runCatching { holder.unlockCanvasAndPost(it) }
                }
            }
        }

        private fun renderOnCanvas(canvas: Canvas, width: Int, height: Int) {
            if (width <= 0 || height <= 0) return

            // 1. Draw Background Wallpaper with Center-Crop
            val wallpaperBitmap = WallpaperHelper.loadBitmap(
                applicationContext,
                currentSettings.selectedWallpaperPath,
                maxOf(width, height)
            )

            if (wallpaperBitmap != null && !wallpaperBitmap.isRecycled) {
                WallpaperHelper.drawCenterCrop(canvas, wallpaperBitmap, width.toFloat(), height.toFloat())
            } else {
                canvas.drawColor(Color.BLACK)
            }

            val calendar = Calendar.getInstance()
            val density = resources.displayMetrics.density

            // Determine active layout based on Foldable screen detection
            val layout = if (isCoverScreen) {
                currentSettings.coverLayout
            } else {
                currentSettings.mainLayout
            }

            // Reserve bottom space for One UI Fold taskbar on main screen
            val taskbarPaddingPx = if (!isCoverScreen) {
                layout.taskbarPaddingBottomDp * density
            } else 0f

            val effectiveHeight = (height - taskbarPaddingPx).coerceAtLeast(100f)

            val clockCenterX = width * layout.clockXPercent
            val clockCenterY = effectiveHeight * layout.clockYPercent

            val notifCenterX = width * layout.notificationXPercent
            val notifCenterY = effectiveHeight * layout.notificationYPercent

            // 2. Render Single "Life is to short Live It" Clock Style
            lifeClock.draw(
                canvas = canvas,
                width = width,
                height = effectiveHeight.toInt(),
                centerX = clockCenterX,
                centerY = clockCenterY,
                settings = currentSettings,
                calendar = calendar,
                scaleMultiplier = layout.clockScale
            )

            clockBounds = lifeClock.getBounds(
                width = width,
                height = effectiveHeight.toInt(),
                centerX = clockCenterX,
                centerY = clockCenterY,
                settings = currentSettings,
                scaleMultiplier = layout.clockScale
            )

            // 3. Render Notifications
            notificationRenderer.draw(
                canvas = canvas,
                width = width,
                height = effectiveHeight.toInt(),
                centerX = notifCenterX,
                centerY = notifCenterY,
                settings = currentSettings,
                notifications = currentNotifications,
                scaleMultiplier = 1.0f
            )

            notifBounds = notificationRenderer.getBounds()
        }

        private fun handleDoubleTap(x: Float, y: Float): Boolean {
            val hitClock = clockBounds.contains(x, y)
            val hitNotif = notifBounds.contains(x, y)

            if (hitClock || hitNotif) {
                triggerHapticFeedback()
                launchTargetApp(currentSettings.doubleTapPackage)
                return true
            }
            return false
        }

        private fun triggerHapticFeedback() {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                    vibratorManager?.defaultVibrator?.vibrate(
                        VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                    vibrator?.vibrate(50)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun launchTargetApp(packageName: String) {
            try {
                val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    startActivity(launchIntent)
                } else {
                    val fallbackIntent = Intent(Intent.ACTION_MAIN).apply {
                        addCategory(Intent.CATEGORY_APP_CALENDAR)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(fallbackIntent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
