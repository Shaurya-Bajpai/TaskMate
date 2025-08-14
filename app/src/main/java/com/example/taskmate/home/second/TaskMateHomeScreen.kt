package com.example.to_dolist.home

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.example.taskmate.data.*
import com.example.taskmate.home.second.FilterType
import com.example.taskmate.home.second.animation.FloatingParticles
import com.example.taskmate.home.second.buttons.FloatingActionButton
import com.example.taskmate.home.second.chips.FilterChipRow
import com.example.taskmate.home.second.dialogs.*
import com.example.taskmate.home.second.item.TodoItem
import com.example.taskmate.home.second.splash.SplashScreen
import com.example.taskmate.home.second.state.EmptyState
import com.example.taskmate.home.second.state.NoResultsState
import com.example.taskmate.home.second.topbar.TopAppBar
import com.example.taskmate.viewmodel.TodoViewModel
import kotlinx.coroutines.delay

@Composable
fun TaskMateHomeScreen(viewModel: TodoViewModel) {
    val activity = LocalContext.current as? Activity
    val focusManager = LocalFocusManager.current
    var editingTodo by remember { mutableStateOf<Todo?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(FilterType.ALL) }
    var isSearchActive by remember { mutableStateOf(false) }

    // Dialog states
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Selection state for multiple selection
    var selectedTodos by remember { mutableStateOf(setOf<Long>()) }
    var isSelectionMode by remember { mutableStateOf(false) }

    val todoList by viewModel.getAllTask.collectAsState(initial = emptyList())
    val completedCount by viewModel.completedTaskCount.collectAsState(initial = 0)
    val totalCount by viewModel.totalTaskCount.collectAsState(initial = 0)

    // Filter logic
    val filteredTodos = remember(todoList, searchQuery, selectedFilter) {
        todoList.filter { todo ->
            val matchesSearch = todo.title.contains(searchQuery, ignoreCase = true) ||
                    todo.description.contains(searchQuery, ignoreCase = true)
            val matchesFilter = when (selectedFilter) {
                FilterType.ALL -> true
                FilterType.ACTIVE -> !todo.isCompleted
                FilterType.COMPLETED -> todo.isCompleted
                FilterType.HIGH_PRIORITY -> todo.priority == Priority.HIGH
                FilterType.MEDIUM_PRIORITY -> todo.priority == Priority.MEDIUM
                FilterType.LOW_PRIORITY -> todo.priority == Priority.LOW
            }
            matchesSearch && matchesFilter
        }
    }

    BackHandler {
        if (isSelectionMode) {
            isSelectionMode = false // Cancel selection
        } else {
            showExitDialog = true // Show exit dialog
        }
    }

    // Clear selection when not in selection mode
    LaunchedEffect(isSelectionMode) {
        if (!isSelectionMode) {
            selectedTodos = emptySet()
        }
    }

    // Simulate loading state
    LaunchedEffect(Unit) {
        isLoading = true
        delay(3010) // Enhanced splash screen
        isLoading = false
    }

    Box(modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            )
        .pointerInput(Unit) {
            detectTapGestures(onTap = {
                if (isSearchActive) {
                    focusManager.clearFocus()
                    isSearchActive = false
                }
            })
        }) {
        if (isLoading) {
            SplashScreen()
        } else {
            Column {
                // Enhanced Top Bar with Statistics
                TopAppBar(
                    completedCount = completedCount,
                    totalCount = totalCount,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                    isSearchActive = isSearchActive,
                )

                // Filter Chips (hide when search is active)
                AnimatedVisibility(
                    visible = !isSearchActive,
                    enter = slideInVertically() + fadeIn(),
                    exit = slideOutVertically() + fadeOut()
                ) {
                    FilterChipRow(
                        selectedFilter = selectedFilter,
                        onFilterSelected = { selectedFilter = it }
                    )
                }

                // Selection mode header

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        filteredTodos.isEmpty() -> {
                            if (todoList.isEmpty()) {
                                EmptyState()
                            } else {
                                NoResultsState(searchQuery)
                            }
                        }
                        else -> {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                contentPadding = PaddingValues(bottom = 100.dp, top = 16.dp)
                            ) {
                                itemsIndexed(
                                    items = filteredTodos,
                                    key = { _, todo -> todo.id }
                                ) { index, todo ->
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = true,
                                        enter = slideInVertically(
                                            initialOffsetY = { it },
                                            animationSpec = tween(
                                                durationMillis = 400,
                                                delayMillis = index * 50
                                            )
                                        ) + fadeIn(
                                            animationSpec = tween(400, delayMillis = index * 50)
                                        ) + scaleIn(
                                            animationSpec = tween(400, delayMillis = index * 50),
                                            initialScale = 0.8f
                                        )
                                    ) {
                                        TodoItem(
                                            todo = todo,
                                            isSelected = selectedTodos.contains(todo.id),
                                            isSelectionMode = isSelectionMode,
                                            onClickEdit = {
                                                editingTodo = todo
                                                showDialog = true
                                            },
                                            onToggleComplete = {
                                                viewModel.updateTask(todo.copy(isCompleted = !todo.isCompleted))
                                            },
                                            onLongPress = {
                                                if (!isSelectionMode) {
                                                    isSelectionMode = true
                                                    selectedTodos = setOf(todo.id)
                                                }
                                            },
                                            onSelectionToggle = {
                                                if (isSelectionMode) {
                                                    selectedTodos = if (selectedTodos.contains(todo.id)) {
                                                        selectedTodos - todo.id
                                                    } else {
                                                        selectedTodos + todo.id
                                                    }
                                                    // Exit selection mode if no items selected
                                                    if (selectedTodos.isEmpty()) {
                                                        isSelectionMode = false
                                                    }
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Enhanced FAB with conditional appearance
                    if (isSelectionMode) {
                        DeleteFAB(
                            onClick = {
                                showDeleteDialog = true
                            }
                        )
                    } else {
                        FloatingActionButton(
                            onClick = {
                                editingTodo = null
                                showDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Floating particles effect
        FloatingParticles()
    }

    if (showDialog) {
        TaskDialog(
            todo = editingTodo,
            onDismiss = {
                showDialog = false
                editingTodo = null
            },
            onConfirm = { todo ->
                showDialog = false
                if (editingTodo != null) {
                    viewModel.updateTask(todo)
                } else {
                    viewModel.addTask(todo)
                }
                editingTodo = null
            }
        )
    }

    if (showDeleteDialog) {
        DeleteDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                showDeleteDialog = false
                selectedTodos.forEach { todoId ->
                    todoList.find { it.id == todoId }?.let { todo ->
                        viewModel.deleteTask(todo)
                    }
                }
                isSelectionMode = false
                selectedTodos = emptySet()
            }
        )
    }

    if (showExitDialog) {
        ExitDialog(
            onDismiss = { showExitDialog = false },
            onConfirm = {
                activity?.finish()
            }
        )
    }
}