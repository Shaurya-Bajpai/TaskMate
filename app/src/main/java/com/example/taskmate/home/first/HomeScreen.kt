package com.example.taskmate.home.first

//@Composable
//fun HomeScreen(viewModel: TodoViewModel) {
//    var showDialog by remember { mutableStateOf(false) }
//    var id by remember { mutableStateOf(0L) }
//    var isLoading by remember { mutableStateOf(true) }
//
//    val todoList by viewModel.getAllTask.collectAsState(initial = emptyList())
//
//    // Simulate loading state
//    LaunchedEffect(Unit) {
//        isLoading = true
//        delay(2000) // Simulate a 2-second splash screen
//        isLoading = false
//    }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(TaskMateColors.gradientBackground)
//    ) {
//        if (isLoading) {
//            SplashScreen()
//        } else {
//            Column {
//                TopBar()
//
//                Box(modifier = Modifier.fillMaxSize()) {
//                    when {
//                        todoList.isEmpty() -> EmptyState()
//                        else -> {
//                            LazyColumn(
//                                modifier = Modifier
//                                    .fillMaxSize()
//                                    .padding(horizontal = 16.dp),
//                                verticalArrangement = Arrangement.spacedBy(12.dp),
//                                contentPadding = PaddingValues(bottom = 100.dp, top = 16.dp)
//                            ) {
//                                itemsIndexed(
//                                    items = todoList,
//                                    key = { _, todo -> todo.id }
//                                ) { index, todo ->
//                                    Box(modifier = Modifier.fillMaxWidth()) {
//                                        androidx.compose.animation.AnimatedVisibility(
//                                            visible = true,
//                                            modifier = Modifier.fillMaxWidth(),
//                                            enter = slideInVertically(
//                                                initialOffsetY = { it },
//                                                animationSpec = tween(
//                                                    durationMillis = 300,
//                                                    delayMillis = index * 50
//                                                )
//                                            ) + fadeIn()
//                                        ) {
//                                            TodoItem(
//                                                todo = todo,
//                                                onClickEdit = {
//                                                    id = todo.id
//                                                    viewModel.titleChange(todo.title)
//                                                    showDialog = true
//                                                },
//                                                onClickDelete = {
//                                                    viewModel.deleteTask(todo = todo)
//                                                }
//                                            )
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//
//                    FloatingActionButton(
//                        onClick = {
//                            id = 0L
//                            showDialog = true
//                        },
//                        modifier = Modifier
//                            .align(Alignment.BottomEnd)
//                            .padding(24.dp)
//                            .scale(1f)
//                            .shadow(
//                                elevation = 12.dp,
//                                shape = CircleShape,
//                                ambientColor = TaskMateColors.PrimaryPurple,
//                                spotColor = TaskMateColors.PrimaryPurple
//                            ),
//                        containerColor = TaskMateColors.PrimaryPurple,
//                        contentColor = TaskMateColors.TextPrimary
//                    ) {
//                        Icon(
//                            imageVector = Icons.Default.Add,
//                            contentDescription = "Add Task",
//                            modifier = Modifier.size(28.dp)
//                        )
//                    }
//                }
//            }
//        }
//    }
//
//    if (showDialog) {
//        TaskDialog(
//            isEdit = id != 0L,
//            title = viewModel.title,
//            onTitleChange = { viewModel.titleChange(it) },
//            onDismiss = {
//                showDialog = false
//                viewModel.titleChange("") // Clear the title when dialog is dismissed
//            },
//            onConfirm = {
//                showDialog = false
//                if (viewModel.title.isNotEmpty()) {
//                    if (id != 0L) {
//                        viewModel.updateTask(
//                            Todo(
//                                id = id,
//                                title = viewModel.title.trim(),
//                            )
//                        )
//                    } else {
//                        viewModel.addTask(
//                            Todo(
//                                title = viewModel.title.trim(),
//                            )
//                        )
//                    }
//                }
//            }
//        )
//    }
//}