package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.engine.SpeechState
import com.example.ui.theme.NeonCyanPrimary
import com.example.ui.theme.VioletSecondary
import kotlin.math.sin

@Composable
fun VoiceWaveformVisualizer(
    speechState: SpeechState,
    volumeLevel: Float,
    size: Dp = 180.dp,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember { Animatable(1f) }
    val rotationAnim = remember { Animatable(0f) }

    LaunchedEffect(speechState, volumeLevel) {
        if (speechState is SpeechState.Listening) {
            scaleAnim.animateTo(
                targetValue = 1f + (volumeLevel * 0.35f),
                animationSpec = tween(durationMillis = 100)
            )
        } else {
            scaleAnim.animateTo(1f, tween(300))
        }
    }

    LaunchedEffect(Unit) {
        rotationAnim.animateTo(
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(8000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        // Outer Pulsing Glow Ring
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val radius = (this.size.minDimension / 2f - 12.dp.toPx()) * scaleAnim.value

            val strokeWidth = when (speechState) {
                is SpeechState.Listening -> 8.dp.toPx()
                is SpeechState.Processing -> 6.dp.toPx()
                is SpeechState.Speaking -> 10.dp.toPx()
                else -> 4.dp.toPx()
            }

            val ringColor = when (speechState) {
                is SpeechState.Listening -> NeonCyanPrimary
                is SpeechState.Processing -> VioletSecondary
                is SpeechState.Speaking -> Color(0xFFF59E0B)
                is SpeechState.Error -> Color(0xFFEF4444)
                else -> NeonCyanPrimary.copy(alpha = 0.5f)
            }

            drawCircle(
                color = ringColor.copy(alpha = 0.2f),
                radius = radius + 8.dp.toPx(),
                center = center
            )

            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(NeonCyanPrimary, VioletSecondary, Color(0xFF38BDF8), NeonCyanPrimary)
                ),
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth)
            )

            // Dynamic Voice Waveform Bars
            if (speechState is SpeechState.Listening || speechState is SpeechState.Speaking) {
                val barCount = 16
                val step = 360f / barCount
                for (i in 0 until barCount) {
                    val angleRad = Math.toRadians((i * step + rotationAnim.value).toDouble())
                    val barHeight = 10.dp.toPx() + (sin(i + rotationAnim.value / 10f) * 15.dp.toPx()) * (volumeLevel + 0.3f)

                    val startX = center.x + (radius - 10.dp.toPx()) * kotlin.math.cos(angleRad).toFloat()
                    val startY = center.y + (radius - 10.dp.toPx()) * sin(angleRad).toFloat()

                    val endX = center.x + (radius + barHeight) * kotlin.math.cos(angleRad).toFloat()
                    val endY = center.y + (radius + barHeight) * sin(angleRad).toFloat()

                    drawLine(
                        color = ringColor,
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }
        }
    }
}
