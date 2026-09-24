package com.example.service

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Usage Statistics Manager based blocker that strictly blocks all launch attempts
 * of non-allowed apps during Kendrīkaraṇa mode, ensuring the only accessible apps
 * are those marked in the Urvarā section.
 */
class UsageStatsBlockerManager(private val context: Context) {

    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
    private var monitorJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    fun hasUsageStatsPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as? AppOpsManager ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageStatsSettings() {
        try {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            try {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            } catch (_: Exception) {}
        }
    }

    /**
     * Starts active monitoring loop during Kendrīkaraṇa session.
     * Continuously checks the current foreground task and forcefully terminates/blocks
     * any non-Urvarā application attempt.
     */
    fun startMonitoring() {
        stopMonitoring()
        monitorJob = scope.launch {
            var lastQueryTime = System.currentTimeMillis() - 5000L
            while (isActive && KendrikaranaStateHolder.isKendrikaranaActive()) {
                val currentTime = System.currentTimeMillis()
                checkAndBlockDisallowedApps(lastQueryTime, currentTime)
                lastQueryTime = currentTime
                delay(300) // Poll every 300ms for fast blocking response
            }
        }
    }

    fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
    }

    private fun checkAndBlockDisallowedApps(startTime: Long, endTime: Long) {
        val manager = usageStatsManager ?: return
        val events = manager.queryEvents(startTime, endTime)
        val event = UsageEvents.Event()
        var latestForegroundPackage: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                latestForegroundPackage = event.packageName
            }
        }

        // Fallback: If queryEvents did not report, inspect top app from recent usage stats
        if (latestForegroundPackage == null) {
            val stats = manager.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                startTime - 60000L,
                endTime
            )
            val topApp = stats?.maxByOrNull { it.lastTimeUsed }
            if (topApp != null && (endTime - topApp.lastTimeUsed) < 2000L) {
                latestForegroundPackage = topApp.packageName
            }
        }

        if (latestForegroundPackage != null) {
            val myPackage = context.packageName
            if (latestForegroundPackage != myPackage &&
                latestForegroundPackage != "com.android.systemui" &&
                !latestForegroundPackage.startsWith("com.android.inputmethod") &&
                !latestForegroundPackage.startsWith("com.google.android.inputmethod")
            ) {
                // If this is a non-Urvarā app, strictly block it!
                if (!KendrikaranaStateHolder.isPackageAllowed(latestForegroundPackage)) {
                    KendrikaranaStateHolder.triggerEndKendrikarana(context, latestForegroundPackage)
                }
            }
        }
    }
}
