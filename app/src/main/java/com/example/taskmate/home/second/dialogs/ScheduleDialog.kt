package com.example.taskmate.home.second.dialogs

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.taskmate.R
import com.example.taskmate.alarm.BatteryOptimizationHelper
import com.example.taskmate.color.TaskMateColors
import com.example.taskmate.data.ReminderOffset
import com.example.taskmate.data.ReminderType
import com.example.taskmate.home.second.chips.ReminderOffsetChip
import com.example.taskmate.home.second.chips.ReminderTypeChip
import com.example.taskmate.home.second.formatDate
import java.util.Calendar
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleDialog(
    initialDate: Long?,
    initialReminderType: ReminderType,
    initialReminderOffsets: Set<ReminderOffset>,
    onDismiss: () -> Unit,
    onConfirm: (date: Long?, reminderType: ReminderType, reminderOffsets: Set<ReminderOffset>) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val maxDialogHeight = (LocalConfiguration.current.screenHeightDp * 0.9f).dp

    var selectedDate by remember { mutableStateOf(initialDate) }
    var selectedReminderType by remember { mutableStateOf(initialReminderType) }
    var selectedReminderOffsets by remember { mutableStateOf(initialReminderOffsets) }

    // Re-shown every time "Alarm" is picked while the app still isn't exempt from battery
    // optimization — unlike the one-time home-screen nudge, this keeps nagging on every tap
    // until the user either grants the exemption or picks a different reminder type, since an
    // alarm silently not firing is worse than a repeated prompt.
    var showBatteryReliabilityDialog by remember { mutableStateOf(false) }
    val hasKnownAutoStartScreen = remember { BatteryOptimizationHelper.hasKnownAutoStartScreen(context) }

    // Below API 33 posting notifications never required runtime permission, so treat those
    // devices as always-granted rather than gating "Notification" behind a permission that
    // doesn't exist there.
    fun hasNotificationPermission(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
    }

    // Requested at the moment "Notification" is tapped (rather than unconditionally whenever
    // this dialog opens, regardless of what the user picks) so the system dialog only appears
    // when it's actually relevant to the choice being made. Selecting the type only on a granted
    // result keeps the chip from showing "Notification" as active when nothing would be posted.
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            selectedReminderType = ReminderType.NOTIFICATION
        }
    }

    // Shown instead of re-requesting the permission once Android has permanently denied it
    // (confirmed on this exact codebase: after two denials, the system stops showing its own
    // dialog at all and silently returns "not granted" every time). Without this, tapping
    // "Notification" launched the same silently-failing request over and over — no dialog, no
    // change, just a chip that never becomes selected.
    var showNotificationSettingsDialog by remember { mutableStateOf(false) }

    // 0 = None, 1 = DatePicker, 2 = TimePicker
    var activePickerDialog by remember { mutableIntStateOf(0) }

    // Create a unified Dialog for Date and Time Pickers to prevent flicker
    if (activePickerDialog != 0) {
        val today = remember {
            val localNow = Calendar.getInstance()
            Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                clear()
                set(
                    localNow.get(Calendar.YEAR),
                    localNow.get(Calendar.MONTH),
                    localNow.get(Calendar.DAY_OF_MONTH)
                )
            }.timeInMillis
        }

        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate ?: today
        )

        val initialCal = remember { Calendar.getInstance() }
        val timePickerState = rememberTimePickerState(
            initialHour = if (selectedDate != null) {
                val cal = Calendar.getInstance()
                cal.timeInMillis = selectedDate!!
                cal.get(Calendar.HOUR_OF_DAY)
            } else initialCal.get(Calendar.HOUR_OF_DAY),
            initialMinute = if (selectedDate != null) {
                val cal = Calendar.getInstance()
                cal.timeInMillis = selectedDate!!
                cal.get(Calendar.MINUTE)
            } else initialCal.get(Calendar.MINUTE)
        )

        var pickerInitialDate by remember { mutableStateOf(datePickerState.selectedDateMillis) }

        // Auto-transition to time picker when a new date is selected
        LaunchedEffect(datePickerState.selectedDateMillis) {
            if (activePickerDialog == 1 && datePickerState.selectedDateMillis != pickerInitialDate) {
                selectedDate = datePickerState.selectedDateMillis
                activePickerDialog = 2
                pickerInitialDate = datePickerState.selectedDateMillis
            }
        }

        DatePickerDialog(
            onDismissRequest = { activePickerDialog = 0 },
            confirmButton = {
                if (activePickerDialog == 1) {
                    TextButton(onClick = {
                        selectedDate = datePickerState.selectedDateMillis
                        activePickerDialog = 2
                    }) {
                        Text(stringResource(id = R.string.next), color = Color(0xFF6366F1))
                    }
                } else if (activePickerDialog == 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { activePickerDialog = 1 }) {
                            Text(stringResource(id = R.string.back), color = Color.White.copy(alpha = 0.7f))
                        }
                        TextButton(onClick = {
                            val cal = Calendar.getInstance()
                            selectedDate?.let { cal.timeInMillis = it }
                            cal.set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                            cal.set(Calendar.MINUTE, timePickerState.minute)
                            cal.set(Calendar.SECOND, 0)
                            selectedDate = cal.timeInMillis
                            activePickerDialog = 0
                        }) {
                            Text(stringResource(id = R.string.ok_button), color = Color(0xFF6366F1))
                        }
                    }
                }
            },
            dismissButton = {
                if (activePickerDialog == 1) {
                    TextButton(onClick = { activePickerDialog = 0 }) {
                        Text(stringResource(id = R.string.cancel), color = Color.White.copy(alpha = 0.7f))
                    }
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = Color(0xFF2D3748)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = if (activePickerDialog == 2) 16.dp else 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (activePickerDialog == 1) {
                    DatePicker(
                        state = datePickerState,
                        colors = DatePickerDefaults.colors(
                            containerColor = Color(0xFF2D3748),
                            titleContentColor = Color.White,
                            headlineContentColor = Color.White,
                            weekdayContentColor = Color.White,
                            subheadContentColor = Color.White,
                            yearContentColor = Color.White,
                            currentYearContentColor = Color(0xFF6366F1),
                            selectedYearContentColor = Color.White,
                            selectedYearContainerColor = Color(0xFF6366F1),
                            dayContentColor = Color.White,
                            selectedDayContentColor = Color.White,
                            selectedDayContainerColor = Color(0xFF6366F1),
                            todayContentColor = Color(0xFF6366F1),
                            todayDateBorderColor = Color(0xFF6366F1)
                        )
                    )
                } else if (activePickerDialog == 2) {
                    Text(
                        text = stringResource(id = R.string.set_time),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = Color(0xFF374151),
                            clockDialSelectedContentColor = Color.White,
                            clockDialUnselectedContentColor = Color.White,
                            selectorColor = Color(0xFF6366F1),
                            containerColor = Color(0xFF2D3748),
                            periodSelectorBorderColor = Color(0xFF6366F1),
                            periodSelectorSelectedContainerColor = Color(0xFF6366F1),
                            periodSelectorUnselectedContainerColor = Color.Transparent,
                            periodSelectorSelectedContentColor = Color.White,
                            periodSelectorUnselectedContentColor = Color.White,
                            timeSelectorSelectedContainerColor = Color(0xFF6366F1),
                            timeSelectorUnselectedContainerColor = Color(0xFF374151),
                            timeSelectorSelectedContentColor = Color.White,
                            timeSelectorUnselectedContentColor = Color.White
                        )
                    )
                }
            }
        }
    }

    AlertDialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = maxDialogHeight)
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3748))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 12.dp, top = 20.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.schedule_label),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = stringResource(id = R.string.cancel),
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

                // Scrollable form content
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    // Due Date & Time Selection — full-width tappable card
                    SectionLabel(text = stringResource(id = R.string.due_date))

                    val dueDateAccent by animateColorAsState(
                        targetValue = if (selectedDate != null) Color(0xFF6366F1) else Color.White.copy(alpha = 0.15f),
                        label = "dueDateAccent"
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { activePickerDialog = 1 },
                        shape = RoundedCornerShape(16.dp),
                        color = if (selectedDate != null) Color(0xFF6366F1).copy(alpha = 0.14f) else Color(0xFF1E293B),
                        border = BorderStroke(1.dp, dueDateAccent)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                tint = if (selectedDate != null) Color(0xFF6366F1) else Color.White.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = selectedDate?.let { formatDate(context, it) } ?: stringResource(id = R.string.no_due_date),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            if (selectedDate != null) {
                                IconButton(onClick = { selectedDate = null }, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = stringResource(id = R.string.clear_date),
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = stringResource(id = R.string.set_date),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF6366F1)
                                )
                            }
                        }
                    }

                    if (selectedDate != null) {
                        SectionDivider()

                        // Reminder Type Selection
                        SectionLabel(text = stringResource(id = R.string.reminder_type_label))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReminderType.entries.forEach { type ->
                                ReminderTypeChip(
                                    type = type,
                                    isSelected = selectedReminderType == type,
                                    onClick = {
                                        when {
                                            // Alarm can't actually stay selected while the app
                                            // isn't exempt from battery optimization — the OS can
                                            // silently kill it before the alarm fires, so the chip
                                            // would show "Alarm" as active while nothing reliable
                                            // is scheduled. Block the selection itself (rather
                                            // than selecting it and nagging afterwards) so
                                            // dismissing the dialog can't leave the UI on a state
                                            // that doesn't actually work.
                                            type == ReminderType.ALARM &&
                                                    !BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context) -> {
                                                showBatteryReliabilityDialog = true
                                            }
                                            // Same idea for Notification: without POST_NOTIFICATIONS
                                            // (API 33+), NotificationManagerCompat.notify() is a
                                            // silent no-op, so request it here and only select the
                                            // type once it's actually been granted. If Android has
                                            // already permanently denied it (shouldShowRequestPermissionRationale
                                            // false after a prior request), the system won't show
                                            // its dialog again — route to the app's notification
                                            // settings instead of silently re-requesting.
                                            type == ReminderType.NOTIFICATION && !hasNotificationPermission() -> {
                                                val prefs = context.getSharedPreferences(
                                                    "taskmate_prefs",
                                                    android.content.Context.MODE_PRIVATE
                                                )
                                                val requestedBefore = prefs.getBoolean(
                                                    "requested_notification_permission",
                                                    false
                                                )
                                                val canShowSystemDialog = !requestedBefore ||
                                                        (activity != null && ActivityCompat.shouldShowRequestPermissionRationale(
                                                            activity,
                                                            Manifest.permission.POST_NOTIFICATIONS
                                                        ))
                                                if (canShowSystemDialog) {
                                                    prefs.edit().putBoolean(
                                                        "requested_notification_permission",
                                                        true
                                                    ).apply()
                                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                } else {
                                                    showNotificationSettingsDialog = true
                                                }
                                            }
                                            else -> {
                                                selectedReminderType = type
                                            }
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        if (selectedReminderType != ReminderType.NONE) {
                            Spacer(modifier = Modifier.height(16.dp))

                            // Reminder Offset Selection — multi-select: pick as many as you want
                            // (e.g. both "1 day before" and "1 hour before").
                            SectionLabel(text = stringResource(id = R.string.remind_me))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(ReminderOffset.entries.toTypedArray()) { offset ->
                                    ReminderOffsetChip(
                                        offset = offset,
                                        isSelected = offset in selectedReminderOffsets,
                                        onClick = {
                                            selectedReminderOffsets = if (offset in selectedReminderOffsets) {
                                                selectedReminderOffsets - offset
                                            } else {
                                                selectedReminderOffsets + offset
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Divider(color = Color.White.copy(alpha = 0.08f), thickness = 1.dp)

                // Action Buttons (fixed footer)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White.copy(alpha = 0.8f)
                        )
                    ) {
                        Text(stringResource(id = R.string.cancel), fontWeight = FontWeight.SemiBold)
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(TaskMateColors.primaryGradient)
                            .clickable {
                                val finalReminderType = if (selectedDate != null) selectedReminderType else ReminderType.NONE
                                val finalOffsets = if (finalReminderType != ReminderType.NONE && selectedReminderOffsets.isEmpty()) {
                                    setOf(ReminderOffset.AT_DUE_TIME)
                                } else {
                                    selectedReminderOffsets
                                }
                                onConfirm(selectedDate, finalReminderType, finalOffsets)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(id = R.string.done),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    if (showBatteryReliabilityDialog) {
        AutoStartReliabilityDialog(
            hasKnownAutoStartScreen = hasKnownAutoStartScreen,
            onDismissRequest = { showBatteryReliabilityDialog = false },
            onNotNow = { showBatteryReliabilityDialog = false },
            onOpenSettings = { showBatteryReliabilityDialog = false }
        )
    }

    if (showNotificationSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationSettingsDialog = false },
            title = {
                Text(
                    stringResource(id = R.string.notification_permission_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    stringResource(id = R.string.notification_permission_desc),
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showNotificationSettingsDialog = false
                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                    context.startActivity(intent)
                }) {
                    Text(
                        stringResource(id = R.string.autostart_open_settings),
                        color = Color(0xFF6366F1),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showNotificationSettingsDialog = false }) {
                    Text(
                        stringResource(id = R.string.battery_optimization_not_now),
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            },
            containerColor = Color(0xFF2D3748),
            shape = RoundedCornerShape(24.dp)
        )
    }
}
