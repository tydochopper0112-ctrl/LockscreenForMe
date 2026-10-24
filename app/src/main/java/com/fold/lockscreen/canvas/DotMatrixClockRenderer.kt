package com.fold.lockscreen.canvas

import android.graphics.*
import com.fold.lockscreen.data.LockscreenSettings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DotMatrixClockRenderer : ClockRenderer {

    private val litPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val unlitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(40, 255, 255, 255)
    }

    private val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.12f
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    override fun draw(
        canvas: Canvas,
        width: Int,
        height: Int,
        centerX: Float,
        centerY: Float,
        settings: LockscreenSettings,
        calendar: Calendar
    ) {
        val minDim = width.coerceAtMost(height)
        val scale = settings.clockSizeScale
        val dotSize = (minDim * 0.016f) * scale
        val dotSpacing = dotSize * 0.35f
        val charSpacing = dotSize * 1.2f

        litPaint.color = settings.clockColor.toInt()
        glowPaint.color = Color.argb(
            60,
            Color.red(settings.clockColor.toInt()),
            Color.green(settings.clockColor.toInt()),
            Color.blue(settings.clockColor.toInt())
        )

        val timePattern = if (settings.is24Hour) {
            if (settings.showSeconds) "HH:mm:ss" else "HH:mm"
        } else {
            if (settings.showSeconds) "hh:mm:ss" else "hh:mm"
        }
        val timeFormat = SimpleDateFormat(timePattern, Locale.getDefault())
        val timeStr = timeFormat.format(calendar.time)

        val totalTextWidth = DotMatrixFont.measureWidth(timeStr, dotSize, dotSpacing, charSpacing)
        val totalTextHeight = DotMatrixFont.measureHeight(dotSize, dotSpacing)

        val startX = centerX - (totalTextWidth / 2f)
        val startY = centerY - (totalTextHeight / 2f)

        // Draw Dot-Matrix Clock
        DotMatrixFont.drawText(
            canvas = canvas,
            text = timeStr,
            startX = startX,
            startY = startY,
            dotSize = dotSize,
            dotSpacing = dotSpacing,
            charSpacing = charSpacing,
            litPaint = litPaint,
            unlitPaint = unlitPaint
        )

        // Draw Date in Dot-Matrix or Monospace style
        val dateFormat = SimpleDateFormat("EEE, dd-MM-yyyy", Locale.getDefault())
        val dateStr = dateFormat.format(calendar.time).uppercase()

        datePaint.apply {
            textSize = (minDim * 0.038f) * scale
            color = settings.accentColor.toInt()
            typeface = Typeface.MONOSPACE
        }

        val dateY = startY + totalTextHeight + (minDim * 0.06f)
        canvas.drawText(dateStr, centerX, dateY, datePaint)
    }

    override fun getBounds(
        width: Int,
        height: Int,
        centerX: Float,
        centerY: Float,
        settings: LockscreenSettings
    ): RectF {
        val minDim = width.coerceAtMost(height)
        val scale = settings.clockSizeScale
        val dotSize = (minDim * 0.016f) * scale
        val dotSpacing = dotSize * 0.35f
        val charSpacing = dotSize * 1.2f
        val sampleText = if (settings.showSeconds) "88:88:88" else "88:88"
        val totalW = DotMatrixFont.measureWidth(sampleText, dotSize, dotSpacing, charSpacing)
        val totalH = DotMatrixFont.measureHeight(dotSize, dotSpacing) + (minDim * 0.08f)

        return RectF(
            centerX - (totalW / 2f) - 20f,
            centerY - (totalH / 2f) - 20f,
            centerX + (totalW / 2f) + 20f,
            centerY + (totalH / 2f) + 30f
        )
    }
}
