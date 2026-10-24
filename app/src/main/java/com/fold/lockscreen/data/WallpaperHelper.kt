package com.fold.lockscreen.data

import android.content.Context
import android.graphics.*
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max

data class WallpaperItem(
    val id: String,
    val title: String,
    val path: String,
    val isCustom: Boolean = false
)

object WallpaperHelper {

    val PRESET_WALLPAPERS = listOf(
        WallpaperItem(
            id = "black",
            title = "AMOLED Black",
            path = "assets:wallpapers/BLACK.jpg"
        ),
        WallpaperItem(
            id = "dont_2",
            title = "Don't Touch 1",
            path = "assets:wallpapers/Don't 2.jpg"
        ),
        WallpaperItem(
            id = "dont_3",
            title = "Don't Touch 2",
            path = "assets:wallpapers/Don't 3.jpg"
        ),
        WallpaperItem(
            id = "split_panel",
            title = "Split Panel LF",
            path = "assets:wallpapers/Split-Panel-LF.jpg"
        )
    )

    private var cachedPath: String? = null
    private var cachedBitmap: Bitmap? = null

    @Synchronized
    fun loadBitmap(context: Context, path: String, targetMaxDim: Int = 2048): Bitmap? {
        if (path == cachedPath && cachedBitmap != null && !cachedBitmap!!.isRecycled) {
            return cachedBitmap
        }

        try {
            var inputStream: InputStream? = null
            if (path.startsWith("assets:")) {
                val assetRelative = path.removePrefix("assets:")
                inputStream = context.assets.open(assetRelative)
            } else if (path.startsWith("file:")) {
                val filePath = path.removePrefix("file:")
                val file = File(filePath)
                if (file.exists()) {
                    inputStream = file.inputStream()
                }
            }

            if (inputStream == null) return null

            // Decode with bounds to avoid huge memory footprint
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            // Need mark/reset support or recreate stream
            inputStream.close()

            // Re-open stream for sizing
            val sizingStream = if (path.startsWith("assets:")) {
                context.assets.open(path.removePrefix("assets:"))
            } else {
                File(path.removePrefix("file:")).inputStream()
            }
            BitmapFactory.decodeStream(sizingStream, null, options)
            sizingStream.close()

            var sampleSize = 1
            var w = options.outWidth
            var h = options.outHeight
            while (w > targetMaxDim || h > targetMaxDim) {
                sampleSize *= 2
                w /= 2
                h /= 2
            }

            val decodeStream = if (path.startsWith("assets:")) {
                context.assets.open(path.removePrefix("assets:"))
            } else {
                File(path.removePrefix("file:")).inputStream()
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val bitmap = BitmapFactory.decodeStream(decodeStream, null, decodeOptions)
            decodeStream.close()

            if (bitmap != null) {
                cachedBitmap?.recycle()
                cachedBitmap = bitmap
                cachedPath = path
            }
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Draws the bitmap with Center-Crop mode so that it fills the given canvas dimensions
     * without distortion, clipping overflow equally from edges.
     */
    fun drawCenterCrop(
        canvas: Canvas,
        bitmap: Bitmap,
        canvasWidth: Float,
        canvasHeight: Float,
        paint: Paint? = null
    ) {
        if (canvasWidth <= 0 || canvasHeight <= 0) return
        val bW = bitmap.width.toFloat()
        val bH = bitmap.height.toFloat()
        if (bW <= 0 || bH <= 0) return

        val scale = max(canvasWidth / bW, canvasHeight / bH)
        val scaledW = bW * scale
        val scaledH = bH * scale
        val left = (canvasWidth - scaledW) / 2f
        val top = (canvasHeight - scaledH) / 2f

        val dstRect = RectF(left, top, left + scaledW, top + scaledH)
        canvas.drawBitmap(bitmap, null, dstRect, paint)
    }

    /**
     * Copies a user-picked photo from URI into internal app files directory.
     * Returns the file path formatted as "file:<absolute-path>"
     */
    fun copyUriToCustomWallpaper(context: Context, uri: Uri): String? {
        return try {
            val destFile = File(context.filesDir, "custom_wallpaper_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            // Clean up any older custom wallpapers
            context.filesDir.listFiles()?.forEach { file ->
                if (file.name.startsWith("custom_wallpaper_") && file.absolutePath != destFile.absolutePath) {
                    file.delete()
                }
            }
            "file:${destFile.absolutePath}"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
