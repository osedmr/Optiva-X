package com.optivax.app.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import java.io.File
import kotlin.math.sqrt

object BlurDetector {

    const val DEFAULT_BLUR_THRESHOLD = 80.0

    /**
     * Calculates the Laplacian variance (sharpness score) of an image file.
     * Lower score indicates higher blurriness.
     * Returns null if the file cannot be decoded.
     */
    fun calculateLaplacianVariance(file: File, sampleTargetSize: Int = 160): Double? {
        if (!file.exists() || !file.isFile || file.length() == 0L) return null
        return try {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, boundsOptions)
            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) return null

            var inSampleSize = 1
            while (boundsOptions.outWidth / (inSampleSize * 2) >= sampleTargetSize &&
                boundsOptions.outHeight / (inSampleSize * 2) >= sampleTargetSize) {
                inSampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            val original = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return null

            val scaled = Bitmap.createScaledBitmap(original, sampleTargetSize, sampleTargetSize, true)
            if (scaled != original) {
                original.recycle()
            }

            val variance = calculateVarianceFromBitmap(scaled)
            scaled.recycle()
            variance
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Computes the variance of the 3x3 Laplacian convolution on grayscale pixel data.
     */
    fun calculateVarianceFromBitmap(bitmap: Bitmap): Double {
        val width = bitmap.width
        val height = bitmap.height
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        // Convert to grayscale matrix
        val gray = DoubleArray(width * height)
        for (i in pixels.indices) {
            val p = pixels[i]
            gray[i] = (Color.red(p) * 0.299 + Color.green(p) * 0.587 + Color.blue(p) * 0.114)
        }

        // Apply 3x3 Laplacian Kernel:
        // [ 0,  1,  0 ]
        // [ 1, -4,  1 ]
        // [ 0,  1,  0 ]
        val laplacian = DoubleArray((width - 2) * (height - 2))
        var idx = 0
        var sum = 0.0

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val center = gray[y * width + x]
                val top = gray[(y - 1) * width + x]
                val bottom = gray[(y + 1) * width + x]
                val left = gray[y * width + (x - 1)]
                val right = gray[y * width + (x + 1)]

                val value = top + bottom + left + right - (4.0 * center)
                laplacian[idx] = value
                sum += value
                idx++
            }
        }

        val count = laplacian.size
        if (count == 0) return 0.0
        val mean = sum / count

        var varianceSum = 0.0
        for (v in laplacian) {
            val diff = v - mean
            varianceSum += diff * diff
        }

        return varianceSum / count
    }

    /**
     * Estimates blur percentage given a variance score and threshold.
     * 100% means completely blurred / dark / out of focus.
     */
    fun calculateBlurPercentage(variance: Double, threshold: Double = DEFAULT_BLUR_THRESHOLD): Int {
        if (variance >= threshold) return 0
        val ratio = (threshold - variance) / threshold
        return (ratio * 100.0).toInt().coerceIn(0, 100)
    }
}
