package com.lefa.thearchive

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.lefa.thearchive.utils.ChatParser
import com.lefa.thearchive.utils.GameEngine
import com.lefa.thearchive.utils.Question
import com.lefa.thearchive.utils.QuestionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Locale

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
    val KEYS_NEEDED = 5
    var currentQuestion by remember { mutableStateOf<Question?>(null) }

    // Load Resources
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            try {
                val inputStream = context.resources.openRawResource(
                    context.resources.getIdentifier("chat", "raw", context.packageName)
                )
                val reader = BufferedReader(InputStreamReader(inputStream))
                val content = reader.readText()
                reader.close()

                val parsed = ChatParser.parseChat(content)
                messages = parsed
                val s = ChatParser.generateStats(parsed)
                statsData = s

                delay(1000)
                withContext(Dispatchers.Main) {
                    screen = "INTRO"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    feedback = e.localizedMessage
                    screen = "ERROR"
                }
            }
        }
    }

    fun pickNextQuestion() {
        feedback = null
        val r = Math.random()
        // Mix of question types
        currentQuestion = when {
            r < 0.25 -> GameEngine.generateWhoSaidIt(messages, 1).firstOrNull()
            r < 0.50 && statsData != null -> GameEngine.generateGuessTheReply(statsData!!.replyPairs, messages, 1).firstOrNull()
            r < 0.75 -> GameEngine.generateCompleteThePhrase(messages, 1).firstOrNull()
            else -> GameEngine.generateChronologicalOrder(messages, 1).firstOrNull()
        }

        // Fallback if null (e.g. not enough reply pairs)
        if (currentQuestion == null) {
            currentQuestion = GameEngine.generateWhoSaidIt(messages, 1).firstOrNull()
        }
    }

    val startGame = {
        keys = 0
        pickNextQuestion()
        screen = "GAME"
    }

    val handleAnswer = { answer: String ->
        currentQuestion?.let { q ->
            val isCorrect = answer == q.correctAnswer
            if (isCorrect) {
                feedback = "CORRECT"
                keys++
            } else {
                feedback = "WRONG"
            }
        }
        Unit
    }

    LaunchedEffect(feedback) {
        if (feedback != null) {
            delay(1500)
            if (feedback == "CORRECT" && keys >= KEYS_NEEDED) {
                screen = "DASHBOARD"
                feedback = null
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
            "LOADING" -> LoadingScreen()
            "ERROR" -> ErrorScreen(feedback ?: "Unknown Error") { }
            "INTRO" -> statsData?.let { IntroScreen(Pair(it.totalMessages, it.dateRange), startGame) }
            "GAME" -> GameScreen(keys, KEYS_NEEDED, currentQuestion, feedback, handleAnswer)
            "DASHBOARD" -> statsData?.let { DashboardScreen(it) { screen = "FINALE" } }
            "FINALE" -> statsData?.let { FinaleScreen(Pair(it.totalMessages, it.dateRange)) }
        }
    }
}

// ... existing LoadingScreen, ErrorScreen, IntroScreen ...

@Composable
fun LoadingScreen() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Text("Opening our memories...", color = DeepLove, fontSize = 18.sp, fontStyle = FontStyle.Italic)
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
fun IntroScreen(stats: Pair<Int, String>, onStart: () -> Unit) {
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
                    "Since ${stats.second.substringBefore(",")}", // Simple trim
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

                    val title = when(question.type) {
                        QuestionType.WHO_SAID_IT -> "Who said this?"
                        QuestionType.GUESS_THE_REPLY -> "What was the reply?"
                        QuestionType.COMPLETE_THE_PHRASE -> "Complete the text"
                        QuestionType.CHRONOLOGICAL_ORDER -> "Which came first?"
                        else -> "Question"
                    }

                    Text(
                        title,
                        color = TextSecondary,
                        letterSpacing = 1.sp,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    // Context (Previous message for Reply Game)
                    if (question.context != null && question.type == QuestionType.GUESS_THE_REPLY) {
                        Surface(
                            color = RoseSurface.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(15.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Column(modifier = Modifier.padding(15.dp)) {
                                Text("Context:", fontSize = 12.sp, color = TextSecondary)
                                Text(question.text, fontStyle = FontStyle.Italic, color = TextPrimary)
                            }
                        }
                    }

                    Surface(
                        color = PureWhite,
                        shadowElevation = 4.dp,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 30.dp)
                    ) {
                        Text(
                            // For Guess Reply, main text is context, we want to hide that logic in specific UI or just show question text
                            // In GameEngine, I set text = msg.content.
                            // For Reply, text is original msg.
                            // For CompletePhrase, text is "I love ____".
                            // For Chronological, text is "A: ... B: ..."
                            if (question.type == QuestionType.GUESS_THE_REPLY) "???" else question.text,
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
                                .height(IntrinsicSize.Min) // Dynamic height for long text
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
fun FinaleScreen(stats: Pair<Int, String>) {
    val scrollState = rememberScrollState()

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
