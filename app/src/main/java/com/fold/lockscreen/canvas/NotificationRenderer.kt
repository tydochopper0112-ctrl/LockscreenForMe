package com.fold.lockscreen.canvas

import android.graphics.*
import com.fold.lockscreen.data.LockscreenSettings
import com.fold.lockscreen.data.NotificationDisplayMode
import com.fold.lockscreen.data.NotificationItem

class NotificationRenderer {

    private val cardBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#1C1C1E")
        alpha = 215
    }

    private val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.8f
        color = Color.argb(60, 255, 255, 255)
    }

    private val appBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FF1122") // Accent Red to harmonize with "Live It"
    }

    private val appNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        color = Color.WHITE
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.SANS_SERIF
        color = Color.WHITE
    }

    private val counterTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textAlign = Paint.Align.LEFT
        color = Color.WHITE
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#FF1122")
    }

    private val boundsRect = RectF()

    fun draw(
        canvas: Canvas,
        width: Int,
        height: Int,
        centerX: Float,
        centerY: Float,
        settings: LockscreenSettings,
        notifications: List<NotificationItem>,
        scaleMultiplier: Float = 1.0f
    ) {
        boundsRect.setEmpty()
        if (settings.notificationMode == NotificationDisplayMode.HIDDEN) {
            return
        }

        val cardWidth = ((width * 0.82f).coerceAtMost(560f)) * scaleMultiplier

        when (settings.notificationMode) {
            NotificationDisplayMode.COUNTER -> {
                drawCounterBadge(canvas, centerX, centerY, cardWidth, notifications.size, scaleMultiplier)
            }
            NotificationDisplayMode.COMPACT_LIST -> {
                drawCompactList(canvas, centerX, centerY, cardWidth, notifications.take(3), scaleMultiplier)
            }
            else -> Unit
        }
    }

    private fun drawCounterBadge(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        maxWidth: Float,
        count: Int,
        scale: Float
    ) {
        val pillHeight = 44f * scale
        val pillWidth = 240f * scale
        val left = centerX - (pillWidth / 2f)
        val top = centerY - (pillHeight / 2f)
        val right = left + pillWidth
        val bottom = top + pillHeight
        val rect = RectF(left, top, right, bottom)

        canvas.drawRoundRect(rect, pillHeight / 2f, pillHeight / 2f, cardBackgroundPaint)
        cardBorderPaint.color = Color.argb(90, 255, 17, 34)
        canvas.drawRoundRect(rect, pillHeight / 2f, pillHeight / 2f, cardBorderPaint)

        // Accent indicator dot
        canvas.drawCircle(left + (24f * scale), centerY, 6f * scale, dotPaint)

        // Text
        val label = if (count > 0) "$count thông báo mới" else "Không có thông báo"
        counterTextPaint.textSize = 15f * scale
        canvas.drawText(label, left + (40f * scale), centerY + (5.5f * scale), counterTextPaint)

        boundsRect.set(rect)
    }

    private fun drawCompactList(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        cardWidth: Float,
        items: List<NotificationItem>,
        scale: Float
    ) {
        if (items.isEmpty()) {
            drawCounterBadge(canvas, centerX, centerY, cardWidth, 0, scale)
            return
        }

        val rowHeight = 48f * scale
        val rowSpacing = 8f * scale
        val totalHeight = (items.size * rowHeight) + ((items.size - 1) * rowSpacing)
        var startY = centerY - (totalHeight / 2f)
        val left = centerX - (cardWidth / 2f)
        val right = left + cardWidth

        boundsRect.set(left - 10f, startY - 10f, right + 10f, startY + totalHeight + 10f)

        for (item in items) {
            val rowRect = RectF(left, startY, right, startY + rowHeight)

            // Draw Card background
            canvas.drawRoundRect(rowRect, 14f * scale, 14f * scale, cardBackgroundPaint)
            cardBorderPaint.color = Color.argb(40, 255, 255, 255)
            canvas.drawRoundRect(rowRect, 14f * scale, 14f * scale, cardBorderPaint)

            // App badge
            val appLabel = if (item.appName.isNotBlank()) item.appName else item.packageName.substringAfterLast('.')
            appNamePaint.textSize = 12f * scale
            val badgeTextW = appNamePaint.measureText(appLabel)
            val badgePadH = 10f * scale
            val badgeW = badgeTextW + (badgePadH * 2f)
            val badgeH = 22f * scale
            val badgeLeft = left + (14f * scale)
            val badgeTop = startY + (rowHeight - badgeH) / 2f
            val badgeRect = RectF(badgeLeft, badgeTop, badgeLeft + badgeW, badgeTop + badgeH)

            canvas.drawRoundRect(badgeRect, 6f * scale, 6f * scale, appBadgePaint)
            canvas.drawText(appLabel, badgeLeft + badgePadH, badgeTop + (15.5f * scale), appNamePaint)

            // Title & Content
            val titleText = if (item.title.isNotBlank()) item.title else item.text
            val displaySnippet = if (item.text.isNotBlank() && item.title.isNotBlank()) {
                "${item.title} • ${item.text}"
            } else titleText

            titlePaint.textSize = 13.5f * scale
            val textStartX = badgeLeft + badgeW + (12f * scale)
            val maxTextW = right - textStartX - (16f * scale)
            val ellipsized = truncateText(displaySnippet, titlePaint, maxTextW)

            canvas.drawText(ellipsized, textStartX, startY + (rowHeight / 2f) + (4.5f * scale), titlePaint)

            startY += rowHeight + rowSpacing
        }
    }

    private fun truncateText(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        var end = text.length
        while (end > 0 && paint.measureText(text.substring(0, end) + "…") > maxWidth) {
            end--
        }
        return if (end > 0) text.substring(0, end) + "…" else ""
    }

    fun getBounds(): RectF = boundsRect
}
