package com.example.taskmate.home.second.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.taskmate.R
import kotlinx.coroutines.delay

private val DeleteRed = Color(0xFFEF4444)
private val DeleteGradient = Brush.horizontalGradient(colors = listOf(Color(0xFFDC2626), DeleteRed))

@Composable
fun DeleteDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    // Same animated-Dialog foundation as ExitDialog (fade+scale scrim/card, dismissal deferred
    // until the exit animation actually finishes) and the same gradient-banner-with-overlapping-
    // badge layout, themed red for a destructive action instead of ExitDialog's brand gradient.
    var visible by remember { mutableStateOf(false) }
    var pendingCallback by remember { mutableStateOf<(() -> Unit)?>(null) }

    LaunchedEffect(Unit) { visible = true }

    LaunchedEffect(visible) {
        if (!visible) {
            delay(200)
            pendingCallback?.invoke()
        }
    }

    fun close(action: () -> Unit) {
        pendingCallback = action
        visible = false
    }

    val scrimAlpha by animateFloatAsState(
        targetValue = if (visible) 0.65f else 0f,
        animationSpec = tween(220),
        label = "scrimAlpha"
    )

    // A gentle pulsing glow behind the icon draws the eye to the destructive action without
    // needing a static, always-loud red flash.
    val iconPulse by rememberInfiniteTransition(label = "deleteIconPulse").animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "deleteIconPulseValue"
    )

    Dialog(
        onDismissRequest = { close(onDismiss) },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = scrimAlpha))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { close(onDismiss) },
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(220)) + scaleIn(
                    initialScale = 0.82f,
                    animationSpec = tween(260, easing = FastOutSlowInEasing)
                ),
                exit = fadeOut(tween(160)) + scaleOut(
                    targetScale = 0.88f,
                    animationSpec = tween(160)
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .widthIn(max = 340.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { /* absorb taps so they don't fall through to the scrim behind */ },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D3748)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Gradient banner with the icon badge overlapping its bottom edge —
                            // gives the card a front "cover" instead of everything reading as one
                            // flat stack.
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .background(DeleteGradient)
                            )

                            Box(
                                modifier = Modifier
                                    .offset(y = (-32).dp)
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF2D3748)),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .scale(iconPulse)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    DeleteRed.copy(alpha = 0.35f),
                                                    DeleteRed.copy(alpha = 0.1f)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = DeleteRed,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .padding(horizontal = 28.dp)
                                    .padding(bottom = 28.dp)
                                    .offset(y = (-20).dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = stringResource(id = R.string.delete_task_title),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = stringResource(id = R.string.delete_task_desc),
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 20.sp
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { close(onDismiss) },
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = Color.White.copy(alpha = 0.85f)
                                        ),
                                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                                    ) {
                                        Text(stringResource(id = R.string.cancel), fontWeight = FontWeight.Medium)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(DeleteGradient)
                                            .clickable { close(onConfirm) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(id = R.string.delete),
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
