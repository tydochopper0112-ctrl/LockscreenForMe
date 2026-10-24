package com.fold.lockscreen.canvas

import android.graphics.*
import com.fold.lockscreen.data.FontStyle
import com.fold.lockscreen.data.LockscreenSettings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DigitalClockRenderer : ClockRenderer {

    private val timePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.05f
    }

    private val secPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.LEFT
    }

    private val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.08f
    }

    private val boundsRect = RectF()

    override fun draw(
        canvas: Canvas,
        width: Int,
        height: Int,
        centerX: Float,
        centerY: Float,
        settings: LockscreenSettings,
        calendar: Calendar
    ) {
        val baseSize = (width.coerceAtMost(height) * 0.18f) * settings.clockSizeScale
        val typeface = ClockRenderer.resolveTypeface(settings.fontStyle)

        timePaint.apply {
            textSize = baseSize
            color = settings.clockColor.toInt()
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
        }

        secPaint.apply {
            textSize = baseSize * 0.35f
            color = settings.accentColor.toInt()
            this.typeface = typeface
        }

        datePaint.apply {
            textSize = baseSize * 0.22f
            color = settings.accentColor.toInt()
            this.typeface = typeface
        }

        val timePattern = if (settings.is24Hour) "HH:mm" else "hh:mm"
        val timeFormat = SimpleDateFormat(timePattern, Locale.getDefault())
        val timeStr = timeFormat.format(calendar.time)

        val secStr = if (settings.showSeconds) {
            String.format(Locale.getDefault(), ":%02d", calendar.get(Calendar.SECOND))
        } else ""

        val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
        val dateStr = dateFormat.format(calendar.time).uppercase()

        // Time measure
        val timeWidth = timePaint.measureText(timeStr)
        val secWidth = if (secStr.isNotEmpty()) secPaint.measureText(secStr) else 0f
        val totalTimeWidth = timeWidth + secWidth

        val timeX = if (secStr.isNotEmpty()) centerX - (secWidth / 2f) else centerX
        val timeY = centerY

        // Draw main time
        canvas.drawText(timeStr, timeX, timeY, timePaint)

        // Draw seconds if enabled
        if (secStr.isNotEmpty()) {
            val secX = timeX + (timeWidth / 2f) + 8f
            val secY = timeY - (baseSize * 0.35f)
            canvas.drawText(secStr, secX, secY, secPaint)
        }

        // Draw date underneath
        val dateY = timeY + (baseSize * 0.40f)
        canvas.drawText(dateStr, centerX, dateY, datePaint)

        // Cache bounds for double-tap
        val halfW = (totalTimeWidth / 2f).coerceAtLeast(width * 0.25f)
        boundsRect.set(
            centerX - halfW - 20f,
            timeY - baseSize - 10f,
            centerX + halfW + 20f,
            dateY + 20f
        )
    }

    override fun getBounds(
        width: Int,
        height: Int,
        centerX: Float,
        centerY: Float,
        settings: LockscreenSettings
    ): RectF {
        val baseSize = (width.coerceAtMost(height) * 0.18f) * settings.clockSizeScale
        val halfW = width * 0.35f * settings.clockSizeScale
        return RectF(
            centerX - halfW,
            centerY - baseSize - 10f,
            centerX + halfW,
            centerY + (baseSize * 0.5f)
        )
    }
}
