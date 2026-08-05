package com.example.taskmate.home.second.dialogs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
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
import com.example.taskmate.color.TaskMateColors
import kotlinx.coroutines.delay

private val ExitBlue = Color(0xFF4491EF)

@Composable
fun ExitDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    // Same animated-Dialog foundation as DeleteDialog (fade+scale scrim/card, dismissal deferred
    // until the exit animation actually finishes), with the visual treatment pushed further: a
    // gradient banner behind an overlapping icon badge, and a swinging-door icon animation instead
    // of a plain static glyph.
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

    // A gentle continuous pendulum swing on the door icon, like it's swinging open and shut on
    // its own — a quieter, ambient version of the same "this icon should feel alive" idea as
    // DeleteDialog's pulsing glow.
    val doorSwing by rememberInfiniteTransition(label = "doorSwing").animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "doorSwingValue"
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
                    initialScale = 0.8f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
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
                            // Gradient banner with the icon badge overlapping its bottom edge,
                            // in place of Delete's plain in-column icon — gives the card a front
                            // "cover" instead of everything reading as one flat stack.
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(72.dp)
                                    .background(TaskMateColors.primaryGradient)
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
                                        .clip(CircleShape)
                                        .background(
                                            Brush.radialGradient(
                                                colors = listOf(
                                                    ExitBlue.copy(alpha = 0.35f),
                                                    ExitBlue.copy(alpha = 0.1f)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ExitToApp,
                                        contentDescription = null,
                                        tint = ExitBlue,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .rotate(doorSwing)
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
                                    text = stringResource(id = R.string.exit_title),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = stringResource(id = R.string.exit_desc),
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
                                            .background(TaskMateColors.primaryGradient)
                                            .clickable { close(onConfirm) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.ExitToApp,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = stringResource(id = R.string.exit_title),
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
