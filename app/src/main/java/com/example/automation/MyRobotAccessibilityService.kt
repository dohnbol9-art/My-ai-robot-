package com.example.automation

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.os.Build
import android.os.Bundle
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MyRobotAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        _isConnected.value = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: ""
        if (pkg.isNotBlank()) {
            _currentPackage.value = pkg
        }

        // Security inspection: detect CAPTCHA, 2FA, biometric or lock challenges
        val textContent = event.text.joinToString(" ").lowercase()
        val className = event.className?.toString()?.lowercase() ?: ""
        if (textContent.contains("captcha") ||
            textContent.contains("verify you are human") ||
            textContent.contains("two-factor") ||
            textContent.contains("2-step verification") ||
            textContent.contains("security challenge") ||
            className.contains("keyguard")
        ) {
            _securityAlert.value = "Security challenge or authentication gate detected in $pkg. Pausing automation."
        }
    }

    override fun onInterrupt() {
        // Interrupted by system
    }

    override fun onDestroy() {
        super.onDestroy()
        if (instance == this) {
            instance = null
            _isConnected.value = false
        }
    }

    /**
     * Click a node containing specific text within authorized boundaries
     */
    fun clickText(targetText: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val nodes = root.findAccessibilityNodeInfosByText(targetText)
        for (node in nodes) {
            if (node.isClickable) {
                return node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            }
            // Try parent
            var parent = node.parent
            while (parent != null) {
                if (parent.isClickable) {
                    return parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                }
                parent = parent.parent
            }
        }
        return false
    }

    /**
     * Fill text in an active editable field
     */
    fun enterText(textToEnter: String): Boolean {
        val root = rootInActiveWindow ?: return false
        val focused = root.findFocus(AccessibilityNodeInfo.FOCUS_INPUT)
        if (focused != null && focused.isEditable) {
            val args = Bundle().apply {
                putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, textToEnter)
            }
            return focused.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
        }
        return false
    }

    /**
     * Safe scroll forward in active list
     */
    fun scrollForward(): Boolean {
        val root = rootInActiveWindow ?: return false
        return root.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD)
    }

    /**
     * Safe tap at coordinates
     */
    fun tapAt(x: Float, y: Float): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val path = Path().apply { moveTo(x, y) }
            val stroke = GestureDescription.StrokeDescription(path, 0, 100)
            val gesture = GestureDescription.Builder().addStroke(stroke).build()
            return dispatchGesture(gesture, null, null)
        }
        return false
    }

    companion object {
        var instance: MyRobotAccessibilityService? = null
            private set

        private val _isConnected = MutableStateFlow(false)
        val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

        private val _currentPackage = MutableStateFlow("com.example")
        val currentPackage: StateFlow<String> = _currentPackage.asStateFlow()

        private val _securityAlert = MutableStateFlow<String?>(null)
        val securityAlert: StateFlow<String?> = _securityAlert.asStateFlow()

        fun clearSecurityAlert() {
            _securityAlert.value = null
        }
    }
}
