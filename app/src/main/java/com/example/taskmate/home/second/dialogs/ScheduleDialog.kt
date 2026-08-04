package com.example.taskmate.home.second.dialogs

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.taskmate.R
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
    val maxDialogHeight = (LocalConfiguration.current.screenHeightDp * 0.9f).dp

    var selectedDate by remember { mutableStateOf(initialDate) }
    var selectedReminderType by remember { mutableStateOf(initialReminderType) }
    var selectedReminderOffsets by remember { mutableStateOf(initialReminderOffsets) }

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
                                    onClick = { selectedReminderType = type },
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
}
