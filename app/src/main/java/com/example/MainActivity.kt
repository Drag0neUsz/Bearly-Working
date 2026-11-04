package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QuotesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.*
import com.example.ui.viewmodel.SavageViewModel

enum class MainDestination(val title: String) {
    SCHEDULE("SCHEDULE"),
    CALENDAR("CALENDAR"),
    ROASTS("ROASTS"),
    SETTINGS("SETTINGS")
}

class MainActivity : ComponentActivity() {

    private val viewModel: SavageViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            )
        )

        setContent {
            SavageBearTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                var currentTab by remember { mutableStateOf(MainDestination.SCHEDULE) }
                val snackbarHostState = remember { SnackbarHostState() }

                // Calendar permission launcher
                val calendarLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    val notificationGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    } else true
                    viewModel.updatePermissions(isGranted, notificationGranted)
                }

                // Notification permission launcher
                val notificationLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    val calendarGranted = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.READ_CALENDAR
                    ) == PackageManager.PERMISSION_GRANTED
                    viewModel.updatePermissions(calendarGranted, isGranted)
                }

                // Initial permission check on launch
                LaunchedEffect(Unit) {
                    val calGranted = ContextCompat.checkSelfPermission(
                        this@MainActivity,
                        Manifest.permission.READ_CALENDAR
                    ) == PackageManager.PERMISSION_GRANTED

                    val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                    } else true

                    viewModel.updatePermissions(calGranted, notifGranted)

                    if (!calGranted) {
                        calendarLauncher.launch(Manifest.permission.READ_CALENDAR)
                    }
                    if (!notifGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                LaunchedEffect(uiState.statusMessage) {
                    uiState.statusMessage?.let { msg ->
                        snackbarHostState.showSnackbar(msg)
                        viewModel.clearStatusMessage()
                    }
                }

                if (currentTab != MainDestination.SCHEDULE) {
                    BackHandler {
                        currentTab = MainDestination.SCHEDULE
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = SoftCanvasBg,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        // Modular dark status bar strip that automatically fits any device's status bar / cutout height
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SoftNavy)
                                .windowInsetsTopHeight(WindowInsets.statusBars)
                                .testTag("status_bar_dark_strip")
                        )
                    },
                    bottomBar = {
                        // Tactile Bottom Navigation Bar
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SoftSurfaceWhite)
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("bottom_nav_bar")
                        ) {
                            HorizontalDivider(thickness = 2.dp, color = SoftBorder)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BottomNavTabItem(
                                    label = "TODAY",
                                    isSelected = currentTab == MainDestination.SCHEDULE,
                                    activeIcon = Icons.Filled.CheckCircle,
                                    inactiveIcon = Icons.Outlined.CheckCircle,
                                    onClick = { currentTab = MainDestination.SCHEDULE },
                                    testTag = "nav_schedule",
                                    modifier = Modifier.weight(1f)
                                )

                                BottomNavTabItem(
                                    label = "CALENDAR",
                                    isSelected = currentTab == MainDestination.CALENDAR,
                                    activeIcon = Icons.Filled.CalendarMonth,
                                    inactiveIcon = Icons.Outlined.CalendarMonth,
                                    onClick = { currentTab = MainDestination.CALENDAR },
                                    testTag = "nav_calendar",
                                    modifier = Modifier.weight(1f)
                                )

                                BottomNavTabItem(
                                    label = "ROASTS",
                                    isSelected = currentTab == MainDestination.ROASTS,
                                    activeIcon = Icons.Filled.FormatQuote,
                                    inactiveIcon = Icons.Outlined.FormatQuote,
                                    onClick = { currentTab = MainDestination.ROASTS },
                                    testTag = "nav_roasts",
                                    modifier = Modifier.weight(1f)
                                )

                                BottomNavTabItem(
                                    label = "SETTINGS",
                                    isSelected = currentTab == MainDestination.SETTINGS,
                                    activeIcon = Icons.Filled.Settings,
                                    inactiveIcon = Icons.Outlined.Settings,
                                    onClick = { currentTab = MainDestination.SETTINGS },
                                    testTag = "nav_settings",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Crossfade(
                        targetState = currentTab,
                        label = "screenTransition",
                        modifier = Modifier
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                    ) { destination ->
                        when (destination) {
                            MainDestination.SCHEDULE -> {
                                HomeScreen(
                                    uiState = uiState,
                                    onRequestCalendarPermission = {
                                        calendarLauncher.launch(Manifest.permission.READ_CALENDAR)
                                    },
                                    onRequestNotificationPermission = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    },
                                    onToggleCompleted = { viewModel.toggleCompletion(it) },
                                    onDeleteItem = { viewModel.deleteItem(it) },
                                    onPokeBear = { viewModel.pokeBear() },
                                    onTriggerSecretRapidTap = { viewModel.triggerSecretRapidTap() },
                                    onSetFilter = { viewModel.setFilter(it) },
                                    onAddTask = { title, isAllDay, hour, minute ->
                                        viewModel.addQuickTask(title, isAllDay, hour, minute)
                                    },
                                    onEditTask = { item, title, isAllDay, hour, minute ->
                                        viewModel.updateTask(item, title, isAllDay, hour, minute)
                                    }
                                )
                            }
                            MainDestination.CALENDAR -> {
                                CalendarScreen(
                                    uiState = uiState,
                                    onSelectDate = { viewModel.selectCalendarDate(it) },
                                    onToggleCompleted = { viewModel.toggleCompletion(it) },
                                    onDeleteItem = { viewModel.deleteItem(it) },
                                    onAddTaskForDate = { dateStr, title, isAllDay, hour, minute ->
                                        viewModel.addTaskForDate(dateStr, title, isAllDay, hour, minute)
                                    },
                                    onEditTask = { item, title, isAllDay, hour, minute ->
                                        viewModel.updateTask(item, title, isAllDay, hour, minute)
                                    },
                                    onSyncCalendarForDate = { viewModel.syncCalendarForDate(it) }
                                )
                            }
                            MainDestination.ROASTS -> {
                                QuotesScreen(
                                    uiState = uiState,
                                    onPreviewQuote = { quote ->
                                        viewModel.setBearQuote(quote)
                                        currentTab = MainDestination.SCHEDULE
                                    }
                                )
                            }
                            MainDestination.SETTINGS -> {
                                SettingsScreen(
                                    uiState = uiState,
                                    onSetInterval = { viewModel.setNotificationInterval(it) },
                                    onToggleNotifications = { viewModel.toggleNotifications(it) },
                                    onToggleQuietHours = { viewModel.toggleQuietHours(it) },
                                    onSetSleepTime = { h, m -> viewModel.setSleepTime(h, m) },
                                    onSetWakeTime = { h, m -> viewModel.setWakeTime(h, m) },
                                    onToggleVibration = { viewModel.toggleVibration(it) },
                                    onTestNotification = { viewModel.testPushNotificationNow() },
                                    onClearRoastsBook = { viewModel.clearRoastsCollectionBook() },
                                    onRequestCalendarPermission = {
                                        calendarLauncher.launch(Manifest.permission.READ_CALENDAR)
                                    },
                                    onRequestNotificationPermission = {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomNavTabItem(
    label: String,
    isSelected: Boolean,
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 2.dp)
            .testTag(testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(SoftGreenShadow, shape = CircleShape)
                    .padding(bottom = 2.dp)
                    .background(SoftGreenLight, shape = CircleShape)
                    .border(width = 2.dp, color = SoftGreen, shape = CircleShape)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = activeIcon,
                    contentDescription = label,
                    tint = SoftGreen,
                    modifier = Modifier.size(19.dp)
                )
            }
        } else {
            Box(
                modifier = Modifier.size(34.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = inactiveIcon,
                    contentDescription = label,
                    tint = SoftTextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Black,
                fontSize = 10.sp,
                letterSpacing = 0.6.sp
            ),
            color = if (isSelected) SoftGreen else SoftTextMuted
        )
    }
}
