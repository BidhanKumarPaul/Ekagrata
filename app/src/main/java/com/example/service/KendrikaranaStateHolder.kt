package com.example.service

import android.content.Context
import android.content.Intent
import com.example.MainActivity
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Thread-safe singleton bridge between the UI/ViewModel and system background services
 * (EkagrataAccessibilityService and UsageStatsBlockerManager).
 */
object KendrikaranaStateHolder {

    @Volatile
    private var active: Boolean = false

    @Volatile
    private var allowedPackages: Set<String> = emptySet()

    @Volatile
    private var currentlyInAllowedApp: Boolean = false

    @Volatile
    private var lastAllowedLaunchTimestamp: Long = 0L

    private val endKendrikaranaListeners = CopyOnWriteArrayList<() -> Unit>()

    fun setSessionActive(isActive: Boolean, allowedPackageNames: Set<String> = emptySet()) {
        active = isActive
        allowedPackages = allowedPackageNames
        if (!isActive) {
            currentlyInAllowedApp = false
            lastAllowedLaunchTimestamp = 0L
        }
    }

    fun updateAllowedPackages(allowedPackageNames: Set<String>) {
        allowedPackages = allowedPackages + allowedPackageNames
    }

    fun notifyAllowedAppLaunched(packageName: String) {
        if (packageName.isNotBlank()) {
            allowedPackages = allowedPackages + packageName
        }
        currentlyInAllowedApp = true
        lastAllowedLaunchTimestamp = System.currentTimeMillis()
    }

    fun isInAllowedAppSession(): Boolean = active && currentlyInAllowedApp

    fun isWithinLaunchGracePeriod(graceMs: Long = 2500L): Boolean {
        if (!active || lastAllowedLaunchTimestamp == 0L) return false
        return (System.currentTimeMillis() - lastAllowedLaunchTimestamp) in 0..graceMs
    }

    fun onReturnedToLauncher() {
        if (!isWithinLaunchGracePeriod(1200L)) {
            currentlyInAllowedApp = false
        }
    }

    fun isKendrikaranaActive(): Boolean = active

    fun isSystemWhitelistedPackage(packageName: String): Boolean {
        val p = packageName.lowercase()
        return p == "android" ||
                p == "com.android.systemui" ||
                p == "com.android.intentresolver" ||
                p.contains("permissioncontroller") ||
                p.contains("packageinstaller") ||
                p.contains("inputmethod") ||
                p.contains("keyboard") ||
                p.startsWith("com.samsung.android.honeyboard")
    }

    fun isPackageAllowed(packageName: String): Boolean {
        if (!active) return true
        if (isSystemWhitelistedPackage(packageName)) return true
        if (allowedPackages.contains(packageName)) return true
        return false
    }

    fun addEndKendrikaranaListener(listener: () -> Unit) {
        endKendrikaranaListeners.add(listener)
    }

    fun removeEndKendrikaranaListener(listener: () -> Unit) {
        endKendrikaranaListeners.remove(listener)
    }

    /**
     * Redirects exclusively to the 'End Kendrīkaraṇa' action.
     * Consumes system navigation and brings MainActivity forward to prompt the session exit confirmation.
     */
    fun triggerEndKendrikarana(context: Context, blockedPackageName: String? = null) {
        currentlyInAllowedApp = false
        lastAllowedLaunchTimestamp = 0L
        endKendrikaranaListeners.forEach { it.invoke() }

        val intent = Intent(context, MainActivity::class.java).apply {
            action = MainActivity.ACTION_END_KENDRIKARANA
            if (blockedPackageName != null) {
                putExtra(MainActivity.EXTRA_BLOCKED_PACKAGE, blockedPackageName)
            }
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {}
    }
}
