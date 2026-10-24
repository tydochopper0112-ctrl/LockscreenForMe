package com.fold.lockscreen.data

import android.content.Context
import android.graphics.*
import android.net.Uri
import android.util.LruCache
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

    // Multi-entry LRU memory cache (up to 32MB) to hold wallpapers and thumbnails safely
    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = (maxMemory / 8).coerceAtLeast(16 * 1024) // in KB

    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return if (!bitmap.isRecycled) bitmap.byteCount / 1024 else 1
        }
    }

    fun loadBitmap(context: Context, path: String, targetMaxDim: Int = 2048): Bitmap? {
        val cacheKey = "$path-$targetMaxDim"

        synchronized(memoryCache) {
            val cached = memoryCache.get(cacheKey)
            if (cached != null && !cached.isRecycled) {
                return cached
            }
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

            // Decode dimensions first
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            inputStream.close()

            // Re-open for bounds measuring
            val boundsStream = if (path.startsWith("assets:")) {
                context.assets.open(path.removePrefix("assets:"))
            } else {
                File(path.removePrefix("file:")).inputStream()
            }
            BitmapFactory.decodeStream(boundsStream, null, options)
            boundsStream.close()

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

            if (bitmap != null && !bitmap.isRecycled) {
                synchronized(memoryCache) {
                    memoryCache.put(cacheKey, bitmap)
                }
                return bitmap
            }
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * Draws the bitmap with Center-Crop mode so that it fills the given canvas dimensions
     * without distortion. Strictly guards against recycled bitmaps.
     */
    fun drawCenterCrop(
        canvas: Canvas,
        bitmap: Bitmap?,
        canvasWidth: Float,
        canvasHeight: Float,
        paint: Paint? = null
    ) {
        if (bitmap == null || bitmap.isRecycled) return
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
        try {
            if (!bitmap.isRecycled) {
                canvas.drawBitmap(bitmap, null, dstRect, paint)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
            // Clean up older custom wallpapers
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
