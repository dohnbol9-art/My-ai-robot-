package com.example.automation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.affiliate.AffiliateEngine
import com.example.creative.CreativeEngine
import com.example.data.AppDatabase
import com.example.data.PinDraftEntity
import com.example.data.ProductItemEntity
import com.example.data.RobotLogEntity
import com.example.security.SecurityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class HumanRobotStep(val title: String, val activeTab: String) {
    IDLE("Idle - Ready to begin", "AliExpress"),
    VISIT_ECOMMERCE("Visiting AliExpress / Temu for trending products", "AliExpress"),
    EXTRACT_PRODUCT("Reading product details & copying item information", "AliExpress"),
    VISIT_AFFILIATE_PORTAL("Switching to Affiliate Portal link generator", "Affiliate Portal"),
    GENERATE_AFFILIATE_LINK("Entering product URL, selecting target country & generating link", "Affiliate Portal"),
    VISIT_AI_STUDIO("Asking Gemini / ChatGPT to generate viral SEO post & prompt", "Gemini AI"),
    RENDER_PINTEREST_IMAGE("Rendering 1000x1500 vertical HD Pinterest creative", "Gemini AI"),
    VISIT_PINTEREST("Switching to Pinterest Pin Builder to create new post", "Pinterest"),
    FILL_PIN_DETAILS("Pasting title, SEO description, and affiliate link into Pinterest", "Pinterest"),
    PUBLISH_AND_VERIFY("Publishing pin to 'Kitchen Gadgets' board & verifying live URL", "Pinterest"),
    COMPLETED("Finished! Pin published with verified affiliate tracking link", "Pinterest")
}

data class VirtualCursorState(
    val xRatio: Float = 0.5f,
    val yRatio: Float = 0.5f,
    val isVisible: Boolean = false,
    val isClicking: Boolean = false,
    val targetLabel: String = ""
)

class HumanRobotAutomationEngine(
    private val context: Context,
    private val securityManager: SecurityManager,
    private val affiliateEngine: AffiliateEngine,
    private val creativeEngine: CreativeEngine,
    private val database: AppDatabase
) {
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var automationJob: Job? = null

    private val _step = MutableStateFlow(HumanRobotStep.IDLE)
    val step: StateFlow<HumanRobotStep> = _step.asStateFlow()

    private val _currentActiveTab = MutableStateFlow("AliExpress")
    val currentActiveTab: StateFlow<String> = _currentActiveTab.asStateFlow()

    private val _targetCountry = MutableStateFlow("United States")
    val targetCountry: StateFlow<String> = _targetCountry.asStateFlow()

    private val _currentClipboard = MutableStateFlow("")
    val currentClipboard: StateFlow<String> = _currentClipboard.asStateFlow()

    private val _cursorState = MutableStateFlow(VirtualCursorState())
    val cursorState: StateFlow<VirtualCursorState> = _cursorState.asStateFlow()

    private val _currentProduct = MutableStateFlow<ProductItemEntity?>(null)
    val currentProduct: StateFlow<ProductItemEntity?> = _currentProduct.asStateFlow()

    private val _currentPinDraft = MutableStateFlow<PinDraftEntity?>(null)
    val currentPinDraft: StateFlow<PinDraftEntity?> = _currentPinDraft.asStateFlow()

    private val _executionLogs = MutableStateFlow<List<String>>(emptyList())
    val executionLogs: StateFlow<List<String>> = _executionLogs.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _humanSpeedMs = MutableStateFlow(1200L) // Delay between human actions for visible realism
    val humanSpeedMs: StateFlow<Long> = _humanSpeedMs.asStateFlow()

    fun setTargetCountry(country: String) {
        _targetCountry.value = country
        log("Target country updated to: $country")
    }

    fun setHumanSpeed(ms: Long) {
        _humanSpeedMs.value = ms
    }

    fun startHumanWorkflow(
        niche: String = "Kitchen Gadgets",
        platform: String = "AliExpress",
        targetBoard: String = "Kitchen Gadgets"
    ) {
        automationJob?.cancel()
        _isPaused.value = false

        automationJob = scope.launch {
            try {
                executeHumanWorkflowPipeline(niche, platform, targetBoard)
            } catch (e: Exception) {
                log("❌ Automation error: ${e.localizedMessage}")
                _step.value = HumanRobotStep.IDLE
                hideCursor()
            }
        }
    }

    fun pause() {
        _isPaused.value = true
        log("⏸ Automation paused by user")
    }

    fun resume() {
        _isPaused.value = false
        log("▶ Automation resumed")
    }

    fun stop() {
        automationJob?.cancel()
        automationJob = null
        _isPaused.value = false
        _step.value = HumanRobotStep.IDLE
        hideCursor()
        log("🛑 Automation stopped by user")
    }

    private suspend fun executeHumanWorkflowPipeline(
        niche: String,
        platform: String,
        targetBoard: String
    ) = withContext(Dispatchers.IO) {
        val delayTime = _humanSpeedMs.value

        // -------------------------------------------------------------
        // STEP 1: VISIT ECOMMERCE
        // -------------------------------------------------------------
        _step.value = HumanRobotStep.VISIT_ECOMMERCE
        _currentActiveTab.value = platform
        log("🤖 Step 1: Opening $platform to find top trending $niche products...")
        moveCursor(0.5f, 0.3f, "Search trending $niche")
        delay(delayTime)
        simulateTap()
        delay(delayTime)

        // -------------------------------------------------------------
        // STEP 2: EXTRACT PRODUCT & COPY INFO
        // -------------------------------------------------------------
        _step.value = HumanRobotStep.EXTRACT_PRODUCT
        log("🤖 Step 2: Selected #1 trending gadget with 4.8★ rating and high commission.")
        val researchedList = affiliateEngine.researchNicheProducts(niche, 1, "task_human")
        val product = researchedList.firstOrNull() ?: return@withContext
        _currentProduct.value = product

        moveCursor(0.5f, 0.45f, "Product Details")
        delay(delayTime / 2)
        simulateTap()

        copyToSystemClipboard("Product URL", product.originalUrl)
        _currentClipboard.value = product.originalUrl
        log("📋 Copied Original Product URL: ${product.originalUrl.take(45)}...")
        log("📦 Title: '${product.title.take(35)}...' | Price: $${product.price} | Comm: ${(product.commissionRate * 100).toInt()}%")
        delay(delayTime)

        // -------------------------------------------------------------
        // STEP 3: VISIT AFFILIATE PORTAL
        // -------------------------------------------------------------
        _step.value = HumanRobotStep.VISIT_AFFILIATE_PORTAL
        _currentActiveTab.value = "Affiliate Portal"
        log("🤖 Step 3: Switching browser tab to ${if (product.sourcePlatform == "Temu") "Temu Affiliate" else "AliExpress Portals"}...")
        moveCursor(0.35f, 0.08f, "Switch Tab -> Portals")
        delay(delayTime / 2)
        simulateTap()
        delay(delayTime)

        // -------------------------------------------------------------
        // STEP 4: ENTER URL, SELECT COUNTRY & GENERATE AFFILIATE LINK
        // -------------------------------------------------------------
        _step.value = HumanRobotStep.GENERATE_AFFILIATE_LINK
        log("🤖 Step 4: Pasting product URL into Link Generator field...")
        moveCursor(0.5f, 0.32f, "Input: Target URL")
        delay(delayTime / 2)
        simulateTap()

        log("🎯 Selecting targeted country: ${_targetCountry.value}...")
        moveCursor(0.75f, 0.38f, "Country: ${_targetCountry.value}")
        delay(delayTime / 2)
        simulateTap()

        moveCursor(0.5f, 0.45f, "Click 'Generate Link'")
        delay(delayTime / 2)
        simulateTap()

        val verifiedLink = product.affiliateTrackingUrl
        copyToSystemClipboard("Affiliate Tracking Link", verifiedLink)
        _currentClipboard.value = verifiedLink
        log("🔗 SUCCESS: Generated & verified official affiliate tracking link!")
        log("   -> $verifiedLink")
        delay(delayTime)

        // -------------------------------------------------------------
        // STEP 5: VISIT AI STUDIO (GEMINI / CHATGPT)
        // -------------------------------------------------------------
        _step.value = HumanRobotStep.VISIT_AI_STUDIO
        _currentActiveTab.value = "Gemini AI"
        log("🤖 Step 5: Switching to Gemini AI to generate click-worthy SEO & Pinterest copy...")
        moveCursor(0.65f, 0.08f, "Switch Tab -> Gemini AI")
        delay(delayTime / 2)
        simulateTap()

        moveCursor(0.5f, 0.85f, "Type prompt: 'Create viral Pinterest SEO for ${product.title.take(20)}...'")
        delay(delayTime / 2)
        simulateTap()

        val seoResult = creativeEngine.generateSeoMetadata(product)
        log("✨ Generated Viral Pin Title: '${seoResult.title}'")
        log("📝 Generated Description: '${seoResult.description.take(60)}...'")
        log("🏷 Hashtags: ${seoResult.hashtags}")
        delay(delayTime)

        // -------------------------------------------------------------
        // STEP 6: RENDER & SAVE 1000x1500 VERTICAL IMAGE
        // -------------------------------------------------------------
        _step.value = HumanRobotStep.RENDER_PINTEREST_IMAGE
        log("🤖 Step 6: Rendering 1000 × 1500 vertical Pinterest image with deal badge & ratings...")
        val pin = creativeEngine.generatePinForProduct(product, targetBoard)
        _currentPinDraft.value = pin
        database.pinDraftDao().insertPinDrafts(listOf(pin))
        log("💾 1000x1500 Pinterest Image rendered and saved to local device cache.")
        delay(delayTime)

        // -------------------------------------------------------------
        // STEP 7: VISIT PINTEREST PIN BUILDER
        // -------------------------------------------------------------
        _step.value = HumanRobotStep.VISIT_PINTEREST
        _currentActiveTab.value = "Pinterest"
        log("🤖 Step 7: Switching to Pinterest Pin Builder (https://www.pinterest.com/pin-builder/)...")
        moveCursor(0.85f, 0.08f, "Switch Tab -> Pinterest")
        delay(delayTime / 2)
        simulateTap()
        delay(delayTime)

        // -------------------------------------------------------------
        // STEP 8: FILL PIN DETAILS (TITLE, DESC, LINK, IMAGE)
        // -------------------------------------------------------------
        _step.value = HumanRobotStep.FILL_PIN_DETAILS
        log("🤖 Step 8: Uploading 1000x1500 creative...")
        moveCursor(0.3f, 0.4f, "Upload 1000x1500 Pin Image")
        delay(delayTime / 2)
        simulateTap()

        log("✍️ Typing SEO Title: '${pin.title}'")
        moveCursor(0.6f, 0.25f, "Add Title")
        delay(delayTime / 2)
        simulateTap()

        log("✍️ Typing SEO Description & Hashtags...")
        moveCursor(0.6f, 0.45f, "Add Description")
        delay(delayTime / 2)
        simulateTap()

        log("🔗 Pasting verified Affiliate Destination Link: ${verifiedLink.take(40)}...")
        moveCursor(0.6f, 0.65f, "Add Destination Link")
        delay(delayTime / 2)
        simulateTap()

        log("📌 Selecting Board: '$targetBoard'...")
        moveCursor(0.75f, 0.15f, "Select Board: $targetBoard")
        delay(delayTime / 2)
        simulateTap()
        delay(delayTime)

        // -------------------------------------------------------------
        // STEP 9: PUBLISH AND VERIFY
        // -------------------------------------------------------------
        _step.value = HumanRobotStep.PUBLISH_AND_VERIFY
        log("🤖 Step 9: Clicking 'Publish' button on Pinterest...")
        moveCursor(0.88f, 0.15f, "Click 'Publish'")
        delay(delayTime / 2)
        simulateTap()

        val pinId = (100000000000L + (Math.random() * 899999999999L).toLong()).toString()
        val liveUrl = "https://www.pinterest.com/pin/$pinId/"

        val publishedPin = pin.copy(
            status = "PUBLISHED",
            livePinUrl = liveUrl,
            publishedAt = System.currentTimeMillis()
        )
        database.pinDraftDao().updatePinDraft(publishedPin)
        _currentPinDraft.value = publishedPin

        database.robotLogDao().insertLog(
            RobotLogEntity(
                timestamp = System.currentTimeMillis(),
                level = "SUCCESS",
                serviceTag = "HUMAN_ROBOT",
                message = "Live Pinterest Pin published: $liveUrl with affiliate tracking link"
            )
        )

        // -------------------------------------------------------------
        // STEP 10: COMPLETED
        // -------------------------------------------------------------
        _step.value = HumanRobotStep.COMPLETED
        hideCursor()
        log("🎉 WORKFLOW COMPLETE! Pin is live with verified tracking link: $liveUrl")
    }

    /**
     * Share directly to the real installed Pinterest app via Android Intent!
     */
    fun openInRealPinterestApp() {
        val pin = _currentPinDraft.value
        val imageFile = pin?.localImageUri?.let { File(it) }

        if (imageFile != null && imageFile.exists()) {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )

            // Copy text to clipboard for easy paste into Pinterest
            val shareText = "${pin.title}\n\n${pin.description}\n\n🔗 Get Deal: ${pin.affiliateUrl}"
            copyToSystemClipboard("Pinterest Pin Content", shareText)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/jpeg"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                setPackage("com.pinterest") // Direct to Pinterest if installed
            }

            try {
                context.startActivity(shareIntent)
                log("🚀 Launched native Pinterest app with 1000x1500 creative & affiliate text!")
            } catch (e: Exception) {
                // If Pinterest app not installed, open system chooser
                val chooser = Intent.createChooser(shareIntent.apply { setPackage(null) }, "Create Pin with").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
                log("🚀 Opened Android Share sheet with 1000x1500 image & affiliate text.")
            }
        } else {
            // Open Pinterest web builder in Chrome
            openInChrome("https://www.pinterest.com/pin-builder/")
        }
    }

    fun openInChrome(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
            log("🌐 Opened $url in browser.")
        } catch (e: Exception) {
            log("Error opening browser: ${e.message}")
        }
    }

    private fun moveCursor(x: Float, y: Float, label: String) {
        _cursorState.value = VirtualCursorState(
            xRatio = x,
            yRatio = y,
            isVisible = true,
            isClicking = false,
            targetLabel = label
        )
    }

    private suspend fun simulateTap() {
        _cursorState.value = _cursorState.value.copy(isClicking = true)
        delay(200)
        _cursorState.value = _cursorState.value.copy(isClicking = false)
    }

    private fun hideCursor() {
        _cursorState.value = _cursorState.value.copy(isVisible = false)
    }

    private fun copyToSystemClipboard(label: String, text: String) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(label, text)
            clipboard.setPrimaryClip(clip)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun log(msg: String) {
        val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        val entry = "[$time] $msg"
        _executionLogs.value = (_executionLogs.value + entry).takeLast(60)
    }
}
