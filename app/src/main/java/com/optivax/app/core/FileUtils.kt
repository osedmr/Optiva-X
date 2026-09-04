package com.optivax.app.core

import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.security.MessageDigest
import java.util.Locale

/**
 * High-performance file operations, size formatting, and SHA-256 hashing utility.
 */
object FileUtils {

    private const val BUFFER_SIZE = 16384 // 16 KB buffer for streaming I/O
    private const val DEFAULT_SAMPLE_SIZE = 8192L // 8 KB for fast partial hash

    /**
     * Converts raw bytes into a human-readable string (e.g. 1.25 GB, 450.00 KB, 512 B).
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0L) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
        var value = bytes.toDouble()
        var unitIndex = 0

        while (value >= 1024.0 && unitIndex < units.size - 1) {
            value /= 1024.0
            unitIndex++
        }

        return if (unitIndex == 0) {
            String.format(Locale.US, "%d %s", bytes, units[unitIndex])
        } else {
            String.format(Locale.US, "%.2f %s", value, units[unitIndex])
        }
    }

    /**
     * Calculates complete SHA-256 checksum of the specified file using buffered streaming
     * to ensure minimal memory footprint even on multi-gigabyte files.
     */
    fun calculateSha256(file: File): String {
        if (!file.exists() || !file.isFile || file.length() == 0L) {
            return ""
        }
        return FileInputStream(file).use { input ->
            calculateSha256(input)
        }
    }

    /**
     * Calculates complete SHA-256 checksum from an InputStream.
     */
    fun calculateSha256(inputStream: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buffer = ByteArray(BUFFER_SIZE)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
        return bytesToHex(digest.digest())
    }

    /**
     * Computes a partial SHA-256 hash over the first [sampleBytes] of the file.
     * Used as a fast pre-filter in duplicate detection before executing full hashes.
     */
    fun calculatePartialSha256(file: File, sampleBytes: Long = DEFAULT_SAMPLE_SIZE): String {
        if (!file.exists() || !file.isFile || file.length() == 0L) {
            return ""
        }
        FileInputStream(file).use { input ->
            val digest = MessageDigest.getInstance("SHA-256")
            val targetBytes = file.length().coerceAtMost(sampleBytes).toInt()
            val buffer = ByteArray(targetBytes.coerceAtMost(BUFFER_SIZE))
            var remaining = targetBytes

            while (remaining > 0) {
                val toRead = remaining.coerceAtMost(buffer.size)
                val read = input.read(buffer, 0, toRead)
                if (read == -1) break
                digest.update(buffer, 0, read)
                remaining -= read
            }
            return bytesToHex(digest.digest())
        }
    }

    /**
     * Converts a byte array to a lowercase hex string.
     */
    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        val hexArray = "0123456789abcdef".toCharArray()
        for (i in bytes.indices) {
            val v = bytes[i].toInt() and 0xFF
            hexChars[i * 2] = hexArray[v ushr 4]
            hexChars[i * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    /**
     * Extracts the extension of a file name in lowercase without leading dot.
     */
    fun getFileExtension(fileName: String): String {
        val lastDotIndex = fileName.lastIndexOf('.')
        return if (lastDotIndex >= 0 && lastDotIndex < fileName.length - 1) {
            fileName.substring(lastDotIndex + 1).lowercase(Locale.ROOT)
        } else {
            ""
        }
    }

    /**
     * Categorizes a file extension into general media/document categories.
     */
    fun getMimeCategory(extension: String): String {
        return when (extension.lowercase(Locale.ROOT)) {
            "jpg", "jpeg", "png", "webp", "gif", "bmp", "heic", "svg" -> "IMAGE"
            "mp4", "mkv", "avi", "mov", "webm", "3gp", "flv" -> "VIDEO"
            "mp3", "m4a", "wav", "flac", "ogg", "aac", "opus" -> "AUDIO"
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt" -> "DOCUMENT"
            "zip", "rar", "7z", "tar", "gz", "bz2" -> "ARCHIVE"
            "apk", "xapk", "apks" -> "APK"
            else -> "OTHER"
        }
    }
}
