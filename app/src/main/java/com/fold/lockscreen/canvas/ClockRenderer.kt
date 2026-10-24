package com.fold.lockscreen.canvas

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Typeface
import com.fold.lockscreen.data.FontStyle
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
        calendar: Calendar
    )

    fun getBounds(
        width: Int,
        height: Int,
        centerX: Float,
        centerY: Float,
        settings: LockscreenSettings
    ): RectF

    companion object {
        fun resolveTypeface(fontStyle: FontStyle): Typeface {
            return when (fontStyle) {
                FontStyle.MONOSPACE -> Typeface.MONOSPACE
                FontStyle.SANS_SERIF -> Typeface.SANS_SERIF
                FontStyle.SERIF -> Typeface.SERIF
                FontStyle.DOT_MATRIX -> Typeface.MONOSPACE // Dot-Matrix clock uses its own dot generator
            }
        }
    }
}
