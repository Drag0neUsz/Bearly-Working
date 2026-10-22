package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
        enableEdgeToEdge()

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
                    containerColor = ComicWhiteBg,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        // Sharp Full-Width Comic Bottom Navigation Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(ComicSurfaceWhite)
                                .border(width = 2.dp, color = ComicInkBlack)
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("bottom_nav_bar")
                        ) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                // Tab 1: SCHEDULE (TODAY)
                                val isSched = currentTab == MainDestination.SCHEDULE
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSched) ComicInkBlack else ComicSurfaceWhite)
                                        .clickable { currentTab = MainDestination.SCHEDULE }
                                        .padding(vertical = 10.dp)
                                        .testTag("nav_schedule"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = if (isSched) Icons.Filled.Today else Icons.Outlined.Today,
                                            contentDescription = "Today Schedule",
                                            tint = if (isSched) Color.White else ComicInkBlack,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "TODAY",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp,
                                            color = if (isSched) Color.White else ComicInkBlack
                                        )
                                    }
                                }

                                // Tab Divider
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(52.dp)
                                        .background(ComicInkBlack)
                                )

                                // Tab 2: CALENDAR
                                val isCalendar = currentTab == MainDestination.CALENDAR
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isCalendar) ComicInkBlack else ComicSurfaceWhite)
                                        .clickable { currentTab = MainDestination.CALENDAR }
                                        .padding(vertical = 10.dp)
                                        .testTag("nav_calendar"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = if (isCalendar) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                                            contentDescription = "Calendar",
                                            tint = if (isCalendar) Color.White else ComicInkBlack,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "CALENDAR",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp,
                                            color = if (isCalendar) Color.White else ComicInkBlack
                                        )
                                    }
                                }

                                // Tab Divider
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(52.dp)
                                        .background(ComicInkBlack)
                                )

                                // Tab 3: ROASTS
                                val isRoasts = currentTab == MainDestination.ROASTS
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isRoasts) ComicInkBlack else ComicSurfaceWhite)
                                        .clickable { currentTab = MainDestination.ROASTS }
                                        .padding(vertical = 10.dp)
                                        .testTag("nav_roasts"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = if (isRoasts) Icons.Filled.FormatQuote else Icons.Outlined.FormatQuote,
                                            contentDescription = "Roasts",
                                            tint = if (isRoasts) Color.White else ComicInkBlack,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "ROASTS",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp,
                                            color = if (isRoasts) Color.White else ComicInkBlack
                                        )
                                    }
                                }

                                // Tab Divider
                                Box(
                                    modifier = Modifier
                                        .width(2.dp)
                                        .height(52.dp)
                                        .background(ComicInkBlack)
                                )

                                // Tab 4: SETTINGS
                                val isSettings = currentTab == MainDestination.SETTINGS
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .background(if (isSettings) ComicInkBlack else ComicSurfaceWhite)
                                        .clickable { currentTab = MainDestination.SETTINGS }
                                        .padding(vertical = 10.dp)
                                        .testTag("nav_settings"),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = if (isSettings) Icons.Filled.Settings else Icons.Outlined.Settings,
                                            contentDescription = "Settings",
                                            tint = if (isSettings) Color.White else ComicInkBlack,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "SETTINGS",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 10.sp,
                                            color = if (isSettings) Color.White else ComicInkBlack
                                        )
                                    }
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Crossfade(
                        targetState = currentTab,
                        label = "screenTransition",
                        modifier = Modifier.padding(innerPadding)
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
