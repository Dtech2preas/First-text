package com.lefa.thearchive

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.lefa.thearchive.model.Stats
import com.lefa.thearchive.ui.theme.TheArchiveTheme
import com.lefa.thearchive.utils.CacheManager
import com.lefa.thearchive.utils.GameEngine

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TheArchiveTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    var currentScreen by remember { mutableStateOf(Screen.LOADING) }
    var loadedStats by remember { mutableStateOf<Stats?>(null) }

    // Quiz State
    var quizQuestions by remember { mutableStateOf<List<com.lefa.thearchive.utils.Question>>(emptyList()) }
    var currentQuestionIndex by remember { mutableStateOf(0) }
    var quizScore by remember { mutableStateOf(0) }
    var gameWon by remember { mutableStateOf(false) }

    when (currentScreen) {
        Screen.LOADING -> {
            LoadingScreen(
                onParsingComplete = { stats ->
                    loadedStats = stats
                    currentScreen = Screen.DASHBOARD
                },
                onStartMeantimeQuiz = {
                    currentScreen = Screen.MEANTIME_QUIZ
                }
            )
        }
        Screen.DASHBOARD -> {
            if (loadedStats != null) {
                DashboardScreen(stats = loadedStats!!)
            } else {
                // Fallback (shouldn't happen with new flow)
                currentScreen = Screen.LOADING
            }
        }
        Screen.MEANTIME_QUIZ -> {
            MeantimeQuizScreen(
                onFinish = {
                    currentScreen = Screen.LOADING // Return to loading to "finish" parsing (or fake wait)
                }
            )
        }
        Screen.GAME -> {
            // Placeholder for main game logic if accessed from Dashboard later
            // For now, focus is on the requested flow
        }
    }
}

enum class Screen {
    LOADING,
    DASHBOARD,
    MEANTIME_QUIZ,
    GAME
}
