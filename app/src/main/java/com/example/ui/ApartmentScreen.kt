package com.example.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.FlatEntity
import com.example.data.model.FlatWithPaymentStatus
import com.example.ui.components.AddEditFlatDialog
import com.example.ui.components.AssociationSettingsDialog
import com.example.ui.components.FlatCard
import com.example.ui.components.MarkPaidDialog
import com.example.ui.components.MetricsDashboardCard
import com.example.ui.components.PaymentReceiptDialog
import com.example.ui.components.ReminderHistorySheet
import com.example.ui.components.SendReminderDialog
import com.example.ui.theme.AmberPending
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.EmeraldPaid
import com.example.ui.theme.Navy900
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate600

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApartmentScreen(
    viewModel: ApartmentViewModel,
    modifier: Modifier = Modifier
) {
    val displayMonth by viewModel.displayMonthName.collectAsStateWithLifecycle()
    val flats by viewModel.filteredFlats.collectAsStateWithLifecycle()
    val metrics by viewModel.metrics.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val filterStatus by viewModel.statusFilter.collectAsStateWithLifecycle()
    val availableBlocks by viewModel.availableBlocks.collectAsStateWithLifecycle()
    val selectedBlock by viewModel.selectedBlock.collectAsStateWithLifecycle()
    val settings by viewModel.associationSettings.collectAsStateWithLifecycle()
    val reminderLogs by viewModel.reminderLogs.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Dialog & sheet states
    var selectedItemForReceipt by remember { mutableStateOf<FlatWithPaymentStatus?>(null) }
    var selectedItemForMarkPaid by remember { mutableStateOf<FlatWithPaymentStatus?>(null) }
    var selectedItemForReminder by remember { mutableStateOf<FlatWithPaymentStatus?>(null) }
    var flatToEdit by remember { mutableStateOf<FlatEntity?>(null) }
    var showAddFlatDialog by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Pending reminder action to trigger after notification permission is granted
    var pendingReminderAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Notification Permission Launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        pendingReminderAction?.invoke()
        pendingReminderAction = null
    }

    fun requestNotificationPermissionAndRun(action: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pendingReminderAction = action
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            action()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.userMessage.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Apartment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = settings.associationName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Monthly Dues & Reminders",
                                fontSize = 11.sp,
                                color = Slate600
                            )
                        }
                    }
                },
                actions = {
                    // History icon with log badge
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier.testTag("open_history_button")
                    ) {
                        if (reminderLogs.isNotEmpty()) {
                            BadgedBox(
                                badge = {
                                    Badge {
                                        Text("${reminderLogs.size}")
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "Reminder History"
                                )
                            }
                        } else {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Reminder History"
                            )
                        }
                    }

                    // Settings Icon
                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("open_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Association Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddFlatDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("add_flat_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Flat",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Add Flat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 90.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Month Switcher Card
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { viewModel.previousMonth() },
                            modifier = Modifier.testTag("prev_month_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Month",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = displayMonth,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = { viewModel.nextMonth() },
                            modifier = Modifier.testTag("next_month_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Month",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Metrics Card with quick 'Remind All' button
            item {
                MetricsDashboardCard(
                    metrics = metrics,
                    displayMonth = displayMonth,
                    onRemindAllPending = {
                        requestNotificationPermissionAndRun {
                            viewModel.sendAllPendingReminders()
                        }
                    }
                )
            }

            // Search Bar & Filter Chips
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.onSearchQueryChanged(it) },
                        placeholder = { Text("Search flat number, resident, block...") },
                        leadingIcon = {
                            Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Slate600)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_flats_input"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = filterStatus == FilterStatus.ALL,
                            onClick = { viewModel.onFilterStatusChanged(FilterStatus.ALL) },
                            label = { Text("All (${metrics.totalFlats})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BluePrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_all")
                        )

                        FilterChip(
                            selected = filterStatus == FilterStatus.PENDING,
                            onClick = { viewModel.onFilterStatusChanged(FilterStatus.PENDING) },
                            label = { Text("Pending (${metrics.pendingCount})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AmberPending,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_pending")
                        )

                        FilterChip(
                            selected = filterStatus == FilterStatus.PAID,
                            onClick = { viewModel.onFilterStatusChanged(FilterStatus.PAID) },
                            label = { Text("Paid (${metrics.paidCount})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = EmeraldPaid,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_paid")
                        )

                        // Blocks filter
                        if (availableBlocks.size > 2) {
                            availableBlocks.filter { it != "ALL" }.forEach { block ->
                                FilterChip(
                                    selected = selectedBlock == block,
                                    onClick = {
                                        if (selectedBlock == block) viewModel.onBlockSelected("ALL")
                                        else viewModel.onBlockSelected(block)
                                    },
                                    label = { Text(block) }
                                )
                            }
                        }
                    }
                }
            }

            // Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "FLATS DIRECTORY",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = Slate600
                    )
                    Text(
                        text = "${flats.size} flats",
                        fontSize = 12.sp,
                        color = Slate600
                    )
                }
            }

            // Flat Items List
            if (flats.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No flats match your filter",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate600
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try changing search terms or filters above.",
                                fontSize = 12.sp,
                                color = Slate600
                            )
                        }
                    }
                }
            } else {
                items(flats, key = { it.flat.id }) { item ->
                    FlatCard(
                        item = item,
                        onMarkPaidClick = { selectedItemForMarkPaid = item },
                        onSendReminderClick = {
                            selectedItemForReminder = item
                        },
                        onViewReceiptClick = { selectedItemForReceipt = item },
                        onEditFlatClick = { flatToEdit = item.flat }
                    )
                }
            }
        }
    }

    // Modal: Send Reminder Dialog
    selectedItemForReminder?.let { item ->
        SendReminderDialog(
            item = item,
            settings = settings,
            displayMonth = displayMonth,
            onSendNotification = {
                requestNotificationPermissionAndRun {
                    viewModel.sendSingleReminder(item)
                }
            },
            onDismiss = { selectedItemForReminder = null }
        )
    }

    // Modal: Mark as Paid Dialog
    selectedItemForMarkPaid?.let { item ->
        MarkPaidDialog(
            item = item,
            displayMonth = displayMonth,
            onConfirm = { amount, method ->
                viewModel.markPaymentAsPaid(item.flat, amount, method)
            },
            onDismiss = { selectedItemForMarkPaid = null }
        )
    }

    // Modal: Digital Receipt Dialog
    selectedItemForReceipt?.let { item ->
        PaymentReceiptDialog(
            item = item,
            associationName = settings.associationName,
            displayMonth = displayMonth,
            onDismiss = { selectedItemForReceipt = null },
            onRevertToPending = {
                viewModel.markPaymentAsPending(item.flat)
            }
        )
    }

    // Modal: Add or Edit Flat Dialog
    if (showAddFlatDialog || flatToEdit != null) {
        AddEditFlatDialog(
            flatToEdit = flatToEdit,
            defaultFee = settings.defaultMonthlyFee,
            onSave = { flatNumber, block, floor, residentName, residentType, phone, email, fee ->
                if (flatToEdit != null) {
                    viewModel.updateFlat(
                        flatToEdit!!.copy(
                            flatNumber = flatNumber,
                            block = block,
                            floor = floor,
                            residentName = residentName,
                            residentType = residentType,
                            phone = phone,
                            email = email,
                            monthlyFee = fee
                        )
                    )
                } else {
                    viewModel.addFlat(
                        flatNumber = flatNumber,
                        block = block,
                        floor = floor,
                        residentName = residentName,
                        residentType = residentType,
                        phone = phone,
                        email = email,
                        monthlyFee = fee
                    )
                }
                showAddFlatDialog = false
                flatToEdit = null
            },
            onDelete = if (flatToEdit != null) {
                { flat -> viewModel.deleteFlat(flat) }
            } else null,
            onDismiss = {
                showAddFlatDialog = false
                flatToEdit = null
            }
        )
    }

    // Modal: Reminder History Audit Sheet
    if (showHistorySheet) {
        ReminderHistorySheet(
            logs = reminderLogs,
            onClearLogs = { viewModel.clearReminderLogs() },
            onDismiss = { showHistorySheet = false }
        )
    }

    // Modal: Association Settings Dialog
    if (showSettingsDialog) {
        AssociationSettingsDialog(
            currentSettings = settings,
            onSave = { newSettings -> viewModel.updateAssociationSettings(newSettings) },
            onDismiss = { showSettingsDialog = false }
        )
    }
}
