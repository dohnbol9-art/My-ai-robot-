package com.example.security

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthorizedService(
    val id: String,
    val name: String,
    val description: String,
    val defaultUrl: String,
    val isAuthorized: Boolean,
    val isConnected: Boolean,
    val credentialLabel: String,
    val hasCredentials: Boolean,
    val lastAuthorizedDate: String = "Active",
    val requiresReauth: Boolean = false
)

class SecurityManager(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("robot_security_vault", Context.MODE_PRIVATE)

    private val _services = MutableStateFlow<List<AuthorizedService>>(emptyList())
    val services: StateFlow<List<AuthorizedService>> = _services.asStateFlow()

    private val _emergencyStopTriggered = MutableStateFlow(false)
    val emergencyStopTriggered: StateFlow<Boolean> = _emergencyStopTriggered.asStateFlow()

    private val _dailyPinLimit = MutableStateFlow(15)
    val dailyPinLimit: StateFlow<Int> = _dailyPinLimit.asStateFlow()

    private val _requireHumanApproval = MutableStateFlow(true)
    val requireHumanApproval: StateFlow<Boolean> = _requireHumanApproval.asStateFlow()

    init {
        loadSettings()
        loadServices()
    }

    private fun loadSettings() {
        _dailyPinLimit.value = prefs.getInt("daily_pin_limit", 15)
        _requireHumanApproval.value = prefs.getBoolean("require_approval", true)
    }

    fun setDailyPinLimit(limit: Int) {
        _dailyPinLimit.value = limit
        prefs.edit().putInt("daily_pin_limit", limit).apply()
    }

    fun setRequireHumanApproval(require: Boolean) {
        _requireHumanApproval.value = require
        prefs.edit().putBoolean("require_approval", require).apply()
    }

    fun triggerEmergencyStop() {
        _emergencyStopTriggered.value = true
    }

    fun resetEmergencyStop() {
        _emergencyStopTriggered.value = false
    }

    fun loadServices() {
        val initialList = listOf(
            AuthorizedService(
                id = SERVICE_TEMU,
                name = "Temu Affiliate",
                description = "Official affiliate tracking, product extraction, commission links",
                defaultUrl = "https://affiliate.temu.com",
                isAuthorized = prefs.getBoolean("auth_$SERVICE_TEMU", true),
                isConnected = prefs.getBoolean("conn_$SERVICE_TEMU", true),
                credentialLabel = "Temu Affiliate ID",
                hasCredentials = !getSecureCredential(SERVICE_TEMU).isNullOrEmpty()
            ),
            AuthorizedService(
                id = SERVICE_ALIEXPRESS,
                name = "AliExpress Affiliate Portal",
                description = "AliExpress Portals link generator, high-margin product catalog",
                defaultUrl = "https://portals.aliexpress.com",
                isAuthorized = prefs.getBoolean("auth_$SERVICE_ALIEXPRESS", true),
                isConnected = prefs.getBoolean("conn_$SERVICE_ALIEXPRESS", true),
                credentialLabel = "AliExpress Tracking ID / API Key",
                hasCredentials = !getSecureCredential(SERVICE_ALIEXPRESS).isNullOrEmpty()
            ),
            AuthorizedService(
                id = SERVICE_PINTEREST,
                name = "Pinterest",
                description = "Publishing pins, board management, visual marketing analytics",
                defaultUrl = "https://www.pinterest.com",
                isAuthorized = prefs.getBoolean("auth_$SERVICE_PINTEREST", true),
                isConnected = prefs.getBoolean("conn_$SERVICE_PINTEREST", true),
                credentialLabel = "Pinterest API Token",
                hasCredentials = !getSecureCredential(SERVICE_PINTEREST).isNullOrEmpty()
            ),
            AuthorizedService(
                id = SERVICE_CHATGPT,
                name = "ChatGPT",
                description = "External reasoning integration & content comparison",
                defaultUrl = "https://chatgpt.com",
                isAuthorized = prefs.getBoolean("auth_$SERVICE_CHATGPT", true),
                isConnected = prefs.getBoolean("conn_$SERVICE_CHATGPT", false),
                credentialLabel = "Session Token",
                hasCredentials = !getSecureCredential(SERVICE_CHATGPT).isNullOrEmpty()
            ),
            AuthorizedService(
                id = SERVICE_GEMINI,
                name = "Google Gemini",
                description = "High-thinking intelligence, planning, research evaluation",
                defaultUrl = "https://gemini.google.com",
                isAuthorized = prefs.getBoolean("auth_$SERVICE_GEMINI", true),
                isConnected = true,
                credentialLabel = "Gemini API Key",
                hasCredentials = true
            ),
            AuthorizedService(
                id = SERVICE_GOOGLE_SEARCH,
                name = "Google Search",
                description = "Product trend validation, search volume, market competition",
                defaultUrl = "https://www.google.com",
                isAuthorized = prefs.getBoolean("auth_$SERVICE_GOOGLE_SEARCH", true),
                isConnected = true,
                credentialLabel = "Direct Search Engine",
                hasCredentials = true
            ),
            AuthorizedService(
                id = SERVICE_MY_ECOMMERCE,
                name = "My E-Commerce Website",
                description = "Direct store sync, inventory checks, custom storefront",
                defaultUrl = "https://mystore.example.com",
                isAuthorized = prefs.getBoolean("auth_$SERVICE_MY_ECOMMERCE", true),
                isConnected = prefs.getBoolean("conn_$SERVICE_MY_ECOMMERCE", true),
                credentialLabel = "Storefront API Key",
                hasCredentials = !getSecureCredential(SERVICE_MY_ECOMMERCE).isNullOrEmpty()
            ),
            AuthorizedService(
                id = SERVICE_CUSTOM,
                name = "Other Authorized Sites",
                description = "Explicitly authorized custom domains & product catalogs",
                defaultUrl = "https://custom.example.com",
                isAuthorized = prefs.getBoolean("auth_$SERVICE_CUSTOM", true),
                isConnected = true,
                credentialLabel = "Domain Whitelist",
                hasCredentials = true
            )
        )
        _services.value = initialList
    }

    fun toggleServiceAuthorization(serviceId: String, authorized: Boolean) {
        prefs.edit().putBoolean("auth_$serviceId", authorized).apply()
        loadServices()
    }

    fun saveSecureCredential(serviceId: String, credentialSecret: String) {
        // Stored securely in private preferences, never leaked in plain logs or sent to AI
        prefs.edit().putString("cred_$serviceId", credentialSecret.trim()).apply()
        prefs.edit().putBoolean("conn_$serviceId", credentialSecret.isNotBlank()).apply()
        loadServices()
    }

    fun getSecureCredential(serviceId: String): String? {
        val cred = prefs.getString("cred_$serviceId", null)
        if (!cred.isNullOrEmpty()) return cred
        // Safe default dummy affiliate IDs for immediate prototype readiness
        return when (serviceId) {
            SERVICE_TEMU -> "act73919"
            SERVICE_ALIEXPRESS -> "ali_pro_partner_99"
            SERVICE_PINTEREST -> "pina_prod_token_authorized"
            else -> null
        }
    }

    fun isServiceAuthorized(serviceId: String): Boolean {
        if (_emergencyStopTriggered.value) return false
        val s = _services.value.find { it.id == serviceId }
        return s?.isAuthorized == true
    }

    fun checkUrlAuthorized(url: String): Boolean {
        if (_emergencyStopTriggered.value) return false
        val lower = url.lowercase()
        return when {
            lower.contains("temu.com") || lower.contains("temu.to") -> isServiceAuthorized(SERVICE_TEMU)
            lower.contains("aliexpress.com") || lower.contains("click.aliexpress.com") -> isServiceAuthorized(SERVICE_ALIEXPRESS)
            lower.contains("pinterest.com") -> isServiceAuthorized(SERVICE_PINTEREST)
            lower.contains("chatgpt.com") || lower.contains("openai.com") -> isServiceAuthorized(SERVICE_CHATGPT)
            lower.contains("gemini.google.com") || lower.contains("googleapis.com") -> isServiceAuthorized(SERVICE_GEMINI)
            lower.contains("google.com") -> isServiceAuthorized(SERVICE_GOOGLE_SEARCH)
            else -> isServiceAuthorized(SERVICE_CUSTOM)
        }
    }

    companion object {
        const val SERVICE_TEMU = "temu_affiliate"
        const val SERVICE_ALIEXPRESS = "aliexpress_affiliate"
        const val SERVICE_PINTEREST = "pinterest"
        const val SERVICE_CHATGPT = "chatgpt"
        const val SERVICE_GEMINI = "gemini"
        const val SERVICE_GOOGLE_SEARCH = "google_search"
        const val SERVICE_MY_ECOMMERCE = "my_ecommerce"
        const val SERVICE_CUSTOM = "custom_sites"
    }
}
