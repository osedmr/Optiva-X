package com.optivax.app

import com.optivax.app.core.FileUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class FileUtilsTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun formatFileSize_returnsCorrectUnits() {
        assertEquals("0 B", FileUtils.formatFileSize(0L))
        assertEquals("0 B", FileUtils.formatFileSize(-10L))
        assertEquals("512 B", FileUtils.formatFileSize(512L))
        assertEquals("1.00 KB", FileUtils.formatFileSize(1024L))
        assertEquals("1.50 KB", FileUtils.formatFileSize(1536L))
        assertEquals("1.00 MB", FileUtils.formatFileSize(1024L * 1024L))
        assertEquals("2.50 GB", FileUtils.formatFileSize((2.5 * 1024L * 1024L * 1024L).toLong()))
    }

    @Test
    fun calculateSha256_computesAccurateHash() {
        val testFile = tempFolder.newFile("test_hash.txt")
        testFile.writeText("OptivaX_Safe_Optimization")

        val hash = FileUtils.calculateSha256(testFile)
        assertTrue(hash.isNotEmpty())
        assertEquals(64, hash.length) // SHA-256 length in hex is 64 characters

        // Identical file produces identical hash
        val duplicateFile = tempFolder.newFile("test_hash_dup.txt")
        duplicateFile.writeText("OptivaX_Safe_Optimization")
        val dupHash = FileUtils.calculateSha256(duplicateFile)
        assertEquals(hash, dupHash)
    }

    @Test
    fun calculatePartialSha256_returnsValidHash() {
        val testFile = tempFolder.newFile("test_partial.bin")
        val data = ByteArray(16384) { it.toByte() }
        testFile.writeBytes(data)

        val partialHash = FileUtils.calculatePartialSha256(testFile, 8192L)
        assertTrue(partialHash.isNotEmpty())
        assertEquals(64, partialHash.length)
    }

    @Test
    fun getFileExtension_and_getMimeCategory_workProperly() {
        assertEquals("apk", FileUtils.getFileExtension("com.example.app.apk"))
        assertEquals("jpg", FileUtils.getFileExtension("PHOTO_2026.JPG"))
        assertEquals("", FileUtils.getFileExtension("LICENSE"))

        assertEquals("APK", FileUtils.getMimeCategory("apk"))
        assertEquals("IMAGE", FileUtils.getMimeCategory("png"))
        assertEquals("VIDEO", FileUtils.getMimeCategory("mp4"))
        assertEquals("AUDIO", FileUtils.getMimeCategory("mp3"))
        assertEquals("DOCUMENT", FileUtils.getMimeCategory("pdf"))
        assertEquals("ARCHIVE", FileUtils.getMimeCategory("zip"))
        assertEquals("OTHER", FileUtils.getMimeCategory("unknownext"))
    }
}
