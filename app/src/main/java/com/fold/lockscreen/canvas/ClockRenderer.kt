package com.fold.lockscreen.canvas

import android.graphics.Canvas
import android.graphics.RectF
import com.fold.lockscreen.data.LockscreenSettings
import java.util.Calendar

interface ClockRenderer {
    fun draw(
        canvas: Canvas,
        width: Int,
        height: Int,
        centerX: Float,
        centerY: Float,
        settings: LockscreenSettings,
        calendar: Calendar,
        scaleMultiplier: Float = 1.0f
    )

    fun getBounds(
        width: Int,
        height: Int,
        centerX: Float,
        centerY: Float,
        settings: LockscreenSettings,
        scaleMultiplier: Float = 1.0f
    ): RectF
}
