package com.example.taskmate.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmate.data.Category
import com.example.taskmate.data.Priority
import com.example.taskmate.data.Todo
import com.example.taskmate.database.TodoRepository
import com.example.taskmate.graph.Graph
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import java.util.Calendar

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {
    // UI State
    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    // Task data
    val getAllTask: Flow<List<Todo>> = repository.getAllTasks()
    val getActiveTasks: Flow<List<Todo>> = repository.getActiveTasks()
    val getCompletedTasks: Flow<List<Todo>> = repository.getCompletedTasks()
    val completedTaskCount: Flow<Int> = repository.getCompletedTaskCount()
    val totalTaskCount: Flow<Int> = repository.getTotalTaskCount()

    // Search functionality
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun searchTasks(query: String): Flow<List<Todo>> {
        return repository.searchTasks(query)
    }

    // Task operations
    fun addTask(todo: Todo) {
        viewModelScope.launch {
            try {
                repository.addTask(todo)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to add task: ${e.message}"
                )
            }
        }
    }

    fun updateTask(todo: Todo) {
        viewModelScope.launch {
            try {
                repository.updateTask(todo)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to update task: ${e.message}"
                )
            }
        }
    }

    fun deleteTask(todo: Todo) {
        viewModelScope.launch {
            try {
                repository.deleteTask(todo)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to delete task: ${e.message}"
                )
            }
        }
    }

    fun toggleTaskCompletion(todo: Todo) {
        updateTask(todo.copy(isCompleted = !todo.isCompleted))
    }

    // Filter functionality
    fun getTasksByCategory(category: Category): Flow<List<Todo>> {
        return repository.getTasksByCategory(category)
    }

    // Statistics
    fun getTaskStatistics(): Flow<TaskStatistics> {
        return combine(
            getAllTask,
            completedTaskCount,
            totalTaskCount
        ) { allTasks, completed, total ->
            TaskStatistics(
                totalTasks = total,
                completedTasks = completed,
                activeTasks = total - completed,
                highPriorityTasks = allTasks.count { it.priority == Priority.HIGH && !it.isCompleted },
                overdueTasks = allTasks.count {
                    it.dueDate != null &&
                            it.dueDate < System.currentTimeMillis() &&
                            !it.isCompleted
                },
                completionRate = if (total > 0) (completed.toFloat() / total * 100).toInt() else 0
            )
        }
    }

    // Clear error message
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    // Set loading state
    fun setLoading(isLoading: Boolean) {
        _uiState.value = _uiState.value.copy(isLoading = isLoading)
    }
}

// Data classes for UI state management
data class TodoUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedFilter: FilterType = FilterType.ALL,
    val selectedCategory: Category? = null
)

data class TaskStatistics(
    val totalTasks: Int,
    val completedTasks: Int,
    val activeTasks: Int,
    val highPriorityTasks: Int,
    val overdueTasks: Int,
    val completionRate: Int
)

// Filter enum for better organization
enum class FilterType(val displayName: String) {
    ALL("All Tasks"),
    ACTIVE("Active"),
    COMPLETED("Completed"),
    HIGH_PRIORITY("High Priority"),
    MEDIUM_PRIORITY("Medium Priority"),
    LOW_PRIORITY("Low Priority"),
    OVERDUE("Overdue"),
    TODAY("Due Today"),
    THIS_WEEK("This Week")
}

// Extension functions for date handling
@RequiresApi(Build.VERSION_CODES.O)
fun Long.isToday(): Boolean {
    val today = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = this@isToday }
    return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
            today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
}

fun Long.isThisWeek(): Boolean {
    val now = Calendar.getInstance()
    val date = Calendar.getInstance().apply { timeInMillis = this@isThisWeek }
    val weekStart = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val weekEnd = Calendar.getInstance().apply {
        timeInMillis = weekStart.timeInMillis
        add(Calendar.WEEK_OF_YEAR, 1)
        add(Calendar.MILLISECOND, -1)
    }
    return date.timeInMillis >= weekStart.timeInMillis && date.timeInMillis <= weekEnd.timeInMillis
}

fun Long.isOverdue(): Boolean {
    return this < System.currentTimeMillis()
}