package com.lefa.thearchive

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lefa.thearchive.model.Stats
import com.lefa.thearchive.model.StaticStats
import com.lefa.thearchive.ui.theme.*
import com.lefa.thearchive.utils.CacheManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun LoadingScreen(
    onParsingComplete: (Stats) -> Unit,
    onStartMeantimeQuiz: () -> Unit
) {
    var progress by remember { mutableStateOf(0f) }
    var isReady by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Fake Loading Logic - 100 Seconds
    LaunchedEffect(Unit) {
        val totalTime = 100_000L // 100 seconds
        val interval = 100L
        val steps = totalTime / interval

        for (i in 1..steps) {
            delay(interval)
            progress = i.toFloat() / steps
        }
        isReady = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Night sky for fireworks
    ) {
        // --- ANIMATIONS ---
        FireworkDisplay()
        FloatingHearts()
        BirdsAnimation()

        // --- CONTENT ---
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "HAPPY NEW YEAR",
                color = Gold,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "2026",
                color = Color.White,
                fontSize = 60.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(50.dp))

            if (!isReady) {
                CircularProgressIndicator(
                    progress = progress,
                    color = DeepLove,
                    modifier = Modifier.size(60.dp),
                    strokeWidth = 6.dp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Analyzing ${StaticStats.data.totalMessages} memories... ${(progress * 100).toInt()}%",
                    color = Color.Gray
                )
            } else {
                 // --- BUTTONS ---
                 Button(
                    onClick = { onParsingComplete(StaticStats.data) },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepLove),
                    shape = RoundedCornerShape(25.dp),
                    modifier = Modifier
                        .height(50.dp)
                        .width(200.dp)
                ) {
                    Text("ENTER ARCHIVE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))

            // "Meantime" Button - Always visible to keep her busy
            OutlinedButton(
                onClick = onStartMeantimeQuiz,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Gold),
                border = androidx.compose.foundation.BorderStroke(1.dp, Gold),
                shape = RoundedCornerShape(25.dp),
                modifier = Modifier
                    .height(50.dp)
                    .width(240.dp)
            ) {
                Text("IN THE MEANTIME... (QUIZ)", fontWeight = FontWeight.Bold)
            }
        }

        // --- CREDITS ---
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Made by D-TECH", color = Color.LightGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("In association with Preasx24", color = Color.Gray, fontSize = 10.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text("For Owami Mcube", color = DeepLove, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// --- ANIMATION COMPONENTS ---

@Composable
fun FireworkDisplay() {
    val infiniteTransition = rememberInfiniteTransition()
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // Generate random fireworks
    val fireworks = remember { List(5) { RandomFirework() } }

    Canvas(modifier = Modifier.fillMaxSize()) {
        fireworks.forEach { fw ->
            val progress = (time + fw.offset) % 1f
            if (progress < 0.8f) { // Only show for 80% of cycle
                val explosionProgress = if (progress < 0.2f) progress * 5 else (progress - 0.2f) * 1.25f
                val alpha = (1f - progress).coerceIn(0f, 1f)

                // Draw particles
                for (i in 0 until 20) {
                    val angle = (Math.PI * 2 * i) / 20
                    val radius = explosionProgress * fw.size * 200 // Max radius
                    val x = fw.x * size.width + cos(angle).toFloat() * radius
                    val y = fw.y * size.height + sin(angle).toFloat() * radius

                    drawCircle(
                        color = fw.color.copy(alpha = alpha),
                        radius = 3f,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

data class RandomFirework(
    val x: Float = Random.nextFloat(),
    val y: Float = Random.nextFloat() * 0.5f, // Top half
    val size: Float = Random.nextFloat() + 0.5f,
    val color: Color = listOf(DeepLove, Gold, SoftAccent, Color.Cyan, Color.Magenta).random(),
    val offset: Float = Random.nextFloat()
)

@Composable
fun FloatingHearts() {
    val infiniteTransition = rememberInfiniteTransition()
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val hearts = remember { List(10) { RandomHeart() } }

    Canvas(modifier = Modifier.fillMaxSize()) {
        hearts.forEach { heart ->
            val progress = (time + heart.offset) % 1f
            val y = size.height - (progress * size.height) // Move up
            val x = (heart.x * size.width) + (sin(progress * 10) * 50) // Wiggle
            val alpha = (1f - progress).coerceIn(0f, 1f)

            drawPath(
                path = createHeartPath(x, y, heart.size * 30),
                color = DeepLove.copy(alpha = alpha * 0.5f)
            )
        }
    }
}

data class RandomHeart(
    val x: Float = Random.nextFloat(),
    val size: Float = Random.nextFloat() + 0.5f,
    val offset: Float = Random.nextFloat()
)

fun createHeartPath(x: Float, y: Float, size: Float): Path {
    return Path().apply {
        moveTo(x, y + size / 4)
        cubicTo(x, y, x - size / 2, y, x - size / 2, y + size / 4)
        cubicTo(x - size / 2, y + size / 2, x, y + size * 0.8f, x, y + size)
        cubicTo(x, y + size * 0.8f, x + size / 2, y + size / 2, x + size / 2, y + size / 4)
        cubicTo(x + size / 2, y, x, y, x, y + size / 4)
        close()
    }
}

@Composable
fun BirdsAnimation() {
     val infiniteTransition = rememberInfiniteTransition()
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val birdX = (time * (size.width + 200)) - 100 // Move across screen
        val birdY = size.height * 0.2f + (sin(time * 20) * 50)

        // Draw simple V shape bird
        val wingY = birdY + (sin(time * 50) * 10)

        val path = Path().apply {
            moveTo(birdX, birdY)
            lineTo(birdX - 20, wingY) // Left wing
            moveTo(birdX, birdY)
            lineTo(birdX + 20, wingY) // Right wing
        }

        drawPath(
            path = path,
            color = Color.White.copy(alpha = 0.8f),
            style = Stroke(width = 3f)
        )

        // Second bird slightly behind
        val bird2X = birdX - 50
        val bird2Y = birdY + 30
        val wing2Y = bird2Y + (sin(time * 50 + 1) * 10)

        val path2 = Path().apply {
            moveTo(bird2X, bird2Y)
            lineTo(bird2X - 15, wing2Y)
            moveTo(bird2X, bird2Y)
            lineTo(bird2X + 15, wing2Y)
        }

        drawPath(
            path = path2,
            color = Color.White.copy(alpha = 0.8f),
            style = Stroke(width = 3f)
        )
    }
}
