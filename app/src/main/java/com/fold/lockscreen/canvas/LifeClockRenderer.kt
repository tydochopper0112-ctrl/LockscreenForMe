package com.fold.lockscreen.canvas

import android.graphics.*
import com.fold.lockscreen.data.LockscreenSettings
import java.util.Calendar
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom clock renderer faithful to "Life is to short Live It" clock style.
 * Features:
 * - Vertical stem of "L" passing through center pivot
 * - Elegant geometric "ife" lowercase typography
 * - Subtitle "is to short" in clean thin white sans-serif
 * - Action text "Live It" in bold vibrant red
 * - Analog clock hands telling real time with opposing counter-weights
 * - Characteristic long red second hand tail matching the reference artwork
 * - Black and white donut center cap with red core
 */
class LifeClockRenderer : ClockRenderer {

    private val stemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.BUTT
    }

    private val ifePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        typeface = Typeface.create("sans-serif-thin", Typeface.NORMAL)
        letterSpacing = 0.02f
    }

    private val isToShortPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
        letterSpacing = 0.06f
    }

    private val liveItPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF1122")
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        letterSpacing = 0.04f
    }

    private val hourHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val minuteHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val secondHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF1122")
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val capRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
    }

    private val capFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private val capCenterPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FF1122")
        style = Paint.Style.FILL
    }

    override fun draw(
        canvas: Canvas,
        width: Int,
        height: Int,
        centerX: Float,
        centerY: Float,
        settings: LockscreenSettings,
        calendar: Calendar,
        scaleMultiplier: Float
    ) {
        val minDim = width.coerceAtMost(height)
        val radius = (minDim * 0.28f) * scaleMultiplier

        // 1. Draw the vertical stem of "L"
        val stemTop = centerY - (radius * 1.35f)
        val stemBottom = centerY + (radius * 0.22f)
        val stemWidth = (radius * 0.075f).coerceAtLeast(3f)

        stemPaint.strokeWidth = stemWidth
        canvas.drawLine(centerX, stemBottom, centerX, stemTop, stemPaint)

        // 2. Draw "ife" to the right of the vertical stem
        ifePaint.textSize = radius * 0.94f
        val textStartX = centerX + (stemWidth / 2f) + (radius * 0.08f)
        val textBaselineY = centerY - (radius * 0.06f)
        canvas.drawText("ife", textStartX, textBaselineY, ifePaint)

        // 3. Draw text below center pivot
        isToShortPaint.textSize = radius * 0.21f
        val isToShortY = centerY + (radius * 0.68f)
        canvas.drawText("is to short", centerX, isToShortY, isToShortPaint)

        liveItPaint.textSize = radius * 0.28f
        val liveItY = centerY + (radius * 0.98f)
        canvas.drawText("Live It", centerX, liveItY, liveItPaint)

        // 4. Calculate real-time hand angles
        val hours = calendar.get(Calendar.HOUR)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)
        val millis = calendar.get(Calendar.MILLISECOND)

        val minuteFraction = minutes + (seconds / 60f)
        val hourFraction = hours + (minuteFraction / 60f)
        val secondFraction = if (settings.showSeconds) seconds + (millis / 1000f) else seconds.toFloat()

        val hourAngle = Math.toRadians((hourFraction * 30.0))
        val minuteAngle = Math.toRadians((minuteFraction * 6.0))
        val secondAngle = Math.toRadians((secondFraction * 6.0))

        // Hour Hand with counterweight
        val hourLen = radius * 0.62f
        val hourTail = radius * 0.16f
        hourHandPaint.strokeWidth = (radius * 0.052f).coerceAtLeast(3f)
        canvas.drawLine(
            (centerX - hourTail * sin(hourAngle)).toFloat(),
            (centerY + hourTail * cos(hourAngle)).toFloat(),
            (centerX + hourLen * sin(hourAngle)).toFloat(),
            (centerY - hourLen * cos(hourAngle)).toFloat(),
            hourHandPaint
        )

        // Minute Hand with counterweight
        val minuteLen = radius * 1.02f
        val minuteTail = radius * 0.20f
        minuteHandPaint.strokeWidth = (radius * 0.040f).coerceAtLeast(2.5f)
        canvas.drawLine(
            (centerX - minuteTail * sin(minuteAngle)).toFloat(),
            (centerY + minuteTail * cos(minuteAngle)).toFloat(),
            (centerX + minuteLen * sin(minuteAngle)).toFloat(),
            (centerY - minuteLen * cos(minuteAngle)).toFloat(),
            minuteHandPaint
        )

        // Second Hand with prominent red tail (matching the artwork's red diagonal aesthetic)
        if (settings.showSeconds) {
            val secondLen = radius * 0.82f
            val secondTail = radius * 0.50f
            secondHandPaint.strokeWidth = (radius * 0.022f).coerceAtLeast(1.8f)
            canvas.drawLine(
                (centerX - secondTail * sin(secondAngle)).toFloat(),
                (centerY + secondTail * cos(secondAngle)).toFloat(),
                (centerX + secondLen * sin(secondAngle)).toFloat(),
                (centerY - secondLen * cos(secondAngle)).toFloat(),
                secondHandPaint
            )
        }

        // 5. Center Cap: Black donut with white ring and red core
        val capRadius = radius * 0.11f
        capFillPaint.color = Color.BLACK
        canvas.drawCircle(centerX, centerY, capRadius, capFillPaint)

        capRingPaint.strokeWidth = (radius * 0.022f).coerceAtLeast(1.5f)
        canvas.drawCircle(centerX, centerY, capRadius, capRingPaint)

        val redPinRadius = radius * 0.032f
        canvas.drawCircle(centerX, centerY, redPinRadius, capCenterPaint)
    }

    override fun getBounds(
        width: Int,
        height: Int,
        centerX: Float,
        centerY: Float,
        settings: LockscreenSettings,
        scaleMultiplier: Float
    ): RectF {
        val minDim = width.coerceAtMost(height)
        val radius = (minDim * 0.28f) * scaleMultiplier

        return RectF(
            centerX - (radius * 1.15f),
            centerY - (radius * 1.42f),
            centerX + (radius * 1.25f),
            centerY + (radius * 1.15f)
        )
    }
}
