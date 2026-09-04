package com.optivax.app.core

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import java.io.File

object ImageHasher {

    /**
     * Computes a 64-bit perceptual difference hash (dHash) for the given image file.
     * Downsamples to 9x8 and compares adjacent pixel brightness.
     * Returns null if the file cannot be decoded as an image.
     */
    fun computeDHash(file: File): Long? {
        if (!file.exists() || !file.isFile || file.length() == 0L) return null
        return try {
            val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, boundsOptions)
            if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) return null

            // Calculate efficient sample size to save RAM
            val sampleSize = calculateInSampleSize(boundsOptions, 72, 64)
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.RGB_565
            }
            val original = BitmapFactory.decodeFile(file.absolutePath, decodeOptions) ?: return null

            // Resize strictly to 9x8 for dHash
            val scaled = Bitmap.createScaledBitmap(original, 9, 8, true)
            if (scaled != original) {
                original.recycle()
            }

            val hash = computeDHashFromBitmap(scaled)
            scaled.recycle()
            hash
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Computes a 64-bit dHash directly from a 9x8 bitmap.
     */
    fun computeDHashFromBitmap(bitmap: Bitmap): Long {
        val width = 9
        val height = 8
        val pixels = IntArray(width * height)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

        var hash = 0L
        var bitIndex = 0

        for (y in 0 until height) {
            for (x in 0 until width - 1) {
                val leftPixel = pixels[y * width + x]
                val rightPixel = pixels[y * width + (x + 1)]

                val leftBrightness = (Color.red(leftPixel) * 299 + Color.green(leftPixel) * 587 + Color.blue(leftPixel) * 114) / 1000
                val rightBrightness = (Color.red(rightPixel) * 299 + Color.green(rightPixel) * 587 + Color.blue(rightPixel) * 114) / 1000

                if (leftBrightness > rightBrightness) {
                    hash = hash or (1L shl bitIndex)
                }
                bitIndex++
            }
        }
        return hash
    }

    /**
     * Calculates the Hamming distance between two 64-bit hashes.
     * Returns the count of differing bits (0..64).
     * Distance <= 8 means > 87.5% perceptual similarity.
     */
    fun hammingDistance(hash1: Long, hash2: Long): Int {
        return java.lang.Long.bitCount(hash1 xor hash2)
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
