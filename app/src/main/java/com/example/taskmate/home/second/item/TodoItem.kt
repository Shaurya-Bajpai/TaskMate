package com.example.taskmate.home.second.item

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.example.taskmate.data.Todo
import com.example.taskmate.home.second.buttons.IconButton
import com.example.taskmate.home.second.formatDate
import com.example.taskmate.home.second.getPriorityColor

@Composable
fun TodoItem(
    todo: Todo,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClickEdit: () -> Unit,
    onToggleComplete: () -> Unit,
    onLongPress: () -> Unit,
    onSelectionToggle: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val hapticFeedback = LocalHapticFeedback.current

    val animatedAlpha by animateFloatAsState(
        targetValue = if (todo.isCompleted) 0.7f else 1f,
        animationSpec = tween(300)
    )

    val cardColor by animateColorAsState(
        targetValue = if (isSelected) {
            Color(0xFF6366F1).copy(alpha = 0.3f)
        } else {
            Color(0xFF2D3748).copy(alpha = 0.95f)
        },
        animationSpec = tween(200)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(animatedAlpha)
            .shadow(
                elevation = if (todo.isCompleted) 4.dp else 8.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = getPriorityColor(todo.priority).copy(alpha = 0.3f),
                spotColor = getPriorityColor(todo.priority).copy(alpha = 0.3f)
            )
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (isSelectionMode) {
                            onSelectionToggle()
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
        Box(modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        getPriorityColor(todo.priority).copy(alpha = 0.1f),
                        Color.Transparent,
                        Color.Transparent
                    )
                )
            )) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Selection indicator or completion checkbox
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
                    } else {
                        Checkbox(
                            checked = todo.isCompleted,
                            onCheckedChange = { onToggleComplete() },
                            colors = CheckboxDefaults.colors(
                                checkedColor = getPriorityColor(todo.priority),
                                uncheckedColor = Color.White.copy(alpha = 0.5f),
                                checkmarkColor = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Task content
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = todo.category.icon,
                                fontSize = 16.sp,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = todo.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (todo.isCompleted) Color.White.copy(alpha = 0.6f) else Color.White,
                                textDecoration = if (todo.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Due date and priority
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Priority badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = getPriorityColor(todo.priority).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = todo.priority.displayName,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
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
                                        text = formatDate(dueDate),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        fontSize = 10.sp,
                                        color = if (isOverdue) Color(0xFFEF4444) else Color.White.copy(alpha = 0.7f)
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
                                        contentDescription = if (expanded) "Collapse" else "Expand",
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
                            text = todo.description,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}