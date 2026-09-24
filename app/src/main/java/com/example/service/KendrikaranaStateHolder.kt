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

    private val endKendrikaranaListeners = CopyOnWriteArrayList<() -> Unit>()

    fun setSessionActive(isActive: Boolean, allowedPackageNames: Set<String> = emptySet()) {
        active = isActive
        allowedPackages = allowedPackageNames
    }

    fun isKendrikaranaActive(): Boolean = active

    fun isPackageAllowed(packageName: String): Boolean {
        if (!active) return true
        return allowedPackages.contains(packageName)
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
