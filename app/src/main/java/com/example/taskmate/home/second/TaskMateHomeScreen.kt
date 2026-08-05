package com.example.taskmate.home

import android.app.Activity
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taskmate.R
import com.example.taskmate.alarm.BatteryOptimizationHelper
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
import androidx.core.content.edit

@Composable
fun TaskMateHomeScreen(viewModel: TodoViewModel, initialTaskId: Long? = null) {
    val activity = LocalContext.current as? Activity
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current


    // Alarms use AlarmManager.setAlarmClock(), which is exempt from Doze/App Standby — but several
    // OEM battery managers (Xiaomi, Oppo, Vivo, Samsung, OnePlus, ...) layer their own process-
    // killing restrictions on top of that regardless, and only the user can grant the exemption.
    // This is the most common reason a task alarm fires reliably on one phone but never on
    // another. Asked once, the first time the home screen loads on a device where the app isn't
    // already exempted.
    var showBatteryOptimizationDialog by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences("taskmate_prefs", android.content.Context.MODE_PRIVATE)
        val alreadyAsked = prefs.getBoolean("asked_battery_optimization", false)
        if (!alreadyAsked && !BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context)) {
            showBatteryOptimizationDialog = true
        }
    }

    // Direct IME-visibility signal, kept separate from isSearchActive on purpose: this only
    // controls the FAB, without pulling in isSearchActive's other side effects (header collapsing,
    // placeholder text swap, height animation).
    //
    // WindowInsets.isImeVisible doesn't reliably update here: this activity uses classic
    // windowSoftInputMode="adjustResize" (not edge-to-edge), so the OS resizes the window itself
    // instead of reporting the keyboard as an ime WindowInsets overlay — which is what that
    // Compose API actually watches. Querying the view's current root window insets directly, the
    // same way TopAppBar already does for its own keyboard-close handling, reports IME visibility
    // correctly regardless of resize vs. edge-to-edge mode.
    val view = LocalView.current
    var isKeyboardVisible by remember { mutableStateOf(false) }
    DisposableEffect(view) {
        val listener = android.view.ViewTreeObserver.OnGlobalLayoutListener {
            isKeyboardVisible = ViewCompat.getRootWindowInsets(view)?.isVisible(WindowInsetsCompat.Type.ime()) ?: false
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose { view.viewTreeObserver.removeOnGlobalLayoutListener(listener) }
    }

    var editingTodo by remember { mutableStateOf<Todo?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(FilterType.ACTIVE) }
    var isSearchActive by remember { mutableStateOf(false) }

    // Dialog states
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Selection state for multiple selection
    var selectedTodos by remember { mutableStateOf(setOf<Long>()) }
    var isSelectionMode by remember { mutableStateOf(false) }

    // Only one task's swipe-to-delete panel may be open at a time — swiping a second row open
    // auto-closes whichever row was previously open, instead of leaving multiple delete panels
    // exposed at once.
    var openSwipeTaskId by remember { mutableStateOf<Long?>(null) }

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

    // Tapping a reminder notification highlights that task in place (scrolling it into view)
    // instead of jumping straight into edit — the user just wants to be pointed at it, not
    // dropped into an editor for a task they may not want to change.
    var highlightedTaskId by remember { mutableStateOf<Long?>(null) }
    val listState = rememberLazyListState()
    var initialHandleDone by remember { mutableStateOf(false) }
    LaunchedEffect(filteredTodos, initialTaskId) {
        if (!initialHandleDone && initialTaskId != null && initialTaskId != -1L && filteredTodos.isNotEmpty()) {
            val targetIndex = filteredTodos.indexOfFirst { it.id == initialTaskId }
            if (targetIndex >= 0) {
                highlightedTaskId = initialTaskId
                listState.animateScrollToItem(targetIndex)
            }
            initialHandleDone = true
        }
    }
    LaunchedEffect(highlightedTaskId) {
        if (highlightedTaskId != null) {
            delay(10_000)
            highlightedTaskId = null
        }
    }

    BackHandler {
        when {
            isSelectionMode -> isSelectionMode = false // Cancel selection
            searchQuery.isNotEmpty() -> {
                // A prior back press may already have been consumed by the OS just to dismiss
                // the keyboard (it never reaches this handler at all) — so by the time back
                // reaches here with a query still typed, it reads as a second/next press and
                // should act like the Cancel button: clear the search instead of falling through
                // to the exit-app dialog.
                searchQuery = ""
                focusManager.clearFocus()
                isSearchActive = false
            }
            else -> showExitDialog = true // Show exit dialog
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
                            } else if (searchQuery.isEmpty()) {
                                NoResultsState(searchQuery, R.string.no_tasks_yet)
                            } else {
                                NoResultsState(searchQuery)
                            }
                        }
                        else -> {
                            LazyColumn(
                                state = listState,
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
                                            isHighlighted = highlightedTaskId == todo.id,
                                            isSwipeOpen = openSwipeTaskId == todo.id,
                                            onSwipeOpenChanged = { isOpen ->
                                                openSwipeTaskId = if (isOpen) todo.id else {
                                                    if (openSwipeTaskId == todo.id) null else openSwipeTaskId
                                                }
                                            },
                                            onClickEdit = {
                                                editingTodo = todo
                                                showDialog = true
                                            },
                                            onToggleComplete = {
                                                viewModel.updateTask(todo.copy(isCompleted = !todo.isCompleted))
                                            },
                                            onDelete = {
                                                viewModel.deleteTask(todo)
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

                    // Enhanced FAB with conditional appearance. Hidden while the keyboard is up too
                    // (isKeyboardVisible), not just during selection.
                    if (isSelectionMode) {
                        DeleteFAB(
                            onClick = {
                                showDeleteDialog = true
                            }
                        )
                    } else if (!isKeyboardVisible) {
                        FloatingActionButton(
                            onClick = {
                                editingTodo = null
                                showDialog = true
                            }
                        )
                    }
                }
            }

        // The home screen underneath is composed and ready the whole time (this is a simulated
        // loading delay, not real data-readiness), so the splash just needs to fade off the top
        // of it — a plain AnimatedVisibility exit, rather than Crossfade's AnimatedContent
        // machinery, which was fighting with the splash's own animateContentSize() and causing a
        // visible "reset" glitch right at the handoff.
        AnimatedVisibility(
            visible = isLoading,
            exit = fadeOut(animationSpec = tween(400)),
            modifier = Modifier.fillMaxSize()
        ) {
            SplashScreen()
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
                    searchQuery = ""
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
    if (showBatteryOptimizationDialog) {
        fun markAsked() {
            context.getSharedPreferences("taskmate_prefs", Context.MODE_PRIVATE)
                .edit { putBoolean("asked_battery_optimization", true) }
        }

        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                showBatteryOptimizationDialog = false
                markAsked()
            },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF6366F1).copy(alpha = 0.35f),
                                    Color(0xFF6366F1).copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color(0xFF6366F1),
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            title = {
                Text(
                    stringResource(id = R.string.battery_optimization_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    stringResource(id = R.string.battery_optimization_desc),
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showBatteryOptimizationDialog = false
                    markAsked()
                    BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
                }) {
                    Text(
                        stringResource(id = R.string.battery_optimization_allow),
                        color = Color(0xFF6366F1),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showBatteryOptimizationDialog = false
                    markAsked()
                }) {
                    Text(
                        stringResource(id = R.string.battery_optimization_not_now),
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            },
            containerColor = Color(0xFF2D3748),
            shape = RoundedCornerShape(24.dp)
        )
    }
}