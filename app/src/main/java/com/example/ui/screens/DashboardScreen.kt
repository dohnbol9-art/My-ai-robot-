package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.RobotViewModel
import com.example.ui.components.StatusBadge
import com.example.ui.theme.RobotAmberWarning
import com.example.ui.theme.RobotBackgroundDark
import com.example.ui.theme.RobotCardBorder
import com.example.ui.theme.RobotCyanPrimary
import com.example.ui.theme.RobotEmeraldSuccess
import com.example.ui.theme.RobotRoseError
import com.example.ui.theme.RobotSurfaceDark
import com.example.ui.theme.RobotSurfaceVariantDark
import com.example.workflow.WorkflowState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(viewModel: RobotViewModel) {
    val workflowState by viewModel.workflowState.collectAsState()
    val latestTask by viewModel.latestTask.collectAsState()
    val products by viewModel.allProducts.collectAsState()
    val pinDrafts by viewModel.allPinDrafts.collectAsState()
    val pendingPins by viewModel.pendingPins.collectAsState()
    val recentLogs by viewModel.recentLogs.collectAsState()
    val dailyLimit by viewModel.dailyPinLimit.collectAsState()
    val emergencyStopActive by viewModel.emergencyStopTriggered.collectAsState()

    val publishedPinsCount = pinDrafts.count { it.status == "PUBLISHED" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RobotBackgroundDark)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Emergency Stop Reset Alert
        if (emergencyStopActive) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF450A0A)),
                    border = BorderStroke(1.5.dp, RobotRoseError),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = RobotRoseError)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Emergency Stop Active. All tasks halted.",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = { viewModel.resetEmergencyStop() },
                            colors = ButtonDefaults.buttonColors(containerColor = RobotRoseError),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Reset", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // Approval Gate Action Banner
        if (workflowState is WorkflowState.PausedForApproval) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2838)),
                    border = BorderStroke(1.5.dp, RobotAmberWarning),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.HourglassBottom, contentDescription = null, tint = RobotAmberWarning)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Human Approval Required",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "10 Pinterest pin drafts generated with affiliate links. Verify items and approve publication to 'Kitchen Gadgets' board.",
                            color = Color(0xFFCBD5E1),
                            fontSize = 12.5.sp
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { viewModel.approveAndResumeCurrentTask() },
                                colors = ButtonDefaults.buttonColors(containerColor = RobotEmeraldSuccess),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("approve_and_publish_button")
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Approve & Publish to Pinterest", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { viewModel.stopWorkflow() },
                                border = BorderStroke(1.dp, RobotRoseError),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Reject", color = RobotRoseError)
                            }
                        }
                    }
                }
            }
        }

        // Active Workflow Execution Monitor
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RobotSurfaceDark),
                border = BorderStroke(1.dp, RobotCardBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "AUTOMATION PIPELINE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = RobotCyanPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = latestTask?.title ?: "No active task",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        StatusBadge(status = latestTask?.status ?: "IDLE")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val progress = when (val s = workflowState) {
                        is WorkflowState.Running -> s.progress
                        is WorkflowState.Completed -> 1.0f
                        is WorkflowState.PausedForApproval -> 0.85f
                        else -> 0f
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = RobotCyanPrimary,
                        trackColor = RobotSurfaceVariantDark
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = latestTask?.currentStepTitle ?: "Ready to execute",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RobotCyanPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Control Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.executeKitchenGadgetsWorkflow() },
                            colors = ButtonDefaults.buttonColors(containerColor = RobotCyanPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("start_workflow_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Run Pipeline", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }

                        OutlinedButton(
                            onClick = { viewModel.pauseWorkflow() },
                            border = BorderStroke(1.dp, RobotCardBorder),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = "Pause", tint = Color.White)
                        }

                        OutlinedButton(
                            onClick = { viewModel.stopWorkflow() },
                            border = BorderStroke(1.dp, RobotRoseError),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = RobotRoseError)
                        }
                    }
                }
            }
        }

        // Metrics Overview Grid
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricCard(
                    title = "Published Today",
                    value = "$publishedPinsCount / $dailyLimit",
                    sub = "Daily Limit",
                    color = RobotCyanPrimary,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Researched",
                    value = "${products.size}",
                    sub = "Products Found",
                    color = RobotEmeraldSuccess,
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    title = "Pin Drafts",
                    value = "${pinDrafts.size}",
                    sub = "${pendingPins.size} Pending",
                    color = RobotAmberWarning,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 10-Step Execution Pipeline Checklist
        item {
            Text(
                text = "10-Step Workflow Checklist",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        val stepList = listOf(
            "1. AI High-Thinking Plan Formulation",
            "2. Security & Service Authorization Audit",
            "3. Product Research (Temu & AliExpress)",
            "4. Specifications & Media Extraction",
            "5. Affiliate Tracking Link Generation",
            "6. Affiliate Link Verification & Compliance",
            "7. Render 1000x1500 Pinterest Marketing Creatives",
            "8. AI SEO Metadata, Keywords & Hashtags",
            "9. User Approval Gate Verification",
            "10. Pinterest Publishing & Verification"
        )

        val currentStepIdx = (workflowState as? WorkflowState.Running)?.stepIndex
            ?: if (workflowState is WorkflowState.Completed) 10 else if (workflowState is WorkflowState.PausedForApproval) 8 else -1

        items(stepList.indices.toList()) { idx ->
            val isDone = idx < currentStepIdx || workflowState is WorkflowState.Completed
            val isCurrent = idx == currentStepIdx && workflowState !is WorkflowState.Completed

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCurrent) Color(0xFF0F2338) else RobotSurfaceDark
                ),
                border = BorderStroke(
                    1.dp,
                    if (isCurrent) RobotCyanPrimary else RobotCardBorder
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isDone) RobotEmeraldSuccess.copy(alpha = 0.2f)
                                    else if (isCurrent) RobotCyanPrimary.copy(alpha = 0.2f)
                                    else RobotSurfaceVariantDark
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = RobotEmeraldSuccess, modifier = Modifier.size(14.dp))
                            } else if (isCurrent) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), color = RobotCyanPrimary, strokeWidth = 2.dp)
                            } else {
                                Text("${idx + 1}", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stepList[idx],
                            fontSize = 12.5.sp,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isDone) Color.White else if (isCurrent) RobotCyanPrimary else Color(0xFF94A3B8)
                        )
                    }

                    if (isDone) {
                        Text("DONE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RobotEmeraldSuccess)
                    } else if (isCurrent) {
                        Text("ACTIVE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = RobotCyanPrimary)
                    }
                }
            }
        }

        // Live Execution Logs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "System Execution Logs",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = { viewModel.clearHistory() }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Clear", tint = Color(0xFF94A3B8))
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF070B12)),
                border = BorderStroke(1.dp, RobotCardBorder),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp)
                ) {
                    if (recentLogs.isEmpty()) {
                        Text(
                            text = "No logs recorded yet. Run a workflow to view execution trace.",
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    } else {
                        recentLogs.take(15).forEach { log ->
                            val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
                            val color = when (log.level) {
                                "ERROR" -> RobotRoseError
                                "SUCCESS" -> RobotEmeraldSuccess
                                "SECURITY" -> RobotAmberWarning
                                else -> Color(0xFF94A3B8)
                            }
                            Text(
                                text = "[$timeStr] [${log.serviceTag}] ${log.message}",
                                fontSize = 10.5.sp,
                                fontFamily = FontFamily.Monospace,
                                color = color,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    sub: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = RobotSurfaceDark),
        border = BorderStroke(1.dp, RobotCardBorder),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 10.sp, color = Color(0xFF94A3B8))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = sub, fontSize = 9.sp, color = Color(0xFF64748B))
        }
    }
}
