package com.example.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automation.MyRobotAccessibilityService
import com.example.security.AuthorizedService
import com.example.ui.RobotViewModel
import com.example.ui.theme.RobotAmberWarning
import com.example.ui.theme.RobotBackgroundDark
import com.example.ui.theme.RobotCardBorder
import com.example.ui.theme.RobotCyanPrimary
import com.example.ui.theme.RobotEmeraldSuccess
import com.example.ui.theme.RobotRoseError
import com.example.ui.theme.RobotSurfaceDark
import com.example.ui.theme.RobotSurfaceVariantDark

@Composable
fun SecurityScreen(viewModel: RobotViewModel) {
    val context = LocalContext.current
    val services by viewModel.services.collectAsState()
    val dailyLimit by viewModel.dailyPinLimit.collectAsState()
    val requireApproval by viewModel.requireHumanApproval.collectAsState()
    val emergencyStopActive by viewModel.emergencyStopTriggered.collectAsState()
    val isAccessibilityActive by MyRobotAccessibilityService.isConnected.collectAsState()
    val securityAlert by MyRobotAccessibilityService.securityAlert.collectAsState()

    var editingService by remember { mutableStateOf<AuthorizedService?>(null) }
    var credentialInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(RobotBackgroundDark)
            .padding(horizontal = 14.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Security Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RobotSurfaceDark),
                border = BorderStroke(1.dp, RobotCardBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = RobotCyanPrimary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "SECURITY & AUTHORIZATION ENGINE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RobotCyanPrimary,
                            letterSpacing = 1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Every website, portal, and Android automation step is strictly guarded. Passwords are never sent to AI models or stored in plaintext. If a CAPTCHA or 2FA gate appears, the robot pauses automatically.",
                        fontSize = 11.5.sp,
                        color = Color(0xFF94A3B8),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Accessibility Service Status
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAccessibilityActive) Color(0xFF0A2218) else Color(0xFF1E1710)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isAccessibilityActive) RobotEmeraldSuccess else RobotAmberWarning
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccessibilityNew,
                                contentDescription = null,
                                tint = if (isAccessibilityActive) RobotEmeraldSuccess else RobotAmberWarning,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Android Accessibility Service",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isAccessibilityActive) RobotEmeraldSuccess.copy(alpha = 0.2f)
                                    else RobotAmberWarning.copy(alpha = 0.2f)
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isAccessibilityActive) "CONNECTED" else "STANDBY",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAccessibilityActive) RobotEmeraldSuccess else RobotAmberWarning
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Allows authorized taps, scrolling, and navigation in supported apps. You can enable or disable this anytime in Android System Settings.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 11.5.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isAccessibilityActive) RobotSurfaceVariantDark else RobotCyanPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (isAccessibilityActive) "Open Settings" else "Enable in System Settings",
                            color = if (isAccessibilityActive) Color.White else Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Publishing Controls (Daily Limit & Human Approval)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = RobotSurfaceDark),
                border = BorderStroke(1.dp, RobotCardBorder),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Safety & Publishing Rules",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Human Approval Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Require Human Approval", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text(text = "Review pin drafts before publishing to Pinterest", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                        Switch(
                            checked = requireApproval,
                            onCheckedChange = { viewModel.setRequireApproval(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = RobotCyanPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Daily Publishing Limit Slider
                    Text(
                        text = "Daily Pinterest Publishing Limit: $dailyLimit pins/day",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Prevents spam flags by throttling automated pins",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )

                    var sliderValue by remember(dailyLimit) { mutableFloatStateOf(dailyLimit.toFloat()) }
                    Slider(
                        value = sliderValue,
                        onValueChange = { sliderValue = it },
                        onValueChangeFinished = { viewModel.setDailyLimit(sliderValue.toInt()) },
                        valueRange = 5f..50f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = RobotCyanPrimary,
                            activeTrackColor = RobotCyanPrimary,
                            inactiveTrackColor = RobotSurfaceVariantDark
                        )
                    )
                }
            }
        }

        // Individual Service Authorizations Header
        item {
            Text(
                text = "Authorized Services & Integrations (${services.size})",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        // 8 Authorized Services List
        items(services, key = { it.id }) { service ->
            ServiceAuthCard(
                service = service,
                onToggleAuth = { isAuth -> viewModel.toggleServiceAuth(service.id, isAuth) },
                onEditCredentials = {
                    editingService = service
                    credentialInput = viewModel.securityManager.getSecureCredential(service.id) ?: ""
                }
            )
        }
    }

    // Edit Credentials Modal Dialog
    editingService?.let { service ->
        AlertDialog(
            onDismissRequest = { editingService = null },
            containerColor = RobotSurfaceDark,
            title = {
                Text(
                    text = "Configure ${service.name}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = service.description,
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = service.credentialLabel,
                        color = RobotCyanPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = credentialInput,
                        onValueChange = { credentialInput = it },
                        placeholder = { Text("Enter ID / Key / Token", color = Color(0xFF64748B), fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = RobotCyanPrimary,
                            unfocusedBorderColor = RobotCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🔒 Encrypted in Android private app storage. Never sent to AI models.",
                        color = Color(0xFF10B981),
                        fontSize = 10.5.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveServiceCredential(service.id, credentialInput)
                        editingService = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RobotCyanPrimary)
                ) {
                    Text("Save Securely", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { editingService = null }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            }
        )
    }
}

@Composable
fun ServiceAuthCard(
    service: AuthorizedService,
    onToggleAuth: (Boolean) -> Unit,
    onEditCredentials: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = RobotSurfaceDark),
        border = BorderStroke(1.dp, RobotCardBorder),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = service.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                    Text(
                        text = service.defaultUrl,
                        color = RobotCyanPrimary,
                        fontSize = 10.5.sp
                    )
                }

                Switch(
                    checked = service.isAuthorized,
                    onCheckedChange = onToggleAuth,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.Black,
                        checkedTrackColor = RobotCyanPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = service.description,
                color = Color(0xFF94A3B8),
                fontSize = 11.5.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (service.isConnected) Icons.Default.Check else Icons.Default.Key,
                        contentDescription = null,
                        tint = if (service.isConnected) RobotEmeraldSuccess else Color(0xFF64748B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (service.isConnected) "Credentials Active" else "No Token Saved",
                        fontSize = 11.sp,
                        color = if (service.isConnected) RobotEmeraldSuccess else Color(0xFF64748B)
                    )
                }

                OutlinedButton(
                    onClick = onEditCredentials,
                    border = BorderStroke(1.dp, RobotCardBorder),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Configure", fontSize = 11.sp, color = Color.White)
                }
            }
        }
    }
}
