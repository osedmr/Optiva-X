package com.optivax.app.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.optivax.app.MainActivity
import com.optivax.app.core.Constants
import java.util.concurrent.TimeUnit

class StorageReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        Log.d(Constants.TAG, "[WORKER] StorageReminderWorker triggered.")
        try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalBytes = totalBlocks * blockSize
            val freeBytes = availableBlocks * blockSize
            val usedPercentage = if (totalBytes > 0) ((totalBytes - freeBytes).toFloat() / totalBytes) * 100f else 0f

            if (usedPercentage >= 85f) {
                showReminderNotification(
                    title = "Optiva X: Depolama Alanınız Dolmak Üzere!",
                    message = "Cihazınızda doluluk %${usedPercentage.toInt()} seviyesine ulaştı. Benzer fotoğrafları ve gereksiz medya fazlalıklarını temizleyerek anında yer açın."
                )
            } else {
                showReminderNotification(
                    title = "Optiva X: Haftalık Depolama Hijyeni",
                    message = "Haftalık kontrol: Cihazınızda biriken eski ekran görüntüleri ve kopya medyaları temizlemeyi unutmayın."
                )
            }

            return Result.success()
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[WORKER] Error in StorageReminderWorker: ${e.message}", e)
            return Result.failure()
        }
    }

    private fun showReminderNotification(title: String, message: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "optivax_storage_reminder"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Depolama ve Temizlik Hatırlatıcıları",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Haftalık ve depolama doluluk bildirimleri"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }

    companion object {
        private const val WORK_NAME = "OptivaX_Storage_Periodic_Reminder"

        fun schedule(context: Context) {
            try {
                val constraints = Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()

                val request = PeriodicWorkRequestBuilder<StorageReminderWorker>(7, TimeUnit.DAYS)
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
                Log.d(Constants.TAG, "[WORKER] Scheduled periodic storage reminder for every 7 days.")
            } catch (e: Exception) {
                Log.e(Constants.TAG, "[WORKER] Failed to schedule reminder: ${e.message}")
            }
        }
    }
}
