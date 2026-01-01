package com.lefa.thearchive

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
import com.lefa.thearchive.ui.theme.RoseBackground

@Composable
fun DashboardScreen(stats: Stats, onFinale: () -> Unit) {
    var currentPage by remember { mutableStateOf(0) }
    Box(Modifier.fillMaxSize().background(RoseBackground)) {
        when (currentPage) {
            0 -> PageOverview(stats)
            1 -> PageFirstSigns(stats)
            2 -> PageRoutines(stats)
            3 -> PageComparisons(stats)
            4 -> PageInteractive(onComplete = { currentPage++ })
            5 -> PageVerdict(stats, onFinale)
        }
        if (currentPage < 5 && currentPage != 4) {
            FloatingActionButton(onClick = { currentPage++ }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp), containerColor = Color.White) { Icon(Icons.Default.ArrowForward, contentDescription = "Next") }
        }
    }
}
