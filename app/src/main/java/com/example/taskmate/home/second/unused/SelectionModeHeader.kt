package com.example.taskmate.home.second.unused

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*


@Composable
fun SelectionModeHeader(selectedCount: Int, onClearSelection: () -> Unit, onDeleteSelected: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF2D3748).copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$selectedCount selected",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = onClearSelection,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color.White.copy(alpha = 0.7f)
                    )
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = onDeleteSelected,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFEF4444)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }
}


// Paste this snippet on the given location to show the desired functionality
// Compare this snippet from app/src/main/java/com/example/taskmate/home/dialogs/TaskDialog.kt

//                AnimatedVisibility(
//                    visible = isSelectionMode,
//                    enter = slideInVertically() + fadeIn(),
//                    exit = slideOutVertically() + fadeOut()
//                ) {
//                    SelectionModeHeader(
//                        selectedCount = selectedTodos.size,
//                        onClearSelection = {
//                            isSelectionMode = false
//                            selectedTodos = emptySet()
//                        },
//                        onDeleteSelected = {
//                            selectedTodos.forEach { todoId ->
//                                todoList.find { it.id == todoId }?.let { todo ->
//                                    viewModel.deleteTask(todo)
//                                }
//                            }
//                            isSelectionMode = false
//                            selectedTodos = emptySet()
//                        }
//                    )
//                }