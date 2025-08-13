package com.example.to_dolist.home.task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.to_dolist.R
import com.example.to_dolist.color.TaskMateColors
import com.example.to_dolist.data.Todo
import com.example.to_dolist.home.dialogs.DeleteDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoItem(
    todo: Todo,
    onClickEdit: () -> Unit,
    onClickDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Black.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = TaskMateColors.CardDark
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(TaskMateColors.cardGradient)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Task icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    TaskMateColors.PrimaryPurple.copy(alpha = 0.2f),
                                    TaskMateColors.PrimaryPurple.copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_task_24),
                        contentDescription = null,
                        tint = TaskMateColors.PrimaryPurple,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Task title
                Text(
                    text = todo.title,
                    modifier = Modifier.weight(1f),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = TaskMateColors.TextPrimary,
                    lineHeight = 24.sp
                )

                // Action buttons
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        icon = Icons.Default.Edit,
                        onClick = onClickEdit,
                        backgroundColor = TaskMateColors.PrimaryPurple.copy(alpha = 0.2f),
                        iconColor = TaskMateColors.PrimaryPurple
                    )

                    IconButton(
                        icon = Icons.Default.Delete,
                        onClick = { showDeleteDialog = true },
                        backgroundColor = TaskMateColors.Error.copy(alpha = 0.2f),
                        iconColor = TaskMateColors.Error
                    )
                }
            }
        }
    }

    // Delete confirmation dialog
    if (showDeleteDialog) {
        DeleteDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                onClickDelete()
            }
        )
    }
}
