package com.fold.lockscreen.canvas

import android.graphics.*
import com.fold.lockscreen.data.LockscreenSettings
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FlipClockRenderer : ClockRenderer {

    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#1C1C1E")
    }

    private val cardTopHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(20, 255, 255, 255)
    }

    private val cardBottomShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.argb(40, 0, 0, 0)
    }

    private val seamPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.parseColor("#0A0A0A")
        strokeWidth = 3f
    }

    private val seamHighlightPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.argb(40, 255, 255, 255)
        strokeWidth = 1.5f
    }

    private val hingePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#3A3A3C")
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val colonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        letterSpacing = 0.08f
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
        val cardW = (minDim * 0.28f) * scale
        val cardH = (minDim * 0.32f) * scale
        val cardCorner = cardW * 0.12f
        val cardSpacing = cardW * 0.25f

        val typeface = ClockRenderer.resolveTypeface(settings.fontStyle)

        val hourPattern = if (settings.is24Hour) "HH" else "hh"
        val hourStr = SimpleDateFormat(hourPattern, Locale.getDefault()).format(calendar.time)
        val minStr = SimpleDateFormat("mm", Locale.getDefault()).format(calendar.time)

        val leftCardLeft = centerX - cardW - (cardSpacing / 2f)
        val leftCardTop = centerY - (cardH / 2f)

        val rightCardLeft = centerX + (cardSpacing / 2f)
        val rightCardTop = centerY - (cardH / 2f)

        // Draw Hour Card
        drawSingleFlipCard(
            canvas = canvas,
            left = leftCardLeft,
            top = leftCardTop,
            width = cardW,
            height = cardH,
            corner = cardCorner,
            text = hourStr,
            textColor = settings.clockColor.toInt(),
            typeface = typeface
        )

        // Draw Minute Card
        drawSingleFlipCard(
            canvas = canvas,
            left = rightCardLeft,
            top = rightCardTop,
            width = cardW,
            height = cardH,
            corner = cardCorner,
            text = minStr,
            textColor = settings.clockColor.toInt(),
            typeface = typeface
        )

        // Draw Separator Dots
        colonPaint.color = settings.accentColor.toInt()
        val colonRadius = (minDim * 0.012f) * scale
        canvas.drawCircle(centerX, centerY - (cardH * 0.18f), colonRadius, colonPaint)
        canvas.drawCircle(centerX, centerY + (cardH * 0.18f), colonRadius, colonPaint)

        // Draw Date Underneath
        val dateFormat = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault())
        val dateStr = dateFormat.format(calendar.time).uppercase()
        datePaint.apply {
            color = settings.accentColor.toInt()
            textSize = (minDim * 0.038f) * scale
            this.typeface = typeface
        }
        val dateY = centerY + (cardH / 2f) + (minDim * 0.07f)
        canvas.drawText(dateStr, centerX, dateY, datePaint)
    }

    private fun drawSingleFlipCard(
        canvas: Canvas,
        left: Float,
        top: Float,
        width: Float,
        height: Float,
        corner: Float,
        text: String,
        textColor: Int,
        typeface: Typeface
    ) {
        val rect = RectF(left, top, left + width, top + height)
        val midY = top + (height / 2f)

        // Card base
        canvas.drawRoundRect(rect, corner, corner, cardPaint)

        // Top half highlight
        canvas.save()
        canvas.clipRect(left, top, left + width, midY)
        canvas.drawRoundRect(rect, corner, corner, cardTopHighlightPaint)
        canvas.restore()

        // Bottom half shadow
        canvas.save()
        canvas.clipRect(left, midY, left + width, top + height)
        canvas.drawRoundRect(rect, corner, corner, cardBottomShadowPaint)
        canvas.restore()

        // Text
        textPaint.apply {
            color = textColor
            textSize = height * 0.65f
            this.typeface = Typeface.create(typeface, Typeface.BOLD)
        }
        val textX = left + (width / 2f)
        val textY = midY + (textPaint.textSize * 0.35f)
        canvas.drawText(text, textX, textY, textPaint)

        // Split seam groove
        canvas.drawLine(left, midY - 1f, left + width, midY - 1f, seamPaint)
        canvas.drawLine(left, midY + 1.5f, left + width, midY + 1.5f, seamHighlightPaint)

        // Hinges on left and right edge
        val hingeW = width * 0.04f
        val hingeH = height * 0.10f
        canvas.drawRoundRect(
            RectF(left - (hingeW / 2f), midY - (hingeH / 2f), left + (hingeW / 2f), midY + (hingeH / 2f)),
            2f, 2f, hingePaint
        )
        canvas.drawRoundRect(
            RectF(left + width - (hingeW / 2f), midY - (hingeH / 2f), left + width + (hingeW / 2f), midY + (hingeH / 2f)),
            2f, 2f, hingePaint
        )
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
        val cardW = (minDim * 0.28f) * scale
        val cardH = (minDim * 0.32f) * scale
        val cardSpacing = cardW * 0.25f
        val totalW = (cardW * 2f) + cardSpacing + 40f

        return RectF(
            centerX - (totalW / 2f),
            centerY - (cardH / 2f) - 20f,
            centerX + (totalW / 2f),
            centerY + (cardH / 2f) + (minDim * 0.08f)
        )
    }
}
