package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * Animated Checkmark Icon drawn on Canvas with smooth stroke progression and spring pulse.
 */
@Composable
fun AnimatedSuccessCheckmark(
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
    circleColor: Color = Color(0xFF16A34A),
    checkColor: Color = Color.White
) {
    val scale = remember { Animatable(0.3f) }
    val checkProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Step 1: Scale & Spring pulse circle entrance
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        // Step 2: Draw checkmark path progressively
        checkProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .scale(scale.value)
            .clip(CircleShape)
            .background(circleColor)
    ) {
        Canvas(modifier = Modifier.size(size * 0.55f)) {
            val width = this.size.width
            val height = this.size.height

            val fullPath = Path().apply {
                moveTo(width * 0.15f, height * 0.5f)
                lineTo(width * 0.42f, height * 0.78f)
                lineTo(width * 0.88f, height * 0.22f)
            }

            val pathMeasure = PathMeasure().apply {
                setPath(fullPath, false)
            }

            val animatedPath = Path()
            pathMeasure.getSegment(
                startDistance = 0f,
                stopDistance = pathMeasure.length * checkProgress.value,
                destination = animatedPath,
                startWithMoveTo = true
            )

            drawPath(
                path = animatedPath,
                color = checkColor,
                style = Stroke(
                    width = width * 0.15f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )
            )
        }
    }
}

/**
 * Subtle Animated Overlay Banner / Modal indicating successful authentication or password reset.
 */
@Composable
fun SuccessAnimationOverlay(
    visible: Boolean,
    title: String = "Verification Successful!",
    subtitle: String = "Redirecting to portal...",
    onFinished: () -> Unit = {}
) {
    LaunchedEffect(visible) {
        if (visible) {
            delay(1200L)
            onFinished()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.8f),
        exit = fadeOut(animationSpec = tween(200)) + scaleOut(targetScale = 0.9f)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.45f))
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 8.dp,
                modifier = Modifier.padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    AnimatedSuccessCheckmark(size = 72.dp)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
