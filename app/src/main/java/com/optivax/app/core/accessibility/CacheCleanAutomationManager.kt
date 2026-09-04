package com.optivax.app.core.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.optivax.app.core.Constants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.LinkedList
import java.util.Queue

data class CacheTargetApp(
    val packageName: String,
    val appName: String,
    val cacheBytes: Long
)

sealed class CacheCleanState {
    object Idle : CacheCleanState()
    data class Running(
        val currentApp: CacheTargetApp,
        val currentIndex: Int,
        val totalCount: Int,
        val freedBytes: Long,
        val statusMessage: String
    ) : CacheCleanState()
    data class Completed(
        val totalFreedBytes: Long,
        val cleanedCount: Int
    ) : CacheCleanState()
    object Cancelled : CacheCleanState()
}

/**
 * Singleton state-machine engine that orchestrates high-speed, safe automated
 * clearing of application caches via Android's AccessibilityService.
 */
object CacheCleanAutomationManager {

    private val _state = MutableStateFlow<CacheCleanState>(CacheCleanState.Idle)
    val state: StateFlow<CacheCleanState> = _state.asStateFlow()

    private var activeService: OptivaCacheCleanerService? = null
    private val queue: Queue<CacheTargetApp> = LinkedList()
    private var totalToClean: Int = 0
    private var totalFreedBytes: Long = 0L
    private var currentTarget: CacheTargetApp? = null
    private var currentStep: AutomationStep = AutomationStep.IDLE
    private var processedCount: Int = 0

    private val mainHandler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null

    private enum class AutomationStep {
        IDLE,
        APP_DETAILS,
        STORAGE_PAGE
    }

    fun bindService(service: OptivaCacheCleanerService) {
        activeService = service
        Log.d(Constants.TAG, "[ACCESSIBILITY] OptivaCacheCleanerService bound to manager.")
    }

    fun unbindService() {
        activeService = null
        Log.d(Constants.TAG, "[ACCESSIBILITY] OptivaCacheCleanerService unbound.")
    }

    fun isServiceConnected(): Boolean = activeService != null

    /**
     * Starts the automated cache cleaning process for the given list of apps.
     */
    fun startCleaning(context: Context, apps: List<CacheTargetApp>) {
        if (apps.isEmpty()) return
        if (activeService == null) {
            Log.w(Constants.TAG, "[ACCESSIBILITY] Service not connected, cannot start automation.")
            return
        }

        queue.clear()
        queue.addAll(apps)
        totalToClean = apps.size
        totalFreedBytes = 0L
        processedCount = 0

        Log.d(Constants.TAG, "[ACCESSIBILITY] Starting automation queue for $totalToClean apps.")
        processNextApp(context)
    }

    fun cancelCleaning() {
        clearTimeout()
        queue.clear()
        currentTarget = null
        currentStep = AutomationStep.IDLE
        _state.value = CacheCleanState.Cancelled
        Log.d(Constants.TAG, "[ACCESSIBILITY] Cleaning automation cancelled by user.")
    }

    fun resetState() {
        _state.value = CacheCleanState.Idle
    }

    private fun processNextApp(context: Context) {
        clearTimeout()
        val next = queue.poll()
        if (next == null) {
            // All apps processed! Finish and return to Optiva X
            currentStep = AutomationStep.IDLE
            currentTarget = null
            _state.value = CacheCleanState.Completed(
                totalFreedBytes = totalFreedBytes,
                cleanedCount = processedCount
            )
            bringOptivaToFront(context)
            return
        }

        currentTarget = next
        currentStep = AutomationStep.APP_DETAILS
        processedCount++

        _state.value = CacheCleanState.Running(
            currentApp = next,
            currentIndex = processedCount,
            totalCount = totalToClean,
            freedBytes = totalFreedBytes,
            statusMessage = "${next.appName} temizleniyor..."
        )

        Log.d(Constants.TAG, "[ACCESSIBILITY] Opening App Details for: ${next.packageName}")
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${next.packageName}")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(intent)

        // Safety timeout: 2500ms max per app. If unresponsive, advance to next
        timeoutRunnable = Runnable {
            Log.w(Constants.TAG, "[ACCESSIBILITY] Timeout reached for ${next.packageName}. Skipping to next.")
            processNextApp(context)
        }
        mainHandler.postDelayed(timeoutRunnable!!, 2500L)
    }

    /**
     * Handles accessibility window events emitted by Android Settings.
     */
    fun onAccessibilityEvent(context: Context, event: AccessibilityEvent, root: AccessibilityNodeInfo?) {
        if (root == null || currentTarget == null) return

        when (currentStep) {
            AutomationStep.APP_DETAILS -> {
                handleAppDetailsScreen(context, root)
            }
            AutomationStep.STORAGE_PAGE -> {
                handleStorageScreen(context, root)
            }
            AutomationStep.IDLE -> {}
        }
    }

    private fun handleAppDetailsScreen(context: Context, root: AccessibilityNodeInfo) {
        val storageNode = findStorageEntryNode(root)
        if (storageNode != null) {
            Log.d(Constants.TAG, "[ACCESSIBILITY] Found Storage entry node. Clicking...")
            currentStep = AutomationStep.STORAGE_PAGE
            storageNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
    }

    private fun handleStorageScreen(context: Context, root: AccessibilityNodeInfo) {
        val clearCacheNode = findClearCacheButtonNode(root)
        if (clearCacheNode != null) {
            Log.d(Constants.TAG, "[ACCESSIBILITY] Found Clear Cache button. Clickable: ${clearCacheNode.isClickable}, Enabled: ${clearCacheNode.isEnabled}")
            if (clearCacheNode.isEnabled) {
                clearCacheNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            currentTarget?.let {
                totalFreedBytes += it.cacheBytes
            }

            // Immediately schedule next app after short 150ms delay
            mainHandler.postDelayed({
                processNextApp(context)
            }, 180L)
        }
    }

    private fun findStorageEntryNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val text = node.text?.toString()?.lowercase() ?: ""
        val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
        val viewId = node.viewIdResourceName?.lowercase() ?: ""

        val isStorageText = (text.contains("depolama") || text.contains("storage") ||
                contentDesc.contains("depolama") || contentDesc.contains("storage")) &&
                !text.contains("temizle") && !text.contains("clear")

        val isStorageId = viewId.contains("storage_settings") || viewId.contains("storage")

        if ((isStorageText || isStorageId) && (node.isClickable || node.parent?.isClickable == true)) {
            return if (node.isClickable) node else node.parent
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findStorageEntryNode(child)
            if (found != null) return found
        }
        return null
    }

    private fun findClearCacheButtonNode(node: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        val text = node.text?.toString()?.lowercase() ?: ""
        val contentDesc = node.contentDescription?.toString()?.lowercase() ?: ""
        val viewId = node.viewIdResourceName?.lowercase() ?: ""

        // CRITICAL SAFETY FILTER: NEVER click "Clear data" / "Verileri temizle" / "Tüm verileri sil" / "Alanı yönet"
        val isDangerous = text.contains("veri") || text.contains("data") ||
                text.contains("alanı yönet") || text.contains("manage space") ||
                contentDesc.contains("veri") || contentDesc.contains("data")

        if (!isDangerous) {
            val hasCacheWord = text.contains("önbellek") || text.contains("cache") ||
                    contentDesc.contains("önbellek") || contentDesc.contains("cache")
            val hasClearWord = text.contains("temizle") || text.contains("clear") ||
                    contentDesc.contains("temizle") || contentDesc.contains("clear")

            val isCacheButton = (hasCacheWord && hasClearWord) ||
                    viewId.contains("clear_cache_button") ||
                    viewId.endsWith("button2")

            if (isCacheButton && (node.isClickable || node.parent?.isClickable == true)) {
                return if (node.isClickable) node else node.parent
            }
        }

        for (i in 0 until node.childCount) {
            val child = node.getChild(i) ?: continue
            val found = findClearCacheButtonNode(child)
            if (found != null) return found
        }
        return null
    }

    private fun clearTimeout() {
        timeoutRunnable?.let { mainHandler.removeCallbacks(it) }
        timeoutRunnable = null
    }

    private fun bringOptivaToFront(context: Context) {
        try {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            if (launchIntent != null) {
                context.startActivity(launchIntent)
            }
        } catch (e: Exception) {
            Log.e(Constants.TAG, "[ACCESSIBILITY] Error bringing Optiva to front: ${e.message}")
        }
    }
}
