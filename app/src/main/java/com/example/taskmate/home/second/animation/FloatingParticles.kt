package com.example.taskmate.home.second.animation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun FloatingParticles() {
    val particles = remember {
        List(30) { i ->
            Particle(
                x = Random.nextInt(0, 1000).toFloat(),
                y = Random.nextInt(1800, 2200).toFloat(),
                size = Random.nextInt(1, 8).toFloat(),
                speed = Random.nextFloat() * (1.5f - 0.3f) + 0.3f,
                alpha = Random.nextFloat() * (0.8f - 0.2f) + 0.2f
            )
        }
    }

    particles.forEachIndexed { i, particle ->
        val infiniteTransition = rememberInfiniteTransition()

        val animatedY by infiniteTransition.animateFloat(
            initialValue = particle.y,
            targetValue = -200f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (8000 / particle.speed).toInt(),
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Restart
            )
        )

        val animatedX by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = sin(i * 0.5f) * 40f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (3000 / particle.speed).toInt(),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        )

        val animatedAlpha by infiniteTransition.animateFloat(
            initialValue = particle.alpha * 0.3f,
            targetValue = particle.alpha,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (2000 / particle.speed).toInt(),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        )

        val scale by infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = (4000 / particle.speed).toInt(),
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        )

        Box(
            modifier = Modifier
                .offset(
                    x = (particle.x + animatedX).dp,
                    y = animatedY.dp
                )
                .scale(scale)
                .size(particle.size.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = animatedAlpha),
                            Color.Transparent
                        ),
                        radius = particle.size * 2
                    ),
                    CircleShape
                )
        )
    }
}