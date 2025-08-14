package com.example.taskmate.home.first

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.taskmate.R
import com.example.taskmate.color.TaskMateColors

@Composable
fun TopBar() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = TaskMateColors.SurfaceDark)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(colors = listOf(TaskMateColors.PrimaryPurple, TaskMateColors.SecondaryPurple)))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_task_24),
                    contentDescription = null,
                    tint = TaskMateColors.TextPrimary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "TaskMate",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TaskMateColors.TextPrimary
                    )
                    Text(
                        text = "Organize your day",
                        fontSize = 14.sp,
                        color = TaskMateColors.TextPrimary.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}