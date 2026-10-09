package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.RobotViewModel
import com.example.ui.components.RobotHeader
import com.example.ui.screens.AssistantScreen
import com.example.ui.screens.BrowserScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MarketingScreen
import com.example.ui.screens.SecurityScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RobotBackgroundDark
import com.example.ui.theme.RobotCyanPrimary
import com.example.ui.theme.RobotSurfaceDark
import com.example.workflow.WorkflowState

enum class RobotScreen(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String
) {
    ASSISTANT("Assistant", Icons.Filled.SmartToy, Icons.Outlined.SmartToy, "nav_assistant"),
    DASHBOARD("Control", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_dashboard"),
    BROWSER("Browser", Icons.Filled.Language, Icons.Outlined.Language, "nav_browser"),
    MARKETING("Affiliate", Icons.Filled.Campaign, Icons.Outlined.Campaign, "nav_marketing"),
    SECURITY("Security", Icons.Filled.Security, Icons.Outlined.Security, "nav_security")
}

class MainActivity : ComponentActivity() {
    private val robotViewModel: RobotViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScaffold(viewModel = robotViewModel)
            }
        }
    }
}

@Composable
fun MainAppScaffold(viewModel: RobotViewModel) {
    var currentScreen by remember { mutableStateOf(RobotScreen.ASSISTANT) }
    val emergencyStopActive by viewModel.emergencyStopTriggered.collectAsState()
    val workflowState by viewModel.workflowState.collectAsState()

    val isRunning = workflowState is WorkflowState.Running

    BackHandler(enabled = currentScreen != RobotScreen.ASSISTANT) {
        currentScreen = RobotScreen.ASSISTANT
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(RobotBackgroundDark)
            .statusBarsPadding(),
        topBar = {
            RobotHeader(
                onEmergencyStop = { viewModel.emergencyStopAll() },
                emergencyStopActive = emergencyStopActive,
                isTaskRunning = isRunning
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                containerColor = RobotSurfaceDark,
                tonalElevation = 8.dp
            ) {
                RobotScreen.values().forEach { screen ->
                    val isSelected = currentScreen == screen
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentScreen = screen },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                                contentDescription = screen.title
                            )
                        },
                        label = {
                            Text(
                                text = screen.title,
                                fontSize = 10.5.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = RobotCyanPrimary,
                            selectedTextColor = RobotCyanPrimary,
                            unselectedIconColor = Color(0xFF64748B),
                            unselectedTextColor = Color(0xFF64748B),
                            indicatorColor = RobotCyanPrimary.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.testTag(screen.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                RobotScreen.ASSISTANT -> AssistantScreen(viewModel = viewModel)
                RobotScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                RobotScreen.BROWSER -> BrowserScreen(viewModel = viewModel)
                RobotScreen.MARKETING -> MarketingScreen(viewModel = viewModel)
                RobotScreen.SECURITY -> SecurityScreen(viewModel = viewModel)
            }
        }
    }
}
