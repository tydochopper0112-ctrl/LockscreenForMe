package com.fold.lockscreen.canvas

import android.graphics.*
import com.fold.lockscreen.data.LockscreenSettings
import com.fold.lockscreen.data.NotificationDisplayMode
import com.fold.lockscreen.data.NotificationItem

class NotificationRenderer {

    private val cardBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#1C1C1E")
        alpha = 210
    }

    private val cardBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.8f
        color = Color.argb(60, 255, 255, 255)
    }

    private val appBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val appNamePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    private val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.SANS_SERIF
        color = Color.WHITE
    }

    private val counterTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }

    private val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val boundsRect = RectF()

    fun draw(
        canvas: Canvas,
        width: Int,
        height: Int,
        centerX: Float,
        centerY: Float,
        settings: LockscreenSettings,
        notifications: List<NotificationItem>
    ) {
        boundsRect.setEmpty()
        if (settings.notificationMode == NotificationDisplayMode.HIDDEN) {
            return
        }

        val accentColor = settings.accentColor.toInt()
        val cardWidth = (width * 0.82f).coerceAtMost(560f)

        when (settings.notificationMode) {
            NotificationDisplayMode.COUNTER -> {
                drawCounterBadge(canvas, centerX, centerY, cardWidth, accentColor, notifications.size)
            }
            NotificationDisplayMode.COMPACT_LIST -> {
                drawCompactList(canvas, centerX, centerY, cardWidth, accentColor, notifications.take(3))
            }
            else -> Unit
        }
    }

    private fun drawCounterBadge(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        maxWidth: Float,
        accentColor: Int,
        count: Int
    ) {
        val pillHeight = 44f
        val pillWidth = 240f
        val left = centerX - (pillWidth / 2f)
        val top = centerY - (pillHeight / 2f)
        val right = left + pillWidth
        val bottom = top + pillHeight
        val rect = RectF(left, top, right, bottom)

        canvas.drawRoundRect(rect, pillHeight / 2f, pillHeight / 2f, cardBackgroundPaint)
        cardBorderPaint.color = Color.argb(90, Color.red(accentColor), Color.green(accentColor), Color.blue(accentColor))
        canvas.drawRoundRect(rect, pillHeight / 2f, pillHeight / 2f, cardBorderPaint)

        // Accent indicator dot
        dotPaint.color = accentColor
        canvas.drawCircle(left + 24f, centerY, 6f, dotPaint)

        // Text
        val label = if (count > 0) "$count thông báo mới" else "Không có thông báo"
        counterTextPaint.apply {
            color = Color.WHITE
            textSize = 15f
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(label, left + 40f, centerY + 5.5f, counterTextPaint)

        boundsRect.set(rect)
    }

    private fun drawCompactList(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        cardWidth: Float,
        accentColor: Int,
        items: List<NotificationItem>
    ) {
        if (items.isEmpty()) {
            drawCounterBadge(canvas, centerX, centerY, cardWidth, accentColor, 0)
            return
        }

        val rowHeight = 48f
        val rowSpacing = 8f
        val totalHeight = (items.size * rowHeight) + ((items.size - 1) * rowSpacing)
        var startY = centerY - (totalHeight / 2f)
        val left = centerX - (cardWidth / 2f)
        val right = left + cardWidth

        boundsRect.set(left - 10f, startY - 10f, right + 10f, startY + totalHeight + 10f)

        for (item in items) {
            val rowRect = RectF(left, startY, right, startY + rowHeight)

            // Draw Card background
            canvas.drawRoundRect(rowRect, 14f, 14f, cardBackgroundPaint)
            cardBorderPaint.color = Color.argb(40, 255, 255, 255)
            canvas.drawRoundRect(rowRect, 14f, 14f, cardBorderPaint)

            // App badge
            val appLabel = if (item.appName.isNotBlank()) item.appName else item.packageName.substringAfterLast('.')
            appNamePaint.textSize = 12f
            appNamePaint.color = Color.WHITE
            val badgeTextW = appNamePaint.measureText(appLabel)
            val badgePadH = 10f
            val badgePadV = 4f
            val badgeW = badgeTextW + (badgePadH * 2f)
            val badgeH = 22f
            val badgeLeft = left + 14f
            val badgeTop = startY + (rowHeight - badgeH) / 2f
            val badgeRect = RectF(badgeLeft, badgeTop, badgeLeft + badgeW, badgeTop + badgeH)

            appBadgePaint.color = accentColor
            appBadgePaint.alpha = 230
            canvas.drawRoundRect(badgeRect, 6f, 6f, appBadgePaint)
            canvas.drawText(appLabel, badgeLeft + badgePadH, badgeTop + 15.5f, appNamePaint)

            // Title & Content
            val titleText = if (item.title.isNotBlank()) item.title else item.text
            val displaySnippet = if (item.text.isNotBlank() && item.title.isNotBlank()) {
                "${item.title} • ${item.text}"
            } else titleText

            titlePaint.textSize = 13.5f
            titlePaint.color = Color.WHITE

            val textStartX = badgeLeft + badgeW + 12f
            val maxTextW = right - textStartX - 16f
            val ellipsized = truncateText(displaySnippet, titlePaint, maxTextW)

            canvas.drawText(ellipsized, textStartX, startY + (rowHeight / 2f) + 4.5f, titlePaint)

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
