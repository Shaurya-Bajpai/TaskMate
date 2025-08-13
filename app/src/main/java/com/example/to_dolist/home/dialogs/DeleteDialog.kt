package com.example.to_dolist.home.dialogs

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.to_dolist.color.TaskMateColors

@Composable
fun DeleteDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = TaskMateColors.Error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "Delete Task",
                color = TaskMateColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete this task? This action cannot be undone.",
                color = TaskMateColors.TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = TaskMateColors.Error
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Delete", color = TaskMateColors.TextPrimary)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = TaskMateColors.TextSecondary
                )
            ) {
                Text("Cancel")
            }
        },
        containerColor = TaskMateColors.SurfaceDark,
        shape = RoundedCornerShape(16.dp)
    )
}