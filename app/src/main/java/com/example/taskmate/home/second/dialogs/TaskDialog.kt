package com.example.taskmate.home.second.dialogs

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.DialogProperties
import com.example.taskmate.R
import com.example.taskmate.data.*
import com.example.taskmate.home.second.chips.CategoryChip
import com.example.taskmate.home.second.chips.PriorityChip
import com.example.taskmate.home.second.formatDate
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(todo: Todo?, onDismiss: () -> Unit, onConfirm: (Todo) -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(todo?.title ?: "") }
    var description by remember { mutableStateOf(todo?.description ?: "") }
    var selectedPriority by remember { mutableStateOf(todo?.priority ?: Priority.MEDIUM) }
    var selectedCategory by remember { mutableStateOf(todo?.category ?: Category.PERSONAL) }
    
    // 0 = None, 1 = DatePicker, 2 = TimePicker
    var activePickerDialog by remember { mutableIntStateOf(0) }
    
    var selectedDate by remember { mutableStateOf(todo?.dueDate) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* We just need it requested to post notifications */ }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Create a unified Dialog for Date and Time Pickers to prevent flicker
    if (activePickerDialog != 0) {
        val today = remember {
            Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
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

        var initialDate by remember { mutableStateOf(datePickerState.selectedDateMillis) }
        
        // Auto-transition to time picker when a new date is selected
        LaunchedEffect(datePickerState.selectedDateMillis) {
            if (activePickerDialog == 1 && datePickerState.selectedDateMillis != initialDate) {
                selectedDate = datePickerState.selectedDateMillis
                activePickerDialog = 2
                initialDate = datePickerState.selectedDateMillis
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
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3748))
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                Text(
                    text = if (todo != null) {
                        stringResource(id = R.string.emoji_edit) + " " + stringResource(id = R.string.edit_task)
                    } else {
                        stringResource(id = R.string.emoji_create) + " " + stringResource(id = R.string.create_new_task)
                    },
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(id = R.string.task_title_hint), color = Color.White.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF6366F1),
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Description field
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(id = R.string.description_hint), color = Color.White.copy(alpha = 0.7f)) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF6366F1),
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Priority Selection
                Text(
                    text = stringResource(id = R.string.priority_level),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Priority.entries.forEach { priority ->
                        PriorityChip(
                            priority = priority,
                            isSelected = selectedPriority == priority,
                            onClick = { selectedPriority = priority },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Category Selection
                Text(
                    text = stringResource(id = R.string.category),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(Category.entries.toTypedArray()) { category ->
                        CategoryChip(
                            category = category,
                            isSelected = selectedCategory == category,
                            onClick = { selectedCategory = category }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Due Date Selection - FIXED DATE PICKER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(id = R.string.due_date),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Text(
                            text = selectedDate?.let { formatDate(context, it) } ?: stringResource(id = R.string.no_due_date),
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    Row {
                        Button(
                            onClick = { activePickerDialog = 1 },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6366F1)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(stringResource(id = R.string.set_date), color = Color.White)
                        }

                        if (selectedDate != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = { selectedDate = null }
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = stringResource(id = R.string.clear_date),
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White.copy(alpha = 0.7f)
                        )
                    ) {
                        Text(stringResource(id = R.string.cancel), modifier = Modifier.padding(vertical = 4.dp))
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val newTodo = Todo(
                                    id = todo?.id ?: 0L,
                                    title = title.trim(),
                                    description = description.trim(),
                                    priority = selectedPriority,
                                    category = selectedCategory,
                                    dueDate = selectedDate,
                                    isCompleted = todo?.isCompleted ?: false,
                                    createdAt = todo?.createdAt ?: System.currentTimeMillis()
                                )
                                onConfirm(newTodo)
                            } else {
                                Toast.makeText(context, context.getString(R.string.empty_title_error), Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF6366F1)
                        )
                    ) {
                        Text(
                            text = if (todo != null) stringResource(id = R.string.update) else stringResource(id = R.string.create),
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
