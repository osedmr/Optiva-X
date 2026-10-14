package com.optivax.app.core.accessibility

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.optivax.app.core.Constants

/**
 * High-performance, lightweight [AccessibilityService] for Optiva X.
 * Automates clearing third-party application caches safely via Android Settings.
 */
class OptivaCacheCleanerService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(Constants.TAG, "[ACCESSIBILITY] OptivaCacheCleanerService connected and active.")
        CacheCleanAutomationManager.bindService(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!AccessibilityConsent.isAccepted(this)) return
        if (CacheCleanAutomationManager.state.value !is CacheCleanState.Running) return
        if (event.packageName?.toString() != "com.android.settings") return
        val root = rootInActiveWindow ?: return
        if (root.packageName?.toString() != "com.android.settings") return
        CacheCleanAutomationManager.onAccessibilityEvent(applicationContext, event, root)
    }

    override fun onInterrupt() {
        Log.w(Constants.TAG, "[ACCESSIBILITY] OptivaCacheCleanerService interrupted.")
        CacheCleanAutomationManager.cancelCleaning()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(Constants.TAG, "[ACCESSIBILITY] OptivaCacheCleanerService destroyed.")
        CacheCleanAutomationManager.unbindService()
    }
}
