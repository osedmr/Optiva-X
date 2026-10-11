package com.optivax.app.core

import android.app.PendingIntent
import android.content.ContentUris
import android.content.Context
import android.content.IntentSender
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.io.File

/**
 * Helper utility for Google Play compliant Scoped Storage and MediaStore operations.
 * Resolves content URIs from filesystem paths and creates Android 11+ delete requests.
 */
object MediaStoreHelper {

    /**
     * Resolves the MediaStore content Uri for a given file path.
     * Searches Images, Video, Audio, and Downloads tables.
     */
    fun getMediaUriForPath(context: Context, path: String): Uri? {
        val file = File(path)
        if (!file.exists()) return null

        val projection = arrayOf(MediaStore.MediaColumns._ID)
        val selection = "${MediaStore.MediaColumns.DATA} = ?"
        val selectionArgs = arrayOf(path)

        val queryUris = mutableListOf(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            queryUris.add(MediaStore.Downloads.EXTERNAL_CONTENT_URI)
        }

        for (baseUri in queryUris) {
            try {
                context.contentResolver.query(
                    baseUri,
                    projection,
                    selection,
                    selectionArgs,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val idIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                        val id = cursor.getLong(idIndex)
                        val contentUri = ContentUris.withAppendedId(baseUri, id)
                        Log.d(Constants.TAG, "[MEDIASTORE_HELPER] Resolved URI for '$path' -> $contentUri")
                        return contentUri
                    }
                }
            } catch (e: Exception) {
                Log.w(Constants.TAG, "[MEDIASTORE_HELPER] Query failed for $baseUri: ${e.message}")
            }
        }
        return null
    }

    /**
     * Resolves content URIs for a batch of file paths.
     */
    fun getMediaUrisForPaths(context: Context, paths: List<String>): List<Uri> {
        return paths.mapNotNull { getMediaUriForPath(context, it) }
    }

    /**
     * Creates an IntentSender for Android 11+ (API 30+) batch media deletion request.
     * The OS displays an official system confirmation dialog:
     * "Allow Optiva X to delete X items?"
     */
    fun createDeleteIntentSender(context: Context, uris: List<Uri>): IntentSender? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && uris.isNotEmpty()) {
            return try {
                val pendingIntent: PendingIntent = MediaStore.createDeleteRequest(context.contentResolver, uris)
                pendingIntent.intentSender
            } catch (e: Exception) {
                Log.e(Constants.TAG, "[MEDIASTORE_HELPER] Failed to create delete request: ${e.message}", e)
                null
            }
        }
        return null
    }

    /**
     * Deletes a media file via ContentResolver if possible.
     * Returns true if successfully deleted via MediaStore.
     */
    fun deleteViaContentResolver(context: Context, uri: Uri): Boolean {
        return try {
            val rows = context.contentResolver.delete(uri, null, null)
            rows > 0
        } catch (e: Exception) {
            Log.w(Constants.TAG, "[MEDIASTORE_HELPER] ContentResolver delete failed for $uri: ${e.message}")
            false
        }
    }
}
