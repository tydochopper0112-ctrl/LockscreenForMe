package com.fold.lockscreen.canvas

import android.graphics.*
import com.fold.lockscreen.data.LockscreenSettings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

class AnalogClockRenderer : ClockRenderer {

    private val dialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val hourHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val minuteHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val secondHandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val centerCapPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.05f
    }

    private val numberPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
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
        val radius = (minDim * 0.32f) * settings.clockSizeScale
        val clockColor = settings.clockColor.toInt()
        val accentColor = settings.accentColor.toInt()
        val typeface = ClockRenderer.resolveTypeface(settings.fontStyle)

        // Outer rim
        dialPaint.apply {
            color = Color.argb(40, Color.red(clockColor), Color.green(clockColor), Color.blue(clockColor))
            strokeWidth = 2.5f
        }
        canvas.drawCircle(centerX, centerY, radius, dialPaint)

        // Draw hour and minute ticks
        tickPaint.color = clockColor
        for (i in 0 until 60) {
            val angle = Math.toRadians((i * 6).toDouble())
            val isMajor = i % 5 == 0
            val tickLen = if (isMajor) radius * 0.12f else radius * 0.05f
            tickPaint.strokeWidth = if (isMajor) 4f else 2f
            tickPaint.alpha = if (isMajor) 230 else 80

            val startR = radius - tickLen
            val x1 = (centerX + startR * sin(angle)).toFloat()
            val y1 = (centerY - startR * cos(angle)).toFloat()
            val x2 = (centerX + radius * sin(angle)).toFloat()
            val y2 = (centerY - radius * cos(angle)).toFloat()

            canvas.drawLine(x1, y1, x2, y2, tickPaint)
        }

        // Draw numerals for 12, 3, 6, 9
        numberPaint.apply {
            color = clockColor
            textSize = radius * 0.16f
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
        }
        val numRadius = radius * 0.72f
        val cardinalHours = listOf(12 to "12", 3 to "3", 6 to "6", 9 to "9")
        for ((hr, label) in cardinalHours) {
            val angle = Math.toRadians((hr * 30).toDouble())
            val nx = (centerX + numRadius * sin(angle)).toFloat()
            val ny = (centerY - numRadius * cos(angle) + (numberPaint.textSize / 3f)).toFloat()
            canvas.drawText(label, nx, ny, numberPaint)
        }

        // Calculate hand angles
        val hours = calendar.get(Calendar.HOUR)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)
        val millis = calendar.get(Calendar.MILLISECOND)

        val minuteFraction = minutes + (seconds / 60f)
        val hourFraction = hours + (minuteFraction / 60f)
        val secondFraction = if (settings.showSeconds) seconds + (millis / 1000f) else 0f

        val hourAngle = Math.toRadians((hourFraction * 30).toDouble())
        val minuteAngle = Math.toRadians((minuteFraction * 6).toDouble())
        val secondAngle = Math.toRadians((secondFraction * 6).toDouble())

        // Hour Hand
        val hourHandLength = radius * 0.50f
        hourHandPaint.apply {
            color = clockColor
            strokeWidth = radius * 0.045f
        }
        canvas.drawLine(
            centerX, centerY,
            (centerX + hourHandLength * sin(hourAngle)).toFloat(),
            (centerY - hourHandLength * cos(hourAngle)).toFloat(),
            hourHandPaint
        )

        // Minute Hand
        val minuteHandLength = radius * 0.75f
        minuteHandPaint.apply {
            color = clockColor
            strokeWidth = radius * 0.030f
        }
        canvas.drawLine(
            centerX, centerY,
            (centerX + minuteHandLength * sin(minuteAngle)).toFloat(),
            (centerY - minuteHandLength * cos(minuteAngle)).toFloat(),
            minuteHandPaint
        )

        // Second Hand
        if (settings.showSeconds) {
            val secondHandLength = radius * 0.85f
            val tailLength = radius * 0.18f
            secondHandPaint.apply {
                color = accentColor
                strokeWidth = radius * 0.015f
            }
            // Tail
            canvas.drawLine(
                (centerX - tailLength * sin(secondAngle)).toFloat(),
                (centerY + tailLength * cos(secondAngle)).toFloat(),
                (centerX + secondHandLength * sin(secondAngle)).toFloat(),
                (centerY - secondHandLength * cos(secondAngle)).toFloat(),
                secondHandPaint
            )
        }

        // Center Cap
        centerCapPaint.color = clockColor
        canvas.drawCircle(centerX, centerY, radius * 0.05f, centerCapPaint)
        centerCapPaint.color = accentColor
        canvas.drawCircle(centerX, centerY, radius * 0.025f, centerCapPaint)

        // Date Display
        val dateFormat = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
        val dateStr = dateFormat.format(calendar.time).uppercase()
        datePaint.apply {
            color = accentColor
            textSize = radius * 0.14f
            this.typeface = typeface
        }
        val dateY = centerY + (radius * 0.40f)
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
        val radius = (minDim * 0.32f) * settings.clockSizeScale
        return RectF(
            centerX - radius - 20f,
            centerY - radius - 20f,
            centerX + radius + 20f,
            centerY + radius + 20f
        )
    }
}
