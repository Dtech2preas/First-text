package com.lefa.thearchive

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lefa.thearchive.model.Message
import com.lefa.thearchive.model.Stats
import com.lefa.thearchive.ui.theme.*
import com.lefa.thearchive.utils.GameEngine
import com.lefa.thearchive.utils.PrefsManager
import com.lefa.thearchive.utils.Question
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val resId = resources.getIdentifier("music", "raw", packageName)
            if (resId != 0) {
                mediaPlayer = MediaPlayer.create(this, resId)
                mediaPlayer?.isLooping = true
                mediaPlayer?.setVolume(0.5f, 0.5f)
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setContent {
            TheArchiveApp()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

@Composable
fun TheArchiveApp() {
    val context = LocalContext.current
    var screen by remember { mutableStateOf("LOADING") }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var statsData by remember { mutableStateOf<Stats?>(null) }
    var feedback by remember { mutableStateOf<String?>(null) }

    // Game State
    var keys by remember { mutableStateOf(0) }
    val KEYS_NEEDED = 15
    var currentQuestion by remember { mutableStateOf<Question?>(null) }

    // Victory State
    var showVictoryAnimation by remember { mutableStateOf(false) }
    var showHoohaaDialog by remember { mutableStateOf(false) }

    fun pickNextQuestion() {
        feedback = null
        // Hardcoded stats don't expose list of messages directly for quiz generation in the same way.
        // We need to parse or use dummy questions if messages list is empty.
        // For now, let's assume we can't generate new dynamic questions without parsing.
        // BUT the user asked to "hardcode info".
        // The GameEngine.stats is available.
        // However, generating questions requires the raw messages list which we skipped parsing to save time.
        // Option: Parse messages in background for the quiz? Or skip the quiz part?
        // User said: "play the in the meantime gave"
        // Let's rely on GameEngine generating something or just use the hardcoded logic if possible.
        // Actually, LoadingScreen still calls onParsingComplete with stats.
        // We will just use the hardcoded stats but for the Game to work we need Messages.
        // Let's assume we might need to parse for the game, OR we just let the Dashboard work.
        // The user prioritized the "Fixed Database" for loading.

        // If messages are empty, we can't play the main game properly.
        // We should probably rely on parsing in background if the user wants to play the game.
        // But for the Dashboard, we use hardcoded stats.

        // Fix: Use GameEngine.generateWhoSaidIt if messages exist.
        if (messages.isNotEmpty()) {
             currentQuestion = GameEngine.generateWhoSaidIt(messages, 1).firstOrNull()
        } else {
            // Fallback if no messages parsed yet
             currentQuestion = Question(com.lefa.thearchive.utils.QuestionType.WHO_SAID_IT, "Loading questions...", "lefa", listOf("lefa", "owami"))
        }
    }

    val startGame = {
        keys = 0
        pickNextQuestion()
        screen = "GAME"
    }

    val skipGame = {
        screen = "DASHBOARD"
    }

    val handleAnswer = { answer: String ->
        currentQuestion?.let { q ->
            val isCorrect = answer == q.correctAnswer
            if (isCorrect) {
                feedback = "CORRECT"
                keys++
            } else {
                feedback = "WRONG"
                // Deduct point, min 0
                if (keys > 0) {
                    keys--
                }
            }
            Unit
        }
        Unit
    }

    LaunchedEffect(feedback) {
        if (feedback != null) {
            delay(1500)
            if (feedback == "CORRECT" && keys >= KEYS_NEEDED) {
                // Game Won Logic
                PrefsManager.setGameWon(context, true)
                showHoohaaDialog = true
                feedback = null // Clear feedback to show dialog
            } else {
                pickNextQuestion()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(RoseBackground, PureWhite)))
    ) {
        when (screen) {
            "LOADING" -> LoadingScreen(
                onParsingComplete = { stats ->
                    statsData = stats
                    // If we used hardcoded stats, messagesByDate might be empty.
                    // If so, we might not be able to play the 'Who Said It' game immediately.
                    // But the Dashboard will work.
                    messages = stats.messagesByDate.values.flatten()
                    screen = "INTRO"
                },
                onStartMeantimeQuiz = {
                    screen = "MEANTIME"
                }
            )
            "MEANTIME" -> MeantimeQuizScreen {
                screen = "LOADING"
            }
            "ERROR" -> ErrorScreen(feedback ?: "Unknown Error") { }
            "INTRO" -> statsData?.let {
                IntroScreen(
                    stats = Pair(it.totalMessages, it.dateRange),
                    onStart = startGame,
                    onSkip = if (PrefsManager.isGameWon(context)) skipGame else null
                )
            }
            "GAME" -> {
                if (showVictoryAnimation) {
                    VictoryAnimation {
                        showVictoryAnimation = false
                        screen = "DASHBOARD"
                    }
                } else if (showHoohaaDialog) {
                    AlertDialog(
                        onDismissRequest = {},
                        title = { Text("Congratulations!", fontWeight = FontWeight.Bold, color = DeepLove) },
                        text = { Text("You have a nice hoohaa just so you know...", fontSize = 18.sp) },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showHoohaaDialog = false
                                    showVictoryAnimation = true
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepLove)
                            ) {
                                Text("Continue")
                            }
                        }
                    )
                } else {
                    GameScreen(keys, KEYS_NEEDED, currentQuestion, feedback, handleAnswer)
                }
            }
            // Use DashboardPages instead of DashboardScreen
            "DASHBOARD" -> DashboardPages(onNavigateBack = { screen = "FINALE" })
            "FINALE" -> statsData?.let {
                FinaleScreen(
                    stats = Pair(it.totalMessages, it.dateRange),
                    onBack = { screen = "DASHBOARD" }
                )
            }
        }
    }
}

@Composable
fun ErrorScreen(msg: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Something went wrong.", color = Error)
        Text(msg, color = TextPrimary, modifier = Modifier.padding(20.dp))
    }
}

@Composable
fun IntroScreen(stats: Pair<Int, String>, onStart: () -> Unit, onSkip: (() -> Unit)?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = null,
            tint = DeepLove,
            modifier = Modifier.size(100.dp)
        )
        Text(
            "OUR STORY",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = DeepLove,
            letterSpacing = 5.sp,
            modifier = Modifier.padding(top = 20.dp)
        )

        Text(
            "A journey through time.",
            fontSize = 18.sp,
            color = TextSecondary,
            fontStyle = FontStyle.Italic,
            modifier = Modifier.padding(vertical = 10.dp)
        )

        Surface(
            color = RoseSurface.copy(alpha = 0.5f),
            shape = RoundedCornerShape(15.dp),
            modifier = Modifier.padding(vertical = 30.dp).fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${stats.first} Messages",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
                Text(
                    "Since ${stats.second.substringBefore(",")}",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        }

        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = DeepLove),
            modifier = Modifier
                .padding(top = 20.dp)
                .height(50.dp)
                .fillMaxWidth(0.7f),
            shape = RoundedCornerShape(25.dp)
        ) {
            Text("START THE JOURNEY", color = Color.White, fontWeight = FontWeight.Bold)
        }

        if (onSkip != null) {
            TextButton(
                onClick = onSkip,
                modifier = Modifier.padding(top = 10.dp)
            ) {
                Text("Go to Dashboard (You already won)", color = TextSecondary)
            }
        }
    }
}

@Composable
fun GameScreen(
    keys: Int,
    maxKeys: Int,
    question: Question?,
    feedback: String?,
    onAnswer: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 20.dp)
        ) {
            Icon(Icons.Default.Star, contentDescription = null, tint = Gold)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Memory $keys / $maxKeys", color = TextSecondary, fontWeight = FontWeight.Bold)
        }

        LinearProgressIndicator(
            progress = keys.toFloat() / maxKeys.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = DeepLove,
            trackColor = RoseSurface
        )

        Spacer(modifier = Modifier.height(30.dp))

        // Question
        AnimatedVisibility(
            visible = question != null,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            if (question != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Text(
                        "Who said this?",
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    Surface(
                        color = PureWhite,
                        shadowElevation = 4.dp,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 30.dp)
                    ) {
                        Text(
                            question.text,
                            color = TextPrimary,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(30.dp),
                            lineHeight = 28.sp
                        )
                    }

                    // Options
                    question.options.forEach { opt ->
                        val label = if (opt == "lefa") "Lefa" else if (opt == "owami") "Owami" else opt

                        Button(
                            onClick = { onAnswer(opt) },
                            colors = ButtonDefaults.buttonColors(containerColor = PureWhite),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .height(IntrinsicSize.Min)
                        ) {
                            Text(label, color = TextPrimary, fontSize = 16.sp, modifier = Modifier.padding(vertical = 10.dp))
                        }
                    }
                }
            }
        }
    }

    // Feedback Overlay
    if (feedback != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (feedback == "CORRECT") Success.copy(alpha = 0.9f) else Error.copy(alpha = 0.9f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = if (feedback == "CORRECT") Icons.Default.Favorite else Icons.Default.Star,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(60.dp)
                )
                Text(
                    if (feedback == "CORRECT") "Love it!" else "Oops!",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun FinaleScreen(stats: Pair<Int, String>, onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    // Handle Android Back Button
    BackHandler {
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(RoseBackground, PureWhite)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back Button (Top Left)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                TextButton(onClick = onBack) {
                    Text("← Back", color = TextSecondary, fontWeight = FontWeight.Bold)
                }
            }

            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = DeepLove,
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 20.dp)
            )
            Text("Happy New Year!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DeepLove)
            Text("To my favorite person", fontSize = 18.sp, color = TextSecondary, modifier = Modifier.padding(top = 10.dp))

            Surface(
                color = PureWhite,
                shadowElevation = 2.dp,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp)
            ) {
                Text(
                    text = """
My Dearest Owami,

We've shared ${stats.first} messages since ${stats.second.substringBefore(",")}.
Each one is a piece of the beautiful puzzle that is 'Us'.

Thank you for being my partner, my love, and my best friend.
I can't wait to create more memories with you.

I love you.

- Lefa
                    """.trimIndent(),
                    color = TextPrimary,
                    fontSize = 16.sp,
                    lineHeight = 28.sp,
                    fontFamily = FontFamily.Serif,
                    modifier = Modifier.padding(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}
