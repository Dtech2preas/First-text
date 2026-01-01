package com.lefa.thearchive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lefa.thearchive.model.StaticStats
import com.lefa.thearchive.model.Stats
import com.lefa.thearchive.ui.theme.DeepLove
import com.lefa.thearchive.ui.theme.RoseBackground
import kotlinx.coroutines.delay

@Composable
fun LoadingScreen(onParsingComplete: (Stats) -> Unit, onStartMeantimeQuiz: () -> Unit) {
    var showEnterButton by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(3000); showEnterButton = true }
    Box(Modifier.fillMaxSize().background(RoseBackground), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Happy New Year 2026", fontSize = 32.sp, color = DeepLove)
            Spacer(Modifier.height(20.dp))
            if (!showEnterButton) { CircularProgressIndicator(color = DeepLove); Spacer(Modifier.height(20.dp)); Text("Analyzing 3 years of love...", color = Color.Gray) }
            else { Button(onClick = { onParsingComplete(StaticStats.data) }, colors = ButtonDefaults.buttonColors(containerColor = DeepLove)) { Text("ENTER ARCHIVE", fontSize = 18.sp) } }
            Spacer(Modifier.height(40.dp))
            Button(onClick = onStartMeantimeQuiz, colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) { Text("IN THE MEANTIME (Quiz)") }
        }
    }
}
