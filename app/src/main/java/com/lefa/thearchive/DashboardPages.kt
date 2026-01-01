package com.lefa.thearchive

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lefa.thearchive.model.Stats
import com.lefa.thearchive.ui.theme.*

@Composable
fun PageOverview(stats: Stats) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("THE ARCHIVE", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Gold)
        Spacer(Modifier.height(8.dp)); Text("Our Digital Footprint", color = Color.Gray); Spacer(Modifier.height(24.dp))
        StatCard("Total Messages", "${stats.totalMessages}", DeepLove)
        StatCard("Total Words", "${stats.totalWords}", SoftAccent)
        StatCard("Total Media Shared", "${stats.mediaCount}", Gold)
        StatCard("Total Emojis", "${stats.totalEmojis}", Color.Cyan)
        Spacer(Modifier.height(30.dp)); Text("Message Distribution", fontWeight = FontWeight.Bold, color = Color.White)
        ComparisonBar("Lefa", stats.lefaMsgs, "Owami", stats.owamiMsgs, DeepLove, Gold)
        Spacer(Modifier.height(20.dp)); Text("Who Loves Harder?", fontWeight = FontWeight.Bold, color = Color.White)
        ComparisonBar("Lefa", stats.lefaLove, "Owami", stats.owamiLove, DeepLove, Gold)
    }
}

@Composable
fun PageFirstSigns(stats: Stats) {
    LazyColumn(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        item { Text("THE BEGINNING", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = SoftAccent); Spacer(Modifier.height(24.dp)) }
        items(stats.firstSigns) { sign ->
            Card(colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.3f)), modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(10.dp).background(if (sign.sender == "lefa") DeepLove else Gold, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text("${sign.sender.replaceFirstChar { it.uppercase() }} - ${sign.date}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp)); Text("\"${sign.message}\"", color = Color.LightGray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                }
            }
        }
    }
}

@Composable
fun PageRoutines(stats: Stats) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("DAILY LIFE", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Gold); Spacer(Modifier.height(24.dp))
        for ((action, counts) in stats.actions) { RoutineRow(action.replace("_", " ").uppercase(), counts.first, counts.second); Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
fun RoutineRow(action: String, lefaCount: Int, owamiCount: Int) {
    Column(Modifier.fillMaxWidth()) {
        Text(action, color = Color.White, fontWeight = FontWeight.Bold); Spacer(Modifier.height(4.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            val total = (lefaCount + owamiCount).toFloat().coerceAtLeast(1f)
            Box(Modifier.weight((lefaCount / total).coerceAtLeast(0.1f)).height(20.dp).background(DeepLove, RoundedCornerShape(4.dp, 0.dp, 0.dp, 4.dp))) { if (lefaCount > 0) Text("$lefaCount", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp)) }
            Box(Modifier.weight((owamiCount / total).coerceAtLeast(0.1f)).height(20.dp).background(Gold, RoundedCornerShape(0.dp, 4.dp, 4.dp, 0.dp))) { if (owamiCount > 0) Text("$owamiCount", color = Color.Black, fontSize = 10.sp, modifier = Modifier.padding(end = 4.dp).align(Alignment.CenterEnd)) }
        }
    }
}

@Composable
fun PageComparisons(stats: Stats) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("THE TRUTH", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DeepLove); Spacer(Modifier.height(30.dp))
        Text("Who is Naughtier? \uD83D\uDE08", fontSize = 20.sp, color = Color.White)
        ComparisonBar("Lefa", stats.lefaNaughty, "Owami", stats.owamiNaughty, DeepLove, Gold); Spacer(Modifier.height(40.dp))
        Text("Who gets Annoyed? \uD83D\uDE12", fontSize = 20.sp, color = Color.White)
        ComparisonBar("Lefa", stats.lefaAnnoyed, "Owami", stats.owamiAnnoyed, DeepLove, Gold); Spacer(Modifier.height(40.dp))
        Text("Who says Sorry? \uD83E\uDD7A", fontSize = 20.sp, color = Color.White)
        val sorry = stats.routine["sorry"] ?: (0 to 0)
        ComparisonBar("Lefa", sorry.first, "Owami", sorry.second, DeepLove, Gold)
    }
}

@Composable
fun PageInteractive(onComplete: () -> Unit) {
    var splashCount by remember { mutableStateOf(0) }; var showCheekyEmoji by remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(); val offset by infiniteTransition.animateFloat(-20f, 20f, infiniteRepeatable(tween(1000), RepeatMode.Reverse))
    Box(Modifier.fillMaxSize().clickable { splashCount++; if (splashCount == 3) showCheekyEmoji = true; if (splashCount >= 5) onComplete() }, contentAlignment = Alignment.Center) {
        Row { Text("🍑", fontSize = 80.sp, modifier = Modifier.graphicsLayer { translationX = offset }); Spacer(Modifier.width(10.dp)); Text("🍆", fontSize = 80.sp, modifier = Modifier.graphicsLayer { translationX = -offset }) }
        if (splashCount > 0) Text("💦", fontSize = 60.sp, modifier = Modifier.offset(y = (-50).dp))
        if (showCheekyEmoji) Text("🤭", fontSize = 100.sp, modifier = Modifier.align(Alignment.TopCenter).padding(top = 100.dp))
    }
}

@Composable
fun PageVerdict(stats: Stats, onNext: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("THE VERDICT", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Gold); Spacer(Modifier.height(20.dp))
        Text("You are both obsessed with each other.", fontSize = 24.sp, color = DeepLove, textAlign = TextAlign.Center); Spacer(Modifier.height(30.dp))
        Text("FOREVER TO GO ❤️", fontSize = 20.sp, color = Gold)
        Spacer(Modifier.height(30.dp))
        Button(onClick = onNext, colors = ButtonDefaults.buttonColors(containerColor = DeepLove)) {
            Text("See Finale", color = Color.White)
        }
    }
}

@Composable
fun StatCard(title: String, value: String, color: Color) {
    Card(Modifier.fillMaxWidth().padding(4.dp), colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.5f))) {
        Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(title, color = Color.LightGray); Text(value, color = color, fontWeight = FontWeight.Bold) }
    }
}

@Composable
fun ComparisonBar(label1: String, value1: Int, label2: String, value2: Int, color1: Color, color2: Color) {
    val total = (value1 + value2).toFloat().coerceAtLeast(1f)
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(30.dp)) {
            Box(Modifier.weight((value1/total).coerceAtLeast(0.05f)).fillMaxHeight().background(color1, RoundedCornerShape(8.dp, 0.dp, 0.dp, 8.dp)), contentAlignment = Alignment.Center) { Text("$value1", color = Color.White) }
            Box(Modifier.weight((value2/total).coerceAtLeast(0.05f)).fillMaxHeight().background(color2, RoundedCornerShape(0.dp, 8.dp, 8.dp, 0.dp)), contentAlignment = Alignment.Center) { Text("$value2", color = Color.Black) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(label1, color = color1); Text(label2, color = color2) }
    }
}
