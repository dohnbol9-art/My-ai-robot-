package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.affiliate.AffiliateEngine
import com.example.ai.GeminiClient
import com.example.automation.BrowserAutomationEngine
import com.example.automation.MyRobotAccessibilityService
import com.example.creative.CreativeEngine
import com.example.data.AppDatabase
import com.example.data.PinDraftEntity
import com.example.data.ProductItemEntity
import com.example.data.RobotLogEntity
import com.example.data.TaskEntity
import com.example.data.TaskStepEntity
import com.example.pinterest.PinterestManager
import com.example.security.AuthorizedService
import com.example.security.SecurityManager
import com.example.workflow.WorkflowEngine
import com.example.workflow.WorkflowState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String,
    val sender: String, // "USER" or "ROBOT"
    val text: String,
    val thinkingThoughts: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val actionSuggested: String? = null
)

class RobotViewModel(application: Application) : AndroidViewModel(application) {
    val database = AppDatabase.getDatabase(application)
    val securityManager = SecurityManager(application)
    val geminiClient = GeminiClient()
    val affiliateEngine = AffiliateEngine(securityManager)
    val creativeEngine = CreativeEngine(application)
    val pinterestManager = PinterestManager(database.pinDraftDao(), securityManager)
    val browserEngine = BrowserAutomationEngine(application, securityManager)

    val workflowEngine = WorkflowEngine(
        context = application,
        database = database,
        geminiClient = geminiClient,
        securityManager = securityManager,
        affiliateEngine = affiliateEngine,
        creativeEngine = creativeEngine,
        pinterestManager = pinterestManager
    )

    // State Flows from Database
    val allTasks: StateFlow<List<TaskEntity>> = database.taskDao().getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestTask: StateFlow<TaskEntity?> = database.taskDao().getLatestTask()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allProducts: StateFlow<List<ProductItemEntity>> = database.productDao().getAllProducts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPinDrafts: StateFlow<List<PinDraftEntity>> = database.pinDraftDao().getAllPinDrafts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingPins: StateFlow<List<PinDraftEntity>> = database.pinDraftDao().getPendingApprovalPins()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentLogs: StateFlow<List<RobotLogEntity>> = database.robotLogDao().getRecentLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Security & Services State
    val services: StateFlow<List<AuthorizedService>> = securityManager.services
    val emergencyStopTriggered: StateFlow<Boolean> = securityManager.emergencyStopTriggered
    val dailyPinLimit: StateFlow<Int> = securityManager.dailyPinLimit
    val requireHumanApproval: StateFlow<Boolean> = securityManager.requireHumanApproval

    // Workflow State
    val workflowState: StateFlow<WorkflowState> = workflowEngine.workflowState

    // Chat Messages
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "init_1",
                sender = "ROBOT",
                text = "Hello! I am My AI Robot, your personal assistant. I am ready to research products on Temu & AliExpress, generate verified affiliate links, create 1000×1500 Pinterest marketing creatives, and publish pins to your boards. You are in full control.",
                thinkingThoughts = "Initialized with gemini-3.1-pro-preview (ThinkingLevel.HIGH). Security perimeter verified. 8 authorized service integrations ready."
            )
        )
    )
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    fun sendUserMessage(text: String) {
        if (text.isBlank()) return

        val userMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            sender = "USER",
            text = text.trim()
        )
        _chatMessages.value = _chatMessages.value + userMsg

        // Check if user is invoking the kitchen gadgets workflow
        val lower = text.lowercase()
        if (lower.contains("kitchen") && (lower.contains("find") || lower.contains("publish") || lower.contains("temu") || lower.contains("aliexpress"))) {
            executeKitchenGadgetsWorkflow(text)
            return
        }

        // Otherwise perform general AI reasoning with gemini-3.1-pro-preview
        viewModelScope.launch(Dispatchers.IO) {
            _isAiThinking.value = true
            val aiResult = geminiClient.analyzeWithHighThinking(text)
            _isAiThinking.value = false

            val robotMsg = ChatMessage(
                id = "robot_${System.currentTimeMillis()}",
                sender = "ROBOT",
                text = aiResult.responseText,
                thinkingThoughts = aiResult.reasoningThoughts
            )
            _chatMessages.value = _chatMessages.value + robotMsg
        }
    }

    fun executeKitchenGadgetsWorkflow(
        prompt: String = "Find 10 kitchen gadgets on Temu and AliExpress, generate their affiliate links, create Pinterest images and SEO descriptions, and publish them to my kitchen gadgets board."
    ) {
        viewModelScope.launch {
            val robotAck = ChatMessage(
                id = "robot_ack_${System.currentTimeMillis()}",
                sender = "ROBOT",
                text = "Understood! Initiating the complete 10-step automated workflow for Kitchen Gadgets across Temu & AliExpress. You can follow real-time progress on the Control Center or pause at any time.",
                thinkingThoughts = "🧠 [gemini-3.1-pro-preview (HIGH THINKING)]\nStarting end-to-end execution pipeline. Target: 10 kitchen gadgets (5 Temu + 5 AliExpress) -> Verified affiliate tracking links -> 1000x1500 vertical pin graphics -> SEO descriptions -> Pinterest Board 'Kitchen Gadgets'."
            )
            _chatMessages.value = _chatMessages.value + robotAck

            workflowEngine.startWorkflow(
                userPrompt = prompt,
                niche = "Kitchen Gadgets",
                productCount = 10,
                targetBoard = "Kitchen Gadgets"
            )
        }
    }

    fun approveAndResumeCurrentTask() {
        val currentTaskId = workflowEngine.currentTaskId.value ?: return
        viewModelScope.launch {
            workflowEngine.resumeAfterApproval(currentTaskId)
        }
    }

    fun pauseWorkflow() = workflowEngine.pause()
    fun resumeWorkflow() = workflowEngine.resume()
    fun stopWorkflow() = workflowEngine.stop()
    fun emergencyStopAll() = workflowEngine.emergencyStopAll()

    fun resetEmergencyStop() = securityManager.resetEmergencyStop()

    fun toggleServiceAuth(serviceId: String, isAuthorized: Boolean) {
        securityManager.toggleServiceAuthorization(serviceId, isAuthorized)
    }

    fun saveServiceCredential(serviceId: String, secret: String) {
        securityManager.saveSecureCredential(serviceId, secret)
    }

    fun setDailyLimit(limit: Int) {
        securityManager.setDailyPinLimit(limit)
    }

    fun setRequireApproval(require: Boolean) {
        securityManager.setRequireHumanApproval(require)
    }

    fun approveSinglePin(pinId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            database.pinDraftDao().updatePinStatus(pinId, "APPROVED")
        }
    }

    fun publishSinglePin(pin: PinDraftEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            pinterestManager.publishPin(pin)
        }
    }

    fun clearHistory() {
        viewModelScope.launch(Dispatchers.IO) {
            database.taskDao().clearTasks()
            database.productDao().clearProducts()
            database.pinDraftDao().clearPinDrafts()
            database.robotLogDao().clearLogs()
        }
    }
}
