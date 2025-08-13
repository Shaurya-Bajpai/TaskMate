package com.example.to_dolist.home.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.example.to_dolist.R
import com.example.to_dolist.color.TaskMateColors

@Composable
fun SplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TaskMateColors.gradientBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = painterResource(R.drawable.baseline_task_24),
                contentDescription = null,
                tint = TaskMateColors.PrimaryPurple,
                modifier = Modifier.size(100.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "TaskMate",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TaskMateColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Organize your day",
                fontSize = 16.sp,
                color = TaskMateColors.TextSecondary
            )
        }
    }
}