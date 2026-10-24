package com.fold.lockscreen.canvas

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF

object DotMatrixFont {
    // 5 wide x 7 high bit patterns for 0-9, :, space, -, A-Z
    private val FONT_PATTERNS = mapOf(
        '0' to intArrayOf(
            0b01110,
            0b10001,
            0b10011,
            0b10101,
            0b11001,
            0b10001,
            0b01110
        ),
        '1' to intArrayOf(
            0b00100,
            0b01100,
            0b00100,
            0b00100,
            0b00100,
            0b00100,
            0b01110
        ),
        '2' to intArrayOf(
            0b01110,
            0b10001,
            0b00001,
            0b00010,
            0b00100,
            0b01000,
            0b11111
        ),
        '3' to intArrayOf(
            0b11110,
            0b00001,
            0b00001,
            0b01110,
            0b00001,
            0b00001,
            0b11110
        ),
        '4' to intArrayOf(
            0b00010,
            0b00110,
            0b01010,
            0b10010,
            0b11111,
            0b00010,
            0b00010
        ),
        '5' to intArrayOf(
            0b11111,
            0b10000,
            0b11110,
            0b00001,
            0b00001,
            0b10001,
            0b01110
        ),
        '6' to intArrayOf(
            0b00110,
            0b01000,
            0b10000,
            0b11110,
            0b10001,
            0b10001,
            0b01110
        ),
        '7' to intArrayOf(
            0b11111,
            0b00001,
            0b00010,
            0b00100,
            0b01000,
            0b01000,
            0b01000
        ),
        '8' to intArrayOf(
            0b01110,
            0b10001,
            0b10001,
            0b01110,
            0b10001,
            0b10001,
            0b01110
        ),
        '9' to intArrayOf(
            0b01110,
            0b10001,
            0b10001,
            0b01111,
            0b00001,
            0b00010,
            0b01100
        ),
        ':' to intArrayOf(
            0b00000,
            0b00100,
            0b00100,
            0b00000,
            0b00100,
            0b00100,
            0b00000
        ),
        ' ' to intArrayOf(
            0b00000,
            0b00000,
            0b00000,
            0b00000,
            0b00000,
            0b00000,
            0b00000
        ),
        '-' to intArrayOf(
            0b00000,
            0b00000,
            0b00000,
            0b11111,
            0b00000,
            0b00000,
            0b00000
        ),
        '.' to intArrayOf(
            0b00000,
            0b00000,
            0b00000,
            0b00000,
            0b00000,
            0b00110,
            0b00110
        )
    )

    fun measureWidth(text: String, dotSize: Float, dotSpacing: Float, charSpacing: Float): Float {
        var width = 0f
        text.forEachIndexed { index, ch ->
            val cols = if (ch == ':' || ch == '.') 3 else 5
            width += cols * (dotSize + dotSpacing)
            if (index < text.length - 1) {
                width += charSpacing
            }
        }
        return width
    }

    fun measureHeight(dotSize: Float, dotSpacing: Float): Float {
        return 7 * (dotSize + dotSpacing)
    }

    fun drawText(
        canvas: Canvas,
        text: String,
        startX: Float,
        startY: Float,
        dotSize: Float,
        dotSpacing: Float,
        charSpacing: Float,
        litPaint: Paint,
        unlitPaint: Paint? = null
    ) {
        var currentX = startX
        text.uppercase().forEach { ch ->
            val pattern = FONT_PATTERNS[ch] ?: FONT_PATTERNS[' ']!!
            val cols = if (ch == ':' || ch == '.') 3 else 5
            val colShift = if (ch == ':' || ch == '.') 1 else 0

            for (row in 0 until 7) {
                val rowBits = pattern[row]
                for (col in 0 until cols) {
                    val bitIndex = 4 - (col + colShift)
                    val isLit = if (bitIndex >= 0) (rowBits and (1 shl bitIndex)) != 0 else false

                    val cx = currentX + col * (dotSize + dotSpacing) + dotSize / 2f
                    val cy = startY + row * (dotSize + dotSpacing) + dotSize / 2f
                    val radius = dotSize / 2f

                    if (isLit) {
                        canvas.drawCircle(cx, cy, radius, litPaint)
                    } else if (unlitPaint != null) {
                        canvas.drawCircle(cx, cy, radius * 0.7f, unlitPaint)
                    }
                }
            }
            currentX += cols * (dotSize + dotSpacing) + charSpacing
        }
    }
}
