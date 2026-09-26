package com.example.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent

/**
 * AccessibilityService that intercepts and consumes Home, Back, and Recent Apps system key events
 * during active Kendrīkaraṇa mode, redirecting them exclusively to the 'End Kendrīkaraṇa' action.
 *
 * It also monitors window state transitions to immediately intercept launch attempts of non-Urvarā apps.
 */
class EkagrataAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
        serviceInfo = info
    }

    /**
     * Intercepts and consumes Home, Back, and Recent Apps system key events
     * during active Kendrīkaraṇa mode, redirecting them exclusively to the 'End Kendrīkaraṇa' action.
     */
    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (KendrikaranaStateHolder.isKendrikaranaActive()) {
            val keyCode = event.keyCode
            val isHome = keyCode == KeyEvent.KEYCODE_HOME
            val isBack = keyCode == KeyEvent.KEYCODE_BACK
            val isRecentApps = keyCode == KeyEvent.KEYCODE_APP_SWITCH ||
                    keyCode == 187 ||
                    keyCode == KeyEvent.KEYCODE_MENU ||
                    keyCode == 82 ||
                    keyCode == KeyEvent.KEYCODE_WINDOW

            if (isHome || isBack || isRecentApps) {
                // Consume both DOWN and UP to prevent system navigation, and trigger End Kendrīkaraṇa
                if (event.action == KeyEvent.ACTION_DOWN) {
                    KendrikaranaStateHolder.triggerEndKendrikarana(applicationContext)
                }
                return true
            }
        }
        return super.onKeyEvent(event)
    }

    /**
     * Window state change listener that strictly blocks attempts to launch non-allowed apps
     * and immediately dismisses Recent Apps / Overview windows.
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!KendrikaranaStateHolder.isKendrikaranaActive()) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            val className = event.className?.toString() ?: ""
            val myPkg = applicationContext.packageName

            // Intercept Recent Apps (Overview / TaskView / RecentsActivity / Quickstep)
            val isRecentsWindow = className.contains("recents", ignoreCase = true) ||
                    className.contains("overview", ignoreCase = true) ||
                    className.contains("taskview", ignoreCase = true) ||
                    className.contains("quickstep", ignoreCase = true) ||
                    (packageName == "com.android.systemui" && className.contains("recents", ignoreCase = true))

            if (isRecentsWindow) {
                // Force close recents screen immediately and return to End Kendrīkaraṇa
                performGlobalAction(GLOBAL_ACTION_BACK)
                KendrikaranaStateHolder.triggerEndKendrikarana(applicationContext)
                return
            }

            // Whitelist Ekāgratā itself and system/keyboard/permission packages
            if (packageName == myPkg || KendrikaranaStateHolder.isSystemWhitelistedPackage(packageName)) {
                return
            }

            // If an allowed Urvarā app is currently in its launch transition, register its target window and allow it
            if (KendrikaranaStateHolder.isWithinLaunchGracePeriod()) {
                KendrikaranaStateHolder.notifyAllowedAppLaunched(packageName)
                return
            }

            // If app is not in the allowed Urvarā list, block it immediately!
            if (!KendrikaranaStateHolder.isPackageAllowed(packageName)) {
                performGlobalAction(GLOBAL_ACTION_HOME)
                KendrikaranaStateHolder.triggerEndKendrikarana(applicationContext, blockedPackageName = packageName)
            }
        }
    }

    override fun onInterrupt() {}
}
