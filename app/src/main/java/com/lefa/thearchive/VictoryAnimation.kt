package com.lefa.thearchive

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun VictoryAnimation(onAnimationEnd: () -> Unit) {
    var animationState by remember { mutableStateOf(0) } // 0: Init, 1: Move In, 2: Splash, 3: End

    // Animation 1: Move Together
    val moveTransition = updateTransition(targetState = animationState, label = "Move")

    val leftOffset by moveTransition.animateDp(label = "Left", transitionSpec = { tween(1000) }) { state ->
        if (state >= 1) 0.dp else (-100).dp
    }

    val rightOffset by moveTransition.animateDp(label = "Right", transitionSpec = { tween(1000) }) { state ->
        if (state >= 1) 0.dp else 100.dp
    }

    // Animation 2: Splash Scale & Alpha
    val splashScale by animateFloatAsState(
        targetValue = if (animationState >= 2) 1.5f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val splashAlpha by animateFloatAsState(
        targetValue = if (animationState >= 2) 1f else 0f,
        animationSpec = tween(500)
    )

    LaunchedEffect(Unit) {
        delay(500)
        animationState = 1 // Start moving
        delay(1000)
        animationState = 2 // Splash!
        delay(2000)
        onAnimationEnd()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Splash Water
        Text(
            "💦",
            fontSize = 100.sp,
            modifier = Modifier
                .scale(splashScale)
                .alpha(splashAlpha)
                .offset(y = (-50).dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Peach
            Text(
                "🍑",
                fontSize = 80.sp,
                modifier = Modifier.offset(x = leftOffset)
            )

            Spacer(modifier = Modifier.width(20.dp))

            // Eggplant
            Text(
                "🍆",
                fontSize = 80.sp,
                modifier = Modifier.offset(x = rightOffset)
            )
        }
    }
}
