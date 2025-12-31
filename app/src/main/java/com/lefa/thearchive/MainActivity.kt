package com.lefa.thearchive

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.lefa.thearchive.ui.theme.*
import com.lefa.thearchive.utils.ChatParser
import com.lefa.thearchive.utils.GameEngine
import com.lefa.thearchive.utils.Question
import com.lefa.thearchive.utils.QuestionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Try to play music
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
    var stats by remember { mutableStateOf(Pair(0, "")) }
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
                if (parsed.isNotEmpty()) {
                    stats = Pair(parsed.size, parsed[0].fullDate)
                }
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

    val startGame = {
        keys = 0
        // Generate first question
        val r = Math.random()
        currentQuestion = if (r > 0.5) {
            GameEngine.generateWhoSaidIt(messages.toMutableList(), 1).firstOrNull()
        } else {
            GameEngine.generateWhenWasIt(messages.toMutableList(), 1).firstOrNull()
        }
        screen = "GAME"
    }

    val nextQuestion = {
        feedback = null
        val r = Math.random()
        currentQuestion = if (r > 0.5) {
            GameEngine.generateWhoSaidIt(messages.toMutableList(), 1).firstOrNull()
        } else {
            GameEngine.generateWhenWasIt(messages.toMutableList(), 1).firstOrNull()
        }
    }

    val handleAnswer = { answer: String ->
        currentQuestion?.let { q ->
            var isCorrect = false
            if (q.type == QuestionType.WHO_SAID_IT) {
                isCorrect = answer == q.correctAnswer
            } else {
                isCorrect = answer == q.correctAnswer
            }

            if (isCorrect) {
                feedback = "CORRECT"
                keys++
                if (keys >= KEYS_NEEDED) {
                     // Wait then go to Finale
                     // We can't delay directly in callback easily without coroutine scope,
                     // but we can use a LaunchedEffect triggered by feedback change or similar,
                     // or just a simple Handler/Thread sleep (not recommended)
                     // Let's use a side effect via state change handled in UI or simple breakdown
                }
            } else {
                feedback = "WRONG"
            }
        }
    }

    // Handling transitions after answer
    LaunchedEffect(feedback) {
        if (feedback != null) {
            delay(1500)
            if (feedback == "CORRECT" && keys >= KEYS_NEEDED) {
                screen = "FINALE"
                feedback = null
            } else {
                nextQuestion()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Bg, Bg2)))
    ) {
        when (screen) {
            "LOADING" -> LoadingScreen()
            "ERROR" -> ErrorScreen(feedback ?: "Unknown Error") { /* Retry logic difficult to reset compose state fully, maybe restart activity */ }
            "INTRO" -> IntroScreen(stats, startGame)
            "GAME" -> GameScreen(keys, KEYS_NEEDED, currentQuestion, feedback, handleAnswer)
            "FINALE" -> FinaleScreen(stats)
        }
    }
}

@Composable
fun LoadingScreen() {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Text("Restoring Archive...", color = TextWhite)
    }
}

@Composable
fun ErrorScreen(msg: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Error Loading Archive.", color = Error)
        Text(msg, color = TextWhite, modifier = Modifier.padding(20.dp))
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
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = Accent,
            modifier = Modifier.size(80.dp)
        )
        Text(
            "THE ARCHIVE",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = TextWhite,
            letterSpacing = 5.sp,
            modifier = Modifier.padding(top = 20.dp)
        )
        Text(
            "Corrupted Memory Detected.",
            fontSize = 18.sp,
            color = Accent,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(vertical = 10.dp)
        )
        Text(
            "${stats.first} fragments found starting from ${stats.second}.\nTo restore the file, you must prove your knowledge of the timeline.",
            color = SecondaryText,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 20.dp),
            lineHeight = 24.sp
        )

        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(containerColor = Accent),
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text("RESTORE DATA", color = Color.White, fontWeight = FontWeight.Bold)
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
        Text("KEYS: $keys / $maxKeys", color = Accent, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 10.dp))
        val progressValue = keys.toFloat() / maxKeys.toFloat()
        LinearProgressIndicator(
            progress = progressValue,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            color = Accent,
            trackColor = Color.White.copy(alpha = 0.2f)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Question
        AnimatedVisibility(
            visible = question != null,
            enter = fadeIn(animationSpec = tween(500)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            if (question != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        if (question.type == QuestionType.WHO_SAID_IT) "WHO SAID THIS?" else "WHEN WAS THIS SENT?",
                        color = SecondaryText,
                        letterSpacing = 2.sp,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    Surface(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 30.dp)
                    ) {
                        Text(
                            question.text,
                            color = TextWhite,
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(30.dp)
                        )
                    }

                    // Options
                    question.options.forEach { opt ->
                        val label = if (question.type == QuestionType.WHEN_WAS_IT) {
                             try {
                                 // opt is ISO string
                                 val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                                 isoFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
                                 val date = isoFormat.parse(opt)
                                 val displayFormat = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
                                 displayFormat.format(date!!)
                             } catch (e: Exception) { opt }
                        } else {
                            if (opt == "lefa") "Lefa" else "Owami"
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 15.dp)
                                .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(15.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(15.dp))
                                .clickable { onAnswer(opt) }
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, color = TextWhite, fontSize = 16.sp)
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
                    if (feedback == "CORRECT") Success.copy(alpha = 0.8f) else Error.copy(alpha = 0.8f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(feedback, fontSize = 40.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 3.sp)
        }
    }
}

@Composable
fun FinaleScreen(stats: Pair<Int, String>) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(Bg, Bg3)))
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
                tint = Color(0xFFE31B23),
                modifier = Modifier
                    .size(100.dp)
                    .padding(bottom = 20.dp)
            )
            Text("ACCESS GRANTED", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = TextWhite, letterSpacing = 2.sp)
            Text("Happy New Year, Owami", fontSize = 18.sp, color = Accent, modifier = Modifier.padding(top = 10.dp))

            Surface(
                color = Color.White.copy(alpha = 0.9f),
                shape = RoundedCornerShape(5.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 30.dp)
            ) {
                Text(
                    text = """
My Dearest Owami,

If you are reading this, you have successfully unlocked our archive.
These ${stats.first} messages are just a glimpse of the story we are writing together.

Every "Hi Stranger", every joke, every moment has led us here.
You are my favorite mystery, my best friend, and my love.

Here's to another year of us.
I love you.

- Lefa
                    """.trimIndent(),
                    color = Color.Black,
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
