package com.zervian.booster.ui.screens.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zervian.booster.ui.theme.ZervianAccentBlue
import com.zervian.booster.ui.theme.ZervianAccentCyan
import com.zervian.booster.ui.theme.ZervianBackground
import com.zervian.booster.ui.theme.ZervianTextSecondary
import kotlinx.coroutines.delay

private const val SPLASH_DURATION_MS = 2400

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val infinite = rememberInfiniteTransition(label = "splash_infinite")

    // Cincin logo berputar terus selama splash tampil - animasi nyata, bukan gambar diam.
    val ringRotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "ring_rotation"
    )

    // Glow di belakang logo "bernapas" (pulsing), bukan statis.
    val glowScale by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow_scale"
    )
    val glowAlpha by infinite.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.55f,
        animationSpec = infiniteRepeatable(tween(1100, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow_alpha"
    )

    val entrance = remember { Animatable(0f) }
    var showSubtitle by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        entrance.animateTo(1f, animationSpec = tween(550, easing = FastOutSlowInEasing))
        showSubtitle = true

        // Progress bar animasi sinkron ke durasi splash - bukan spinner generik diam.
        val progressAnim = Animatable(0f)
        progressAnim.animateTo(
            1f,
            animationSpec = tween(SPLASH_DURATION_MS - 550, easing = LinearOutSlowInEasing)
        ) {
            progress = value
        }

        onFinished()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(ZervianBackground),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                // Glow radial berdenyut di belakang logo.
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .graphicsLayer(scaleX = glowScale, scaleY = glowScale)
                        .background(
                            Brush.radialGradient(
                                listOf(ZervianAccentCyan.copy(alpha = glowAlpha), Color.Transparent)
                            )
                        )
                )

                // Cincin berputar digambar manual lewat Canvas - benar-benar animasi, bukan GIF/asset statis.
                Canvas(modifier = Modifier.size(110.dp)) {
                    rotate(ringRotation) {
                        drawArc(
                            brush = Brush.sweepGradient(
                                listOf(ZervianAccentCyan, ZervianAccentBlue, Color.Transparent, ZervianAccentCyan)
                            ),
                            startAngle = 0f,
                            sweepAngle = 300f,
                            useCenter = false,
                            style = Stroke(width = 6f, cap = StrokeCap.Round)
                        )
                    }
                }

                Text(
                    "Z",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = ZervianAccentCyan,
                    modifier = Modifier.graphicsLayer(scaleX = entrance.value, scaleY = entrance.value)
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "ZERVIAN",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = ZervianAccentCyan,
                modifier = Modifier.graphicsLayer(scaleX = entrance.value, scaleY = entrance.value)
            )

            AnimatedVisibility(
                visible = showSubtitle,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 2 }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(Modifier.height(6.dp))
                    Text("Game Performance Suite", color = ZervianTextSecondary, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(28.dp))
                    AnimatedProgressBar(progress = progress)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${(progress * 100).toInt()}%",
                        color = ZervianTextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedProgressBar(progress: Float) {
    Box(
        modifier = Modifier
            .width(160.dp)
            .height(4.dp)
            .background(ZervianTextSecondary.copy(alpha = 0.2f), shape = RoundedCornerShape(2.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(
                    Brush.horizontalGradient(listOf(ZervianAccentBlue, ZervianAccentCyan)),
                    shape = RoundedCornerShape(2.dp)
                )
        )
    }
}
