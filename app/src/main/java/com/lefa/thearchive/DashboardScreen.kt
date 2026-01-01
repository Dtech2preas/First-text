package com.lefa.thearchive

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.lefa.thearchive.model.Stats
import com.lefa.thearchive.ui.theme.DeepLove

@Composable
fun DashboardScreen(stats: Stats) {
    var currentPage by remember { mutableStateOf(0) }
    val totalPages = 6

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // --- CONTENT ---
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                        slideOutHorizontally { width -> -width } + fadeOut()
            }, label = "PageTransition"
        ) { page ->
            when (page) {
                0 -> PageOverview(stats)
                1 -> PageFirstSigns(stats)
                2 -> PageRoutines(stats)
                3 -> PageComparisons(stats)
                4 -> PageInteractive(onComplete = { currentPage = 5 })
                5 -> PageVerdict(stats)
            }
        }

        // --- NAVIGATION ---
        // Only show Next button if it's not the interactive page (which has its own logic)
        // and not the last page
        if (currentPage != 4 && currentPage < totalPages - 1) {
            FloatingActionButton(
                onClick = { currentPage++ },
                containerColor = DeepLove,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
            ) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next")
            }
        }
    }
}
