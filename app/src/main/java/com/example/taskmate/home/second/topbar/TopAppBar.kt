package com.example.taskmate.home.second.topbar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.taskmate.R
import com.example.taskmate.color.TaskMateColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(
    completedCount: Int,
    totalCount: Int,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    isSearchActive: Boolean,
) {
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f
    val focusRequester = remember { FocusRequester() }
    val focusManager: FocusManager = LocalFocusManager.current

    var searchFocused by remember { mutableStateOf(false) }
    val clearFocusRequester = remember { FocusRequester() }

    // Pressing back while the keyboard is showing dismisses it at the OS level and never reaches
    // Compose's back dispatcher — so there's no callback to hook for "keyboard just closed" other
    // than watching the window's actual IME-visibility inset directly. That changes no matter how
    // the keyboard closed (back press, gesture, done/search action), and only then do we drop
    // focus, so the search field's border doesn't stay stuck highlighted as if still active.
    val view = LocalView.current
    DisposableEffect(view) {
        val listener = android.view.ViewTreeObserver.OnGlobalLayoutListener {
            val imeVisible = ViewCompat.getRootWindowInsets(view)?.isVisible(WindowInsetsCompat.Type.ime()) ?: true
            if (!imeVisible && searchFocused) {
                clearFocusRequester.requestFocus()
            }
        }
        view.viewTreeObserver.addOnGlobalLayoutListener(listener)
        onDispose { view.viewTreeObserver.removeOnGlobalLayoutListener(listener) }
    }

    // Height animation
    val topBarHeight by animateDpAsState(
        targetValue = if (isSearchActive) 100.dp else 180.dp,
        animationSpec = tween(300)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(topBarHeight),
        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(TaskMateColors.primaryGradient)) {
            Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
                // Invisible focus target: requesting focus here (instead of trying to "clear"
                // focus) is what reliably drops the search field's focused-border rendering.
                Box(
                    modifier = Modifier
                        .size(1.dp)
                        .focusRequester(clearFocusRequester)
                        .focusTarget()
                )
                // Header with stats (hide when search is active)
                AnimatedVisibility(
                    visible = !isSearchActive,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = stringResource(id = R.string.app_name),
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            AnimatedVisibility(visible = totalCount > 0) {
                                Text(
                                    text = stringResource(id = R.string.tasks_completed_format, completedCount, totalCount),
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }

                        // Circular progress
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = progress,
                                modifier = Modifier.size(50.dp),
                                color = Color.White,
                                strokeWidth = 4.dp,
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )
                            Text(
                                text = "${(progress * 100).toInt()}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                // Always show search field, but with different spacing
                Spacer(modifier = Modifier.height(if (isSearchActive) 8.dp else 16.dp))

                // Enhanced Search field
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchChange,
                        placeholder = {
                            Text(
                                if (isSearchActive) stringResource(id = R.string.search_active_placeholder) else stringResource(id = R.string.search_inactive_placeholder),
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 16.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f)
                            )
                        },
                        trailingIcon = {
                            AnimatedVisibility(
                                visible = searchQuery.isNotEmpty(),
                                enter = fadeIn() + scaleIn(),
                                exit = fadeOut() + scaleOut()
                            ) {
                                IconButton(onClick = { onSearchChange("") }) {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = stringResource(R.string.clear_search),
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .focusRequester(focusRequester)
                            .onFocusChanged { searchFocused = it.isFocused },
                        textStyle = LocalTextStyle.current.copy(
                            color = Color.White,
                            fontSize = 16.sp
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Color.White,
                            focusedBorderColor = Color.White.copy(alpha = 0.8f),
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search, autoCorrect = true, capitalization = KeyboardCapitalization.Words),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        singleLine = true
                    )

                    // Search mode toggle button
                    AnimatedVisibility(
                        visible = isSearchActive || searchQuery.isNotEmpty(),
                        enter = expandHorizontally() + fadeIn(),
                        exit = shrinkHorizontally() + fadeOut()
                    ) {
                        TextButton(
                            onClick = {
                                onSearchChange("")
                                focusManager.clearFocus()
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color.White
                            ),
                        ) {
                            Text(stringResource(id = R.string.cancel), fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}