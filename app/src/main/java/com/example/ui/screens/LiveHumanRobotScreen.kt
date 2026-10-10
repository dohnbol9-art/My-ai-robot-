package com.example.ui.screens

import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.automation.HumanRobotStep
import com.example.ui.RobotViewModel
import com.example.ui.theme.RobotAmberWarning
import com.example.ui.theme.RobotBackgroundDark
import com.example.ui.theme.RobotCardBorder
import com.example.ui.theme.RobotCyanPrimary
import com.example.ui.theme.RobotEmeraldSuccess
import com.example.ui.theme.RobotRoseError
import com.example.ui.theme.RobotSurfaceDark
import com.example.ui.theme.RobotSurfaceVariantDark

data class BrowserTab(
    val id: String,
    val title: String,
    val url: String,
    val badgeIcon: String
)

@Composable
fun LiveHumanRobotScreen(viewModel: RobotViewModel) {
    val context = LocalContext.current
    val humanEngine = viewModel.humanRobotEngine

    val currentStep by humanEngine.step.collectAsState()
    val activeTabName by humanEngine.currentActiveTab.collectAsState()
    val targetCountry by humanEngine.targetCountry.collectAsState()
    val clipboardText by humanEngine.currentClipboard.collectAsState()
    val cursorState by humanEngine.cursorState.collectAsState()
    val logs by humanEngine.executionLogs.collectAsState()
    val currentProduct by humanEngine.currentProduct.collectAsState()
    val currentPinDraft by humanEngine.currentPinDraft.collectAsState()
    val isAutopilotActive by humanEngine.isAutopilotActive.collectAsState()

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var showLogsDrawer by remember { mutableStateOf(false) }

    val tabs = remember {
        listOf(
            BrowserTab("AliExpress", "AliExpress", "https://www.aliexpress.com", "🔴"),
            BrowserTab("Temu", "Temu", "https://www.temu.com", "🟠"),
            BrowserTab("Affiliate Portal", "AliExpress Portals", "https://portals.aliexpress.com/link_generate.htm", "⚡"),
            BrowserTab("Gemini AI", "Gemini AI", "https://gemini.google.com", "🔵"),
            BrowserTab("Pinterest", "Pinterest Builder", "https://www.pinterest.com/pin-builder/", "📌")
        )
    }

    // Auto-navigate WebView when robot switches active tab
    LaunchedEffect(activeTabName) {
        val targetTab = tabs.find { it.id == activeTabName } ?: tabs.first()
        webViewInstance?.loadUrl(targetTab.url)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RobotBackgroundDark)
    ) {
        // Top Multi-Tab Bar (Live Browser Switcher)
        Surface(
            color = RobotSurfaceDark,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    items(tabs, key = { it.id }) { tab ->
                        val isActive = tab.id == activeTabName
                        val isRobotAutomatingThis = (currentStep != HumanRobotStep.IDLE && currentStep != HumanRobotStep.COMPLETED) && isActive

                        Surface(
                            color = if (isActive) Color(0xFF0F2642) else RobotSurfaceVariantDark,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(
                                if (isRobotAutomatingThis) 2.dp else 1.dp,
                                if (isRobotAutomatingThis) RobotCyanPrimary else if (isActive) RobotCyanPrimary.copy(alpha = 0.5f) else RobotCardBorder
                            ),
                            modifier = Modifier.clickable {
                                webViewInstance?.loadUrl(tab.url)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = tab.badgeIcon, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = tab.title,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isActive) Color.White else Color(0xFF94A3B8)
                                )
                                if (isRobotAutomatingThis) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(RobotEmeraldSuccess)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Human Working HUD Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B192C)),
            border = BorderStroke(1.dp, RobotCyanPrimary.copy(alpha = 0.6f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                // Header line with status indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(RobotCyanPrimary.copy(alpha = 0.2f))
                                .border(1.dp, RobotCyanPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SmartToy, contentDescription = null, tint = RobotCyanPrimary, modifier = Modifier.size(15.dp))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE HUMAN ROBOT AGENT",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = RobotCyanPrimary,
                            letterSpacing = 0.5.sp
                        )
                    }

                    // Target Country Selector
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(RobotSurfaceVariantDark)
                            .clickable {
                                val next = when (targetCountry) {
                                    "United States" -> "United Kingdom"
                                    "United Kingdom" -> "Canada"
                                    "Canada" -> "Australia"
                                    "Australia" -> "Germany"
                                    else -> "United States"
                                }
                                humanEngine.setTargetCountry(next)
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = when (targetCountry) {
                                "United States" -> "🇺🇸 US"
                                "United Kingdom" -> "🇬🇧 UK"
                                "Canada" -> "🇨🇦 CA"
                                "Australia" -> "🇦🇺 AU"
                                else -> "🇩🇪 DE"
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Current Action Description
                Text(
                    text = currentStep.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                // Live Clipboard Row
                if (clipboardText.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF040A14))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = RobotEmeraldSuccess, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Clipboard: $clipboardText",
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFA7F3D0),
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep == HumanRobotStep.IDLE || currentStep == HumanRobotStep.COMPLETED) {
                        Button(
                            onClick = { humanEngine.startHumanWorkflow() },
                            colors = ButtonDefaults.buttonColors(containerColor = RobotCyanPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("start_human_robot_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Run Live", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }

                        Button(
                            onClick = { humanEngine.startAutopilot(count = 3) },
                            colors = ButtonDefaults.buttonColors(containerColor = RobotEmeraldSuccess),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("start_autopilot_button")
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.Black, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("Autopilot 3x", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }
                    } else {
                        Button(
                            onClick = { humanEngine.stop() },
                            colors = ButtonDefaults.buttonColors(containerColor = RobotRoseError),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop Robot", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                        }
                    }

                    // Native Pinterest App Intent launcher
                    OutlinedButton(
                        onClick = { humanEngine.openInRealPinterestApp() },
                        border = BorderStroke(1.dp, Color(0xFFE11D48)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("📌 Pinterest", color = Color(0xFFFB7185), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }

                    // Native Chrome browser launcher
                    IconButton(
                        onClick = {
                            val activeTab = tabs.find { it.id == activeTabName } ?: tabs.first()
                            humanEngine.openInChrome(activeTab.url)
                        },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = "Chrome", tint = Color(0xFF94A3B8))
                    }
                }
            }
        }

        // Live Smartphone Viewport Container with Animated Touch Cursor
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            val containerWidth = maxWidth
            val containerHeight = maxHeight

            // 1. Embedded Real Interactive Android WebView
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            loadsImagesAutomatically = true
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            userAgentString = "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36 MyAIRobot/1.0"
                        }
                        CookieManager.getInstance().setAcceptCookie(true)
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                            }
                        }
                        loadUrl(tabs.first().url)
                        webViewInstance = this
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // 2. Animated Glowing Virtual Human Touch Cursor
            if (cursorState.isVisible) {
                val animatedX by animateFloatAsState(
                    targetValue = cursorState.xRatio,
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                    label = "cursorX"
                )
                val animatedY by animateFloatAsState(
                    targetValue = cursorState.yRatio,
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
                    label = "cursorY"
                )

                val pulseTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by pulseTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.35f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulseScale"
                )

                val xPx = (containerWidth * animatedX)
                val yPx = (containerHeight * animatedY)

                Box(
                    modifier = Modifier
                        .offset(x = xPx - 20.dp, y = yPx - 20.dp)
                        .size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Outer Ripple
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .scale(if (cursorState.isClicking) 0.85f else pulseScale)
                            .clip(CircleShape)
                            .background(RobotCyanPrimary.copy(alpha = if (cursorState.isClicking) 0.6f else 0.25f))
                    )
                    // Inner Center Touch Dot
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (cursorState.isClicking) RobotEmeraldSuccess else RobotCyanPrimary)
                            .border(2.dp, Color.White, CircleShape)
                    )
                    // Touch action label tag
                    if (cursorState.targetLabel.isNotBlank()) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(4.dp),
                            border = BorderStroke(1.dp, RobotCyanPrimary),
                            modifier = Modifier.offset(y = 28.dp)
                        ) {
                            Text(
                                text = cursorState.targetLabel,
                                fontSize = 9.sp,
                                color = RobotCyanPrimary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Collapsible Terminal Activity Logs
        Surface(
            color = Color(0xFF060D17),
            border = BorderStroke(1.dp, RobotCardBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showLogsDrawer = !showLogsDrawer },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "📟", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Robot Activity Terminal (${logs.size} actions)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF94A3B8)
                        )
                    }
                    Icon(
                        imageVector = if (showLogsDrawer) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(18.dp)
                    )
                }

                AnimatedVisibility(visible = showLogsDrawer) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .padding(top = 4.dp)
                    ) {
                        items(logs.reversed()) { logEntry ->
                            Text(
                                text = logEntry,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (logEntry.contains("SUCCESS") || logEntry.contains("COMPLETE")) RobotEmeraldSuccess else Color(0xFFCBD5E1),
                                lineHeight = 14.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }
        }
    }
}
