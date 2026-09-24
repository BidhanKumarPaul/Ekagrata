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
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOWS_CHANGED
            flags = AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 50
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
            val isRecentApps = keyCode == KeyEvent.KEYCODE_APP_SWITCH || keyCode == 187

            if (isHome || isBack || isRecentApps) {
                // When key is released, trigger the redirection to 'End Kendrīkaraṇa'
                if (event.action == KeyEvent.ACTION_UP) {
                    KendrikaranaStateHolder.triggerEndKendrikarana(applicationContext)
                }
                // Return true to consume the key event and prevent system navigation
                return true
            }
        }
        return super.onKeyEvent(event)
    }

    /**
     * Window state change listener that strictly blocks attempts to launch non-allowed apps.
     */
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (!KendrikaranaStateHolder.isKendrikaranaActive()) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: return
            val myPkg = applicationContext.packageName

            // Whitelist Ekāgratā itself, system UI, and active input method keyboards
            if (packageName == myPkg ||
                packageName == "com.android.systemui" ||
                packageName.startsWith("com.android.inputmethod") ||
                packageName.startsWith("com.google.android.inputmethod")
            ) {
                return
            }

            // If app is not in the allowed Urvarā list, block it immediately!
            if (!KendrikaranaStateHolder.isPackageAllowed(packageName)) {
                KendrikaranaStateHolder.triggerEndKendrikarana(applicationContext, blockedPackageName = packageName)
            }
        }
    }

    override fun onInterrupt() {}
}
