package com.example.taskmate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.taskmate.color.TaskMateColors
import com.example.taskmate.database.TodoDatabase
import com.example.taskmate.database.TodoRepository
import com.example.taskmate.viewmodel.TodoViewModel
import com.example.taskmate.viewmodel.TodoViewModelFactory
import com.example.to_dolist.home.TaskMateHomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = TaskMateColors.BackgroundDark.toArgb()
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false
        val database = Room.databaseBuilder(applicationContext, TodoDatabase::class.java, "todo_database").build()
        val todoDao = database.todoDao()
        setContent {
            val repository = TodoRepository(todoDao)
            val factory = TodoViewModelFactory(repository)
            val viewModel: TodoViewModel = viewModel(factory = factory)
            TaskMateHomeScreen(viewModel)
        }
    }
}