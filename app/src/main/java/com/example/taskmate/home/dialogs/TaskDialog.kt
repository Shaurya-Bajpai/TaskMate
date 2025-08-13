package com.example.taskmate.home.dialogs

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.DialogProperties
import com.example.taskmate.color.TaskMateColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(isEdit: Boolean, title: String, onTitleChange: (String) -> Unit, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = TaskMateColors.SurfaceDark
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = if (isEdit) "Edit Task" else "Add New Task",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TaskMateColors.TextPrimary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    label = { Text("Task description", color = TaskMateColors.TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TaskMateColors.TextPrimary,
                        unfocusedTextColor = TaskMateColors.TextPrimary,
                        cursorColor = TaskMateColors.PrimaryPurple,
                        focusedBorderColor = TaskMateColors.PrimaryPurple,
                        unfocusedBorderColor = TaskMateColors.TextSecondary
                    ),
                    maxLines = 4,
                    keyboardOptions = KeyboardOptions.Default.copy(
                        autoCorrect = true,
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TaskMateColors.TextSecondary
                        )
                    ) {
                        Text("Cancel", modifier = Modifier.padding(vertical = 4.dp))
                    }

                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                onConfirm()
                            } else {
                                Toast.makeText(context, "Description cannot be empty", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = TaskMateColors.PrimaryPurple
                        )
                    ) {
                        Text(
                            text = if (isEdit) "Update" else "Add",
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = TaskMateColors.TextPrimary
                        )
                    }
                }
            }
        }
    }
}
