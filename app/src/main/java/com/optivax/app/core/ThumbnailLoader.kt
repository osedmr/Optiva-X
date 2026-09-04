package com.optivax.app.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.util.LruCache
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * High-performance, memory-conscious thumbnail and media preview loader with LRU caching.
 * Decodes images (.jpg, .png, .webp) and video frames (.mp4, .3gp) off the UI thread.
 */
object ThumbnailLoader {

    private val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSize = (maxMemory / 8).coerceAtLeast(1024)

    private val memoryCache = object : LruCache<String, Bitmap>(cacheSize) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    suspend fun getThumbnail(file: File, targetSize: Int = 220): Bitmap? = withContext(Dispatchers.IO) {
        val cacheKey = "${file.absolutePath}_$targetSize"
        memoryCache.get(cacheKey)?.let { return@withContext it }

        if (!file.exists() || !file.isFile) return@withContext null

        val ext = file.extension.lowercase()
        val bitmap = when (ext) {
            "jpg", "jpeg", "png", "webp" -> decodeImageThumbnail(file, targetSize)
            "mp4", "3gp", "mkv", "mov" -> decodeVideoThumbnail(file, targetSize)
            else -> null
        }

        if (bitmap != null) {
            memoryCache.put(cacheKey, bitmap)
        }
        bitmap
    }

    suspend fun getFullPreview(file: File, maxDimension: Int = 1080): Bitmap? = withContext(Dispatchers.IO) {
        val cacheKey = "${file.absolutePath}_full_$maxDimension"
        memoryCache.get(cacheKey)?.let { return@withContext it }

        if (!file.exists() || !file.isFile) return@withContext null

        val ext = file.extension.lowercase()
        val bitmap = when (ext) {
            "jpg", "jpeg", "png", "webp" -> decodeImageThumbnail(file, maxDimension)
            "mp4", "3gp", "mkv", "mov" -> decodeVideoThumbnail(file, maxDimension)
            else -> null
        }

        if (bitmap != null) {
            memoryCache.put(cacheKey, bitmap)
        }
        bitmap
    }

    private fun decodeImageThumbnail(file: File, targetSize: Int): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, options)

            options.inSampleSize = calculateInSampleSize(options, targetSize, targetSize)
            options.inJustDecodeBounds = false
            options.inPreferredConfig = Bitmap.Config.RGB_565

            BitmapFactory.decodeFile(file.absolutePath, options)
        } catch (e: Exception) {
            null
        }
    }

    private fun decodeVideoThumbnail(file: File, targetSize: Int): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val rawBitmap = retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()

            if (rawBitmap != null && (rawBitmap.width > targetSize || rawBitmap.height > targetSize)) {
                val scale = targetSize.toFloat() / maxOf(rawBitmap.width, rawBitmap.height)
                val scaled = Bitmap.createScaledBitmap(
                    rawBitmap,
                    (rawBitmap.width * scale).toInt().coerceAtLeast(1),
                    (rawBitmap.height * scale).toInt().coerceAtLeast(1),
                    true
                )
                if (scaled != rawBitmap) {
                    rawBitmap.recycle()
                }
                scaled
            } else {
                rawBitmap
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize.coerceAtLeast(1)
    }
}

/**
 * Asynchronously loads and remembers an ImageBitmap thumbnail for Compose UI.
 */
@Composable
fun rememberThumbnailState(file: File, targetSize: Int = 220): State<ImageBitmap?> {
    return produceState<ImageBitmap?>(initialValue = null, key1 = file.absolutePath) {
        val bitmap = ThumbnailLoader.getThumbnail(file, targetSize)
        value = bitmap?.asImageBitmap()
    }
}

/**
 * Asynchronously loads full-size preview image for In-App Media Viewer.
 */
@Composable
fun rememberFullMediaState(file: File, maxDimension: Int = 1080): State<ImageBitmap?> {
    return produceState<ImageBitmap?>(initialValue = null, key1 = file.absolutePath) {
        val bitmap = ThumbnailLoader.getFullPreview(file, maxDimension)
        value = bitmap?.asImageBitmap()
    }
}
