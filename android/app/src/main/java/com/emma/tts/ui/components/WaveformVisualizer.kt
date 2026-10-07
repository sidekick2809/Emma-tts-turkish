package com.emma.tts.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.emma.tts.ui.theme.EmmaAccent
import com.emma.tts.ui.theme.EmmaPrimary
import com.emma.tts.ui.theme.EmmaTeal

@Composable
fun WaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 18
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val gradient = Brush.verticalGradient(
            colors = listOf(EmmaAccent, EmmaPrimary, EmmaTeal)
        )

        for (i in 0 until barCount) {
            val minHeight = 6.dp
            val maxHeight = 42.dp

            val heightAnim by infiniteTransition.animateFloat(
                initialValue = if (isPlaying) (8 + (i * 7) % 24).toFloat() else 6f,
                targetValue = if (isPlaying) (12 + ((i + 5) * 11) % 36).toFloat() else 6f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 350 + (i * 45) % 300,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bar_$i"
            )

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(if (isPlaying) heightAnim.dp else minHeight)
                    .clip(RoundedCornerShape(2.dp))
                    .background(gradient)
            )
        }
    }
}
