package com.example.taskmate.home.second.dialogs

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taskmate.R
import com.example.taskmate.data.*
import com.example.taskmate.home.second.chips.CategoryChip
import com.example.taskmate.home.second.chips.PriorityChip
import com.example.taskmate.home.second.chips.ReminderOffsetChip
import com.example.taskmate.home.second.chips.ReminderTypeChip
import com.example.taskmate.home.second.formatDate
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(todo: Todo?, onDismiss: () -> Unit, onConfirm: (Todo) -> Unit) {
    val context = LocalContext.current
    var titleFocused by remember { mutableStateOf(false) }
    var descriptionFocused by remember { mutableStateOf(false) }

    // FocusManager.clearFocus() doesn't reliably remove a TextField's focused-border rendering
    // on this Compose version — the fix that actually works is forcing focus onto a dummy
    // invisible node instead of trying to "clear" it (see the zero-size Box below).
    val clearFocusRequester = remember { FocusRequester() }

    // When the keyboard is showing, the system's back-press dismisses it at the OS level and
    // never reaches Compose's back dispatcher at all — so a BackHandler alone can't catch that
    // case. Instead, watch the window's actual IME-visibility inset directly: it changes no
    // matter how the keyboard closed (back press, gesture, done button), and only then do we
    // drop focus, so the field's border doesn't stay stuck in its "focused" highlight color.
    val view = LocalView.current
    DisposableEffect(view) {
        val listener = android.view.ViewTreeObserver.OnGlobalLayoutListener {
            val imeVisible = ViewCompat.getRootWindowInsets(view)?.isVisible(WindowInsetsCompat.Type.ime()) ?: true
            if (!imeVisible && (titleFocused || descriptionFocused)) {
                clearFocusRequester.requestFocus()
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose { view.viewTreeObserver.removeOnGlobalLayoutListener(listener) }
    }

    // Belt-and-suspenders: if a field is somehow still focused with no IME involved and the
    // user presses back, drop focus on the first press rather than dismissing the whole dialog.
    BackHandler(enabled = titleFocused || descriptionFocused) {
        clearFocusRequester.requestFocus()
    }

    var title by remember { mutableStateOf(todo?.title ?: "") }
    var description by remember { mutableStateOf(TextFieldValue(todo?.description ?: "")) }
    var selectedPriority by remember { mutableStateOf(todo?.priority ?: Priority.MEDIUM) }
    var selectedCategory by remember { mutableStateOf(todo?.category ?: Category.PERSONAL) }
    var selectedDate by remember { mutableStateOf(todo?.dueDate) }

    var selectedReminderOffsets by remember {
        mutableStateOf(
            ReminderOffset.decodeSet(todo?.reminderOffsetsMinutes).ifEmpty { setOf(ReminderOffset.AT_DUE_TIME) }
        )
    }
    var selectedReminderType by remember { mutableStateOf(todo?.reminderType ?: ReminderType.NOTIFICATION) }
    var showScheduleDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { /* We just need it requested to post notifications */ }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    if (showScheduleDialog) {
        ScheduleDialog(
            initialDate = selectedDate,
            initialReminderType = selectedReminderType,
            initialReminderOffsets = selectedReminderOffsets,
            onDismiss = { showScheduleDialog = false },
            onConfirm = { date, reminderType, reminderOffsets ->
                selectedDate = date
                selectedReminderType = reminderType
                selectedReminderOffsets = reminderOffsets
                showScheduleDialog = false
            }
        )
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
                // Invisible focus target: requesting focus here (instead of trying to "clear"
                // focus) is what reliably drops a TextField's focused-border rendering.
                Box(
                    modifier = Modifier
                        .size(1.dp)
                        .focusRequester(clearFocusRequester)
                        .focusTarget()
                )

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
                    label = {
                        Text(
                            stringResource(id = R.string.task_title_hint),
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { titleFocused = it.isFocused },
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
                    onValueChange = { newValue -> description = continueListOnEnter(description, newValue) },
                    label = {
                        Text(
                            stringResource(id = R.string.description_hint),
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { descriptionFocused = it.isFocused },
                    minLines = 2,
                    maxLines = 5,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFF6366F1),
                        focusedBorderColor = Color(0xFF6366F1),
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Default
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Description formatting toolbar — Bullet/Numbered/Arrow start a new prefixed
                // line at the cursor. Bold only applies to an actual selection (disabled
                // otherwise) and toggles off if the selection is already bold. Formatting
                // renders styled once saved (task list); the edit field itself stays plain
                // so nothing looks garbled mid-edit.
                val isBoldEnabled = !description.selection.collapsed
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FormatToolbarButton(label = "•") {
                        description = insertLineWithPrefix(description, "• ")
                    }
                    FormatToolbarButton(label = "1.") {
                        description = insertLineWithPrefix(description, nextNumberedPrefix(description))
                    }
                    FormatToolbarButton(label = "→") {
                        description = insertLineWithPrefix(description, "→ ")
                    }
                    FormatToolbarButton(
                        label = "B",
                        fontWeight = FontWeight.Bold,
                        enabled = isBoldEnabled
                    ) {
                        description = toggleBoldSelection(description)
                    }
                }

                SectionDivider()

                // Priority Selection
                SectionLabel(text = stringResource(id = R.string.priority_level))

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

                SectionDivider()

                // Category Selection
                SectionLabel(text = stringResource(id = R.string.category))

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

                SectionDivider()

                SectionLabel(text = stringResource(id = R.string.schedule_label))

                val scheduleSummary = if (selectedDate == null) {
                    stringResource(id = R.string.no_schedule_set)
                } else {
                    val dateStr = formatDate(context, selectedDate!!)
                    if (selectedReminderType == ReminderType.NONE) {
                        dateStr
                    } else {
                        val typeLabel = stringResource(id = selectedReminderType.displayName)
                        val offsetLabelList = mutableListOf<String>()
                        for (offset in selectedReminderOffsets.sortedBy { it.minutes }) {
                            offsetLabelList.add(stringResource(id = offset.displayName))
                        }
                        "$dateStr · $typeLabel · ${offsetLabelList.joinToString(", ")}"
                    }
                }

                // Due Date Selection - FIXED DATE PICKER
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { showScheduleDialog = true },
                    shape = RoundedCornerShape(16.dp),
                    color = if (selectedDate != null) Color(0xFF6366F1).copy(alpha = 0.14f) else Color(
                        0xFF1E293B
                    ),
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
                            text = scheduleSummary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = stringResource(
                                id = if (selectedDate != null) R.string.edit_schedule else R.string.set_schedule
                            ),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF6366F1)
                        )
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
                                    description = description.text.trim(),
                                    priority = selectedPriority,
                                    category = selectedCategory,
                                    dueDate = selectedDate,
                                    reminderOffsetsMinutes = selectedDate?.let { ReminderOffset.encodeSet(selectedReminderOffsets) },
                                    reminderType = if (selectedDate != null) selectedReminderType else ReminderType.NONE,
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

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        color = Color.White.copy(alpha = 0.55f),
        modifier = Modifier.padding(bottom = 10.dp)
    )
}

@Composable
fun SectionDivider() {
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
private fun FormatToolbarButton(
    label: String,
    tint: Color = Color.White.copy(alpha = 0.85f),
    background: Color = Color(0xFF1E293B),
    fontWeight: FontWeight = FontWeight.SemiBold,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(10.dp),
        color = if (enabled) background else background.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = if (enabled) 0.15f else 0.06f))
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = label,
                color = if (enabled) tint else tint.copy(alpha = 0.35f),
                fontWeight = fontWeight,
                fontSize = 14.sp
            )
        }
    }
}

/** Starts a new prefixed line (e.g. "• ", "1. ", "→ ") at the cursor, or right where it
 *  stands if already at the start of a line. */
private fun insertLineWithPrefix(value: TextFieldValue, prefix: String): TextFieldValue {
    val text = value.text
    val cursor = value.selection.start
    val atLineStart = cursor == 0 || text.getOrNull(cursor - 1) == '\n'
    val insertion = if (atLineStart) prefix else "\n$prefix"
    val before = text.substring(0, cursor)
    val after = text.substring(cursor)
    return TextFieldValue(
        text = before + insertion + after,
        selection = TextRange(cursor + insertion.length)
    )
}

private val NUMBERED_LINE_PREFIX_REGEX = Regex("""^(\d+)\.\s""")

/** Continues numbering from the line directly above the cursor, or starts at "1. ". */
private fun nextNumberedPrefix(value: TextFieldValue): String {
    val text = value.text
    val cursor = value.selection.start
    val lineStart = text.lastIndexOf('\n', (cursor - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
    if (lineStart == 0) return "1. "
    val prevLineEnd = lineStart - 1
    val prevLineStart = text.lastIndexOf('\n', (prevLineEnd - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
    val previousLine = text.substring(prevLineStart, prevLineEnd)
    val next = (NUMBERED_LINE_PREFIX_REGEX.find(previousLine)?.groupValues?.get(1)?.toIntOrNull() ?: 0) + 1
    return "$next. "
}

private val SIMPLE_LINE_PREFIX_REGEX = Regex("^(• |→ )")

/**
 * When [newValue] is exactly [oldValue] plus a single Enter keypress (no other edit), continues
 * whatever list prefix the just-finished line had — bumping the number for a numbered line, or
 * repeating "• "/"→ " for a bullet/arrow line. Pressing Enter on an already-empty list line exits
 * the list instead (removes the empty prefix), matching standard notes-app behavior. Any other
 * edit (typing, pasting, selection replace) passes through unchanged.
 */
private fun continueListOnEnter(oldValue: TextFieldValue, newValue: TextFieldValue): TextFieldValue {
    val oldText = oldValue.text
    val newText = newValue.text
    val oldCursor = oldValue.selection.start

    val isPlainEnter = oldValue.selection.collapsed &&
            newText.length == oldText.length + 1 &&
            newText.getOrNull(oldCursor) == '\n' &&
            newText.substring(0, oldCursor) == oldText.substring(0, oldCursor) &&
            newText.substring(oldCursor + 1) == oldText.substring(oldCursor)

    if (!isPlainEnter) return newValue

    val lineStart = oldText.lastIndexOf('\n', (oldCursor - 1).coerceAtLeast(0)).let { if (it == -1) 0 else it + 1 }
    val finishedLine = oldText.substring(lineStart, oldCursor)

    val simpleMatch = SIMPLE_LINE_PREFIX_REGEX.find(finishedLine)
    val numberedMatch = NUMBERED_LINE_PREFIX_REGEX.find(finishedLine)
    val existingPrefix = simpleMatch?.value ?: numberedMatch?.value ?: return newValue

    if (finishedLine == existingPrefix) {
        // Empty list item — exit the list by removing the dangling prefix instead of continuing it.
        val trimmedText = oldText.substring(0, lineStart) + oldText.substring(oldCursor)
        return newValue.copy(text = trimmedText, selection = TextRange(lineStart))
    }

    val continuation = if (numberedMatch != null) {
        val next = (numberedMatch.groupValues[1].toIntOrNull() ?: 0) + 1
        "$next. "
    } else {
        existingPrefix
    }
    val cursorAfterNewline = oldCursor + 1
    val withContinuation = newText.substring(0, cursorAfterNewline) + continuation + newText.substring(cursorAfterNewline)
    return newValue.copy(text = withContinuation, selection = TextRange(cursorAfterNewline + continuation.length))
}

private const val BOLD_MARKER = "**"

/**
 * Toggles bold on the current selection: wraps it in "**" if not already bold, or removes
 * the markers if the selection is already bold (either the selection sits just inside the
 * markers, or the selection includes the markers themselves).
 */
private fun toggleBoldSelection(value: TextFieldValue): TextFieldValue {
    val selection = value.selection
    if (selection.collapsed) return value

    val text = value.text
    val start = selection.start
    val end = selection.end
    val markerLen = BOLD_MARKER.length

    val hasSurroundingMarkers = start >= markerLen && end + markerLen <= text.length &&
            text.substring(start - markerLen, start) == BOLD_MARKER &&
            text.substring(end, end + markerLen) == BOLD_MARKER

    if (hasSurroundingMarkers) {
        val newText = text.removeRange(end, end + markerLen).removeRange(start - markerLen, start)
        return TextFieldValue(newText, TextRange(start - markerLen, end - markerLen))
    }

    val selectedText = text.substring(start, end)
    if (selectedText.length >= 2 * markerLen && selectedText.startsWith(BOLD_MARKER) && selectedText.endsWith(BOLD_MARKER)) {
        val inner = selectedText.substring(markerLen, selectedText.length - markerLen)
        val newText = text.substring(0, start) + inner + text.substring(end)
        return TextFieldValue(newText, TextRange(start, start + inner.length))
    }

    val newText = text.substring(0, start) + BOLD_MARKER + selectedText + BOLD_MARKER + text.substring(end)
    return TextFieldValue(newText, TextRange(start + markerLen, end + markerLen))
}
