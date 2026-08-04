package com.example.taskmate.home.second.item

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.animation.core.animateTo
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import com.example.taskmate.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.*
import com.example.taskmate.data.Todo
import com.example.taskmate.home.first.dialogs.DeleteDialog
import com.example.taskmate.home.second.buttons.IconButton
import com.example.taskmate.home.second.formatDate
import com.example.taskmate.home.second.getPriorityColor
import com.example.taskmate.ui.theme.HighlightColor
import kotlinx.coroutines.launch
import kotlin.math.roundToInt


private val BOLD_REGEX = Regex("\\*\\*(.+?)\\*\\*")

@Composable
fun TodoItem(
    todo: Todo,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClickEdit: () -> Unit,
    isHighlighted: Boolean = false,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onLongPress: () -> Unit,
    onSelectionToggle: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Swipe-right to toggle complete, replacing the old tap-the-checkbox interaction. The card
    // itself slides right on drag, revealing a colored action panel underneath; past the
    // threshold, releasing fires onToggleComplete(), then the card always animates back to rest
    // — this toggles state in place rather than dismissing/removing the item from the list.
    var isDragging by remember { mutableStateOf(false) }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }
    val swipeOffset = remember { Animatable(0f) }
    val currentOffsetPx = if (isDragging) dragOffsetPx else swipeOffset.value
    val density = LocalDensity.current
    val swipeThresholdPx = with(density) { 88.dp.toPx() }
    val swipeMaxPx = with(density) { 120.dp.toPx() }

    // Swipe-left reveals a delete button
    val deleteOpenPx = with(density) { 76.dp.toPx() }
    val deleteThresholdPx = deleteOpenPx / 2f

    val animatedAlpha by animateFloatAsState(
        targetValue = if (todo.isCompleted) 0.7f else 1f,
        animationSpec = tween(300)
    )

    val cardColor by animateColorAsState(
        targetValue = if (isSelected) {
            HighlightColor.copy(alpha = 0.3f)
        } else {
            Color(0xFF2D3748).copy(alpha = 0.95f)
        },
        animationSpec = tween(200)
    )

    // The accent stripe/category badge normally follow priority color, but while selected they
    // switch to the selection color too — otherwise priority-orange and selection-indigo clashed
    // into a muddy mix instead of reading as one cohesive "selected" state.
    val accentColor by animateColorAsState(
        targetValue = if (isSelected) HighlightColor else getPriorityColor(todo.priority),
        animationSpec = tween(200)
    )

    // A notification tap points the user at this task via a soft pulsing glow border — not a
    // recolored card, which read as an ugly flat color swap rather than a highlight. The pulse
    // rides on top of highlightGlow's fade in/out, so it fades to nothing once isHighlighted
    // flips false again 10s later, no separate "return to normal" logic needed.
    val highlightGlow by animateFloatAsState(
        targetValue = if (isHighlighted) 1f else 0f,
        animationSpec = tween(400)
    )
    val highlightPulse by rememberInfiniteTransition(label = "highlightPulse").animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "highlightPulseValue"
    )
    val highlightAlpha = highlightGlow * highlightPulse

    Box(modifier = Modifier.fillMaxWidth()) {
        // Action panel revealed behind the card as it slides on swipe: dragging right previews
        // the complete/undo action (unchanged), dragging left reveals a delete bin instead.
        if (!isSelectionMode) {
            when {
                currentOffsetPx > 0f -> {
                    val swipeProgress = (currentOffsetPx / swipeThresholdPx).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(20.dp))
                            .background(
                                if (todo.isCompleted) Color(0xFFF59E0B) else Color(
                                    0xFF10B981
                                )
                            ),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(start = 28.dp)
                                .alpha(swipeProgress),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (todo.isCompleted) Icons.Default.Close else Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (todo.isCompleted) {
                                    stringResource(id = R.string.filter_active)
                                } else {
                                    stringResource(id = R.string.done)
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                currentOffsetPx < 0f -> {
                    val deleteRevealProgress = (-currentOffsetPx / deleteOpenPx).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFEF4444)),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(end = 28.dp)
                                .alpha(deleteRevealProgress)
                                .clickable(onClick = { showDeleteConfirm = true }),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = stringResource(id = R.string.delete),
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(currentOffsetPx.roundToInt(), 0) }
            .then(
                if (!isSelectionMode) {
                    Modifier.pointerInput(todo.id, todo.isCompleted) {
                        detectHorizontalDragGestures(
                            onDragStart = {
                                isDragging = true
                                dragOffsetPx = swipeOffset.value
                            },
                            onDragEnd = {
                                isDragging = false
                                val finalOffsetPx = dragOffsetPx
                                coroutineScope.launch {
                                    swipeOffset.snapTo(finalOffsetPx)
                                    when {
                                        finalOffsetPx > swipeThresholdPx -> {
                                            onToggleComplete()
                                            swipeOffset.animateTo(0f, animationSpec = tween(250))
                                        }
                                        finalOffsetPx < -deleteThresholdPx -> {
                                            swipeOffset.animateTo(-deleteOpenPx, animationSpec = tween(200))
                                        }
                                        else -> {
                                            swipeOffset.animateTo(0f, animationSpec = tween(250))
                                        }
                                    }
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                                val finalOffsetPx = dragOffsetPx
                                coroutineScope.launch {
                                    swipeOffset.snapTo(finalOffsetPx)
                                    swipeOffset.animateTo(0f, animationSpec = tween(250))
                                }
                            },
                            onHorizontalDrag = { change, dragAmount ->
                                change.consume()
                                dragOffsetPx = (dragOffsetPx + dragAmount).coerceIn(-deleteOpenPx, swipeMaxPx)
                            }
                        )
                    }
                } else {
                    Modifier
                }
            )
            .alpha(animatedAlpha)
            .then(
                if (highlightAlpha > 0f) {
                    Modifier.border(
                        width = 1.5.dp,
                        color = HighlightColor.copy(alpha = highlightAlpha),
                        shape = RoundedCornerShape(20.dp)
                    )
                } else {
                    Modifier
                }
            )
            .shadow(
                elevation = lerp(
                    if (todo.isCompleted) 4.dp else 8.dp,
                    14.dp,
                    highlightAlpha
                ),
                shape = RoundedCornerShape(20.dp),
                ambientColor = lerp(getPriorityColor(todo.priority),
                    HighlightColor, highlightGlow).copy(alpha = 0.3f + highlightAlpha * 0.25f),
                spotColor = lerp(getPriorityColor(todo.priority),
                    HighlightColor, highlightGlow).copy(alpha = 0.3f + highlightAlpha * 0.25f)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (isSelectionMode) {
                            onSelectionToggle()
                        } else if (swipeOffset.value != 0f) {
                            coroutineScope.launch { swipeOffset.animateTo(0f, animationSpec = tween(200)) }
                        } else {
                            expanded = !expanded
                        }
                    },
                    onLongPress = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress()
                    }
                )
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        // A priority-colored accent stripe on the leading edge, so priority reads at a glance
        // while scanning the list. Drawn directly (not a separate IntrinsicSize.Min-measured
        // Row) so it just tracks whatever height this Box ends up with each frame — including
        // while the description below is mid collapse/expand animation — instead of lagging a
        // beat behind it, which an intrinsic-measurement pass on an animating child caused.
        val stripeWidthPx = with(density) { 4.dp.toPx() }
        Box(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.14f),
                            Color.Transparent
                        )
                    )
                )
                .drawBehind {
                    drawRect(color = accentColor, size = androidx.compose.ui.geometry.Size(stripeWidthPx, size.height))
                }
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Selection checkbox in selection mode; otherwise a non-interactive status
                    // dot — completion is now toggled by swiping the card, not tapping this.
                    if (isSelectionMode) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onSelectionToggle() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF6366F1),
                                uncheckedColor = Color.White.copy(alpha = 0.5f),
                                checkmarkColor = Color.White
                            )
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    // Category badge — a tinted icon chip reads much better at a glance than a
                    // bare inline emoji sitting flush against the title text.
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(accentColor.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = stringResource(todo.category.icon), fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Task content
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = todo.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (todo.isCompleted) Color.White.copy(alpha = 0.6f) else Color.White,
                            textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Due date and priority
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Priority badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = getPriorityColor(todo.priority).copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, getPriorityColor(todo.priority).copy(alpha = 0.35f))
                            ) {
                                Text(
                                    text = stringResource(todo.priority.displayName),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = getPriorityColor(todo.priority)
                                )
                            }

                            // Due date
                            todo.dueDate?.let { dueDate ->
                                val isOverdue = dueDate < System.currentTimeMillis() && !todo.isCompleted
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isOverdue) Color(0xFFEF4444).copy(alpha = 0.2f) else Color(0xFF6B7280).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = formatDate(context, dueDate),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 10.sp,
                                        color = if (isOverdue) Color(0xFFEF4444) else Color.White.copy(alpha = 0.7f
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Action buttons - only show edit button and expand indicator
                    if (!isSelectionMode) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            IconButton(
                                icon = Icons.Default.Edit,
                                onClick = onClickEdit,
                                backgroundColor = Color(0xFF6366F1).copy(alpha = 0.2f),
                                iconColor = Color(0xFF6366F1),
                                size = 36.dp
                            )

                            // Expand/collapse indicator
                            if (todo.description.isNotBlank()) {
                                IconButton(onClick = { expanded = !expanded }, modifier = Modifier.size(24.dp)) {
                                    Icon(
                                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                        contentDescription = if (expanded) stringResource(R.string.task_item_collapse) else stringResource(R.string.task_item_expand),
                                        tint = Color.White.copy(alpha = 0.6f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Expandable description section
                AnimatedVisibility(
                    visible = expanded && todo.description.isNotBlank(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.1f))
                            .padding(20.dp)
                    ) {
                        Text(
                            text = buildFormattedText(todo.description, stripMarkers = true),
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        DeleteDialog(
            onDismiss = {
                showDeleteConfirm = false
                coroutineScope.launch { swipeOffset.animateTo(0f, animationSpec = tween(200)) }
            },
            onConfirm = {
                showDeleteConfirm = false
                onDelete()
            }
        )
    }
}

/**
 * Renders [raw] markdown-lite text as styled [AnnotatedString].
 * [stripMarkers] removes the `**` marker characters — pass true for read-only display
 * (the task list); the description edit field always shows plain, unstyled text instead,
 * so formatting only becomes visible once the task is saved.
 */
fun buildFormattedText(raw: String, stripMarkers: Boolean = false): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        for (match in BOLD_REGEX.findAll(raw)) {
            if (match.range.first > cursor) {
                append(raw.substring(cursor, match.range.first))
            }
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append(if (stripMarkers) match.groupValues[1] else match.value)
            }
            cursor = match.range.last + 1
        }
        if (cursor < raw.length) append(raw.substring(cursor))
    }
}
