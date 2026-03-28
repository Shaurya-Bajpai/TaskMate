package com.example.taskmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.example.taskmate.color.TaskMateColors
import com.example.taskmate.home.TaskMateHomeScreen
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.taskmate.viewmodel.TodoViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = TaskMateColors.BackgroundDark.toArgb()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        val initialTaskId = intent.getLongExtra("taskId", -1L)
        setContent {
            val viewModel: TodoViewModel = hiltViewModel()
            TaskMateHomeScreen(viewModel, initialTaskId)
        }
    }
}