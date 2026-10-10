package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ui.ChatMessage
import com.example.ui.RobotViewModel
import com.example.ui.components.ThinkingProcessCard
import com.example.ui.theme.RobotBackgroundDark
import com.example.ui.theme.RobotCardBorder
import com.example.ui.theme.RobotCyanPrimary
import com.example.ui.theme.RobotEmeraldSuccess
import com.example.ui.theme.RobotSurfaceDark
import com.example.ui.theme.RobotSurfaceVariantDark
import com.example.workflow.WorkflowState

@Composable
fun AssistantScreen(viewModel: RobotViewModel) {
    val messages by viewModel.chatMessages.collectAsState()
    val isThinking by viewModel.isAiThinking.collectAsState()
    val workflowState by viewModel.workflowState.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Real Android Speech Recognizer Result Launcher
    val speechRecognizerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spokenText.isNullOrBlank()) {
                inputText = spokenText
                viewModel.sendUserMessage(spokenText)
            }
        }
    }

    // Microphone Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak command to My AI Robot...")
            }
            try {
                speechRecognizerLauncher.launch(intent)
            } catch (e: Exception) {
                val fallback = "Start live human robot automation"
                inputText = fallback
                viewModel.sendUserMessage(fallback)
            }
        } else {
            val fallback = "Start live human robot automation"
            inputText = fallback
            viewModel.sendUserMessage(fallback)
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RobotBackgroundDark)
    ) {
        // Active Workflow Status Bar
        if (workflowState is WorkflowState.Running) {
            val running = workflowState as WorkflowState.Running
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C243B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, RobotCyanPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = RobotCyanPrimary,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Step ${running.stepIndex + 1}/10: ${running.stepTitle}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Automated execution in progress...",
                            fontSize = 11.sp,
                            color = RobotCyanPrimary
                        )
                    }
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatBubble(message = msg)
            }

            if (isThinking) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = RobotCyanPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "gemini-3.1-pro-preview reasoning with high thinking...",
                            fontSize = 12.sp,
                            color = RobotCyanPrimary
                        )
                    }
                }
            }
        }

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                SuggestionChip(
                    text = "🤖 Hands-Free Autopilot (No Typing)",
                    isPrimary = true,
                    onClick = {
                        viewModel.humanRobotEngine.startAutopilot(count = 3)
                    }
                )
            }
            item {
                SuggestionChip(
                    text = "🎙️ Voice: AliExpress -> Portals -> Pinterest",
                    isPrimary = true,
                    onClick = {
                        viewModel.sendUserMessage("Find trending AliExpress gadget, generate affiliate link in portal with target country, create Gemini 1000x1500 pin and post to Pinterest")
                    }
                )
            }
            item {
                SuggestionChip(
                    text = "🌐 Open All Sites in Chrome",
                    isPrimary = false,
                    onClick = {
                        viewModel.humanRobotEngine.openInChrome("https://www.aliexpress.com")
                    }
                )
            }
            item {
                SuggestionChip(
                    text = "🔥 10 Gadgets Batch Automation",
                    isPrimary = false,
                    onClick = {
                        viewModel.executeKitchenGadgetsWorkflow()
                    }
                )
            }
            item {
                SuggestionChip(
                    text = "🔍 Temu Affiliate Top Finds",
                    isPrimary = false,
                    onClick = {
                        viewModel.sendUserMessage("Research top viral Temu finds with high commission and create Pinterest pins")
                    }
                )
            }
        }

        // Input Bar
        Surface(
            color = RobotSurfaceDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Voice Command Mic button - Listens to speech hands-free!
                IconButton(
                    onClick = {
                        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(RobotCyanPrimary.copy(alpha = 0.2f))
                        .border(1.dp, RobotCyanPrimary, CircleShape)
                        .testTag("voice_command_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice Input - Speak your command",
                        tint = RobotCyanPrimary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text("Instruct My AI Robot...", color = Color(0xFF64748B), fontSize = 14.sp)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = RobotCyanPrimary,
                        unfocusedBorderColor = RobotCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            viewModel.sendUserMessage(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank(),
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (inputText.isNotBlank()) RobotCyanPrimary else RobotSurfaceVariantDark)
                        .testTag("send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = if (inputText.isNotBlank()) Color.Black else Color(0xFF64748B)
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.sender == "USER"

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 0.95f),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(RobotCyanPrimary.copy(alpha = 0.2f))
                        .border(1.dp, RobotCyanPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "Robot",
                        tint = RobotCyanPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Column {
                // Thinking process if present
                if (!message.thinkingThoughts.isNullOrBlank()) {
                    ThinkingProcessCard(thoughts = message.thinkingThoughts)
                    Spacer(modifier = Modifier.height(4.dp))
                }

                Surface(
                    color = if (isUser) Color(0xFF0369A1) else RobotSurfaceVariantDark,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isUser) RobotCyanPrimary.copy(alpha = 0.5f) else RobotCardBorder
                    )
                ) {
                    Text(
                        text = message.text,
                        color = Color.White,
                        fontSize = 13.5.sp,
                        modifier = Modifier.padding(12.dp),
                        lineHeight = 19.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SuggestionChip(
    text: String,
    isPrimary: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isPrimary) Color(0xFF0C243B) else RobotSurfaceVariantDark,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isPrimary) RobotCyanPrimary else RobotCardBorder
        ),
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("suggestion_chip")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = text,
                color = if (isPrimary) RobotCyanPrimary else Color(0xFFCBD5E1),
                fontSize = 11.5.sp,
                fontWeight = if (isPrimary) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
