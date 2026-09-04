package com.optivax.app.data.repository

import android.util.Log
import com.optivax.app.core.Constants
import com.optivax.app.core.Resource
import com.optivax.app.data.local.WhitelistDao
import com.optivax.app.data.system.StorageStatsHelper
import com.optivax.app.data.system.UsageStatsHelper
import com.optivax.app.di.IoDispatcher
import com.optivax.app.domain.model.AppUsageInfo
import com.optivax.app.domain.repository.AppManagerRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository implementing application usage, storage analytics, and idle app filtering
 * on [Dispatchers.IO] with unified Logcat telemetry under [Constants.TAG].
 */
@Singleton
class AppManagerRepositoryImpl @Inject constructor(
    private val usageStatsHelper: UsageStatsHelper,
    private val storageStatsHelper: StorageStatsHelper,
    private val whitelistDao: WhitelistDao,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : AppManagerRepository {

    override fun getInstalledAppsUsage(idleThresholdDays: Long): Flow<Resource<List<AppUsageInfo>>> = flow {
        emit(Resource.Loading)
        Log.d(Constants.TAG, "[APPS] Starting getInstalledAppsUsage query (threshold: $idleThresholdDays days)...")
        try {
            val now = System.currentTimeMillis()
            val startTime = now - (365L * 24L * 60L * 60L * 1000L)
            val usageStatsMap = usageStatsHelper.queryUsageStatsMap(startTime, now)
            val installedApps = usageStatsHelper.getInstalledApplications(includeSystemApps = true)
            val idleThresholdMillis = idleThresholdDays * 24L * 60L * 60L * 1000L

            Log.d(Constants.TAG, "[APPS] Processing metrics for ${installedApps.size} installed apps...")
            val appList = mutableListOf<AppUsageInfo>()

            for (appInfo in installedApps) {
                val packageName = appInfo.packageName
                val appName = usageStatsHelper.getApplicationLabel(appInfo)
                val isSystem = usageStatsHelper.isSystemApp(appInfo)
                val storageMetrics = storageStatsHelper.getPackageStorageStats(packageName)
                val usageStats = usageStatsMap[packageName]
                val lastTimeUsed = usageStats?.lastTimeUsed ?: 0L

                val isIdle = !isSystem && (lastTimeUsed == 0L || (now - lastTimeUsed) >= idleThresholdMillis)

                appList.add(
                    AppUsageInfo(
                        packageName = packageName,
                        appName = appName,
                        appSizeBytes = storageMetrics.appBytes,
                        dataSizeBytes = storageMetrics.dataBytes,
                        cacheSizeBytes = storageMetrics.cacheBytes,
                        lastTimeUsed = lastTimeUsed,
                        isIdle = isIdle,
                        isSystemApp = isSystem,
                        uid = appInfo.uid
                    )
                )
            }

            val sortedList = appList.sortedByDescending { it.totalSizeBytes }
            val idleCount = sortedList.count { it.isIdle }
            Log.d(
                Constants.TAG,
                "[APPS] Analyzed ${sortedList.size} apps: $idleCount idle apps detected."
            )
            emit(Resource.Success(sortedList))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[APPS] Error in getInstalledAppsUsage: ${e.message}", e)
            emit(Resource.Error(e.localizedMessage ?: "Uygulama istatistikleri alınırken hata oluştu", e))
        }
    }.flowOn(ioDispatcher)

    override fun getIdleApps(idleThresholdDays: Long): Flow<Resource<List<AppUsageInfo>>> = flow {
        emit(Resource.Loading)
        Log.d(Constants.TAG, "[APPS] Starting getIdleApps query...")
        try {
            val now = System.currentTimeMillis()
            val startTime = now - (365L * 24L * 60L * 60L * 1000L)
            val usageStatsMap = usageStatsHelper.queryUsageStatsMap(startTime, now)
            val installedApps = usageStatsHelper.getInstalledApplications(includeSystemApps = false)
            val idleThresholdMillis = idleThresholdDays * 24L * 60L * 60L * 1000L

            val idleApps = mutableListOf<AppUsageInfo>()

            for (appInfo in installedApps) {
                val packageName = appInfo.packageName
                val isWhitelisted = whitelistDao.isWhitelisted(packageName)
                if (isWhitelisted) continue

                val appName = usageStatsHelper.getApplicationLabel(appInfo)
                val storageMetrics = storageStatsHelper.getPackageStorageStats(packageName)
                val usageStats = usageStatsMap[packageName]
                val lastTimeUsed = usageStats?.lastTimeUsed ?: 0L

                val isIdle = (lastTimeUsed == 0L || (now - lastTimeUsed) >= idleThresholdMillis)

                if (isIdle) {
                    idleApps.add(
                        AppUsageInfo(
                            packageName = packageName,
                            appName = appName,
                            appSizeBytes = storageMetrics.appBytes,
                            dataSizeBytes = storageMetrics.dataBytes,
                            cacheSizeBytes = storageMetrics.cacheBytes,
                            lastTimeUsed = lastTimeUsed,
                            isIdle = true,
                            isSystemApp = false,
                            uid = appInfo.uid
                        )
                    )
                }
            }

            Log.d(Constants.TAG, "[APPS] getIdleApps found ${idleApps.size} idle applications.")
            emit(Resource.Success(idleApps.sortedByDescending { it.totalSizeBytes }))
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[APPS] Error in getIdleApps: ${e.message}", e)
            emit(Resource.Error(e.localizedMessage ?: "Atıl uygulamalar listelenirken hata oluştu", e))
        }
    }.flowOn(ioDispatcher)
}
