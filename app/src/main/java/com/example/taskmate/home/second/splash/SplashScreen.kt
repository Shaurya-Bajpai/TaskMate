package com.example.taskmate.home.second.splash

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.example.taskmate.R

@Composable
fun SplashScreen() {
    val infiniteTransition = rememberInfiniteTransition()

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    val textScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1E1B4B),
                        Color(0xFF312E81),
                        Color(0xFF0F0A1E)
                    ),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Background particles
        repeat(25) { i ->
            val particleScale by infiniteTransition.animateFloat(
                initialValue = 0.5f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween((2000..4000).random(), easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                )
            )

            Box(modifier = Modifier
                    .offset(x = ((-200..200).random()).dp, y = ((-400..400).random()).dp)
                    .scale(particleScale)
                    .size((2..6).random().dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape))
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.animateContentSize()) {
            Box(contentAlignment = Alignment.Center) {
                // Outer glow ring
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(pulseScale)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF6366F1).copy(alpha = glowAlpha * 0.3f),
                                    Color.Transparent
                                ),
                                radius = 200f
                            ),
                            CircleShape
                        )
                )

                // Middle ring
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(scale)
                        .rotate(-rotation * 0.7f)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF8B5CF6).copy(alpha = 0.4f),
                                    Color(0xFF6366F1).copy(alpha = 0.6f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        )
                )

                // Inner rotating ring
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .scale(scale)
                        .rotate(rotation)
                        .background(
                            Brush.sweepGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFF6366F1).copy(alpha = 0.5f),
                                    Color(0xFFEF4444).copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Icon container with glow
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        Color(0xFF6366F1).copy(alpha = 0.2f),
                                        Color.Transparent
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(R.drawable.taskmate),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .scale(textScale)
                                .clip(CircleShape),
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "TaskMate",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.scale(textScale)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "✨ Organize • Prioritize • Achieve ✨",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(textScale * 0.98f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Enhanced progress bar with glow
            Box {
                LinearProgressIndicator(
                    modifier = Modifier
                        .width(220.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF6366F1).copy(alpha = 0.3f),
                                    Color(0xFF8B5CF6).copy(alpha = 0.3f)
                                )
                            )
                        ),
                    color = Color.Transparent,
                    trackColor = Color.Transparent
                )

                // Animated progress glow
                LinearProgressIndicator(
                    modifier = Modifier
                        .width(220.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = Color(0xFF6366F1).copy(alpha = glowAlpha),
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }
        }
    }
}