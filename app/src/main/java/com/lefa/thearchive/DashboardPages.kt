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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lefa.thearchive.model.Stats
import com.lefa.thearchive.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.absoluteValue

// --- Page 1: Overview ---
@Composable
fun PageOverview(stats: Stats) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("THE ARCHIVE", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Gold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Our Digital Footprint", color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        StatCard("Total Messages", "${stats.totalMessages}", DeepLove)
        StatCard("Total Words", "${stats.totalWords}", SoftAccent)
        StatCard("Total Media Shared", "${stats.mediaCount}", Gold)
        StatCard("Total Emojis", "${stats.totalEmojis}", Color.Cyan)

        Spacer(modifier = Modifier.height(30.dp))

        Text("Message Distribution", fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(10.dp))
        ComparisonBar(
            label1 = "Lefa", value1 = stats.lefaMsgs,
            label2 = "Owami", value2 = stats.owamiMsgs,
            color1 = DeepLove, color2 = Gold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text("Who Loves Harder? (I love you counts)", fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(10.dp))
        ComparisonBar(
            label1 = "Lefa", value1 = stats.lefaLove,
            label2 = "Owami", value2 = stats.owamiLove,
            color1 = DeepLove, color2 = Gold
        )
    }
}

// --- Page 2: First Signs ---
@Composable
fun PageFirstSigns(stats: Stats) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Text("THE BEGINNING", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = SoftAccent)
            Spacer(modifier = Modifier.height(8.dp))
            Text("When feelings started catching...", color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))
        }

        items(stats.firstSigns) { sign ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(if (sign.sender == "lefa") DeepLove else Gold, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${sign.sender.capitalize()} - ${sign.date}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\"${sign.message}\"",
                        color = Color.LightGray,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}

// --- Page 3: Routines ---
@Composable
fun PageRoutines(stats: Stats) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("DAILY LIFE", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Gold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("What we talk about...", color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        // Fixed: Explicit iteration over map entries
        for ((action, counts) in stats.actions) {
            RoutineRow(
                action = action.replace("_", " ").capitalize(),
                lefaCount = counts.first,
                owamiCount = counts.second
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun RoutineRow(action: String, lefaCount: Int, owamiCount: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(action, color = Color.White, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lefa Bar
            val total = (lefaCount + owamiCount).toFloat().coerceAtLeast(1f)
            val lefaFrac = lefaCount / total
            val owamiFrac = owamiCount / total

            Box(
                modifier = Modifier
                    .weight(if (lefaFrac < 0.1f) 0.1f else lefaFrac)
                    .height(20.dp)
                    .background(DeepLove, RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                if (lefaCount > 0) Text("$lefaCount", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(start = 4.dp))
            }

            Box(
                modifier = Modifier
                    .weight(if (owamiFrac < 0.1f) 0.1f else owamiFrac)
                    .height(20.dp)
                    .background(Gold, RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (owamiCount > 0) Text("$owamiCount", color = Color.Black, fontSize = 10.sp, modifier = Modifier.padding(end = 4.dp))
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Lefa", color = DeepLove, fontSize = 10.sp)
            Text("Owami", color = Gold, fontSize = 10.sp)
        }
    }
}

// --- Page 4: Comparisons (Naughty/Annoyed) ---
@Composable
fun PageComparisons(stats: Stats) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("THE TRUTH", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = DeepLove)
        Spacer(modifier = Modifier.height(30.dp))

        // Naughty
        Text("Who is Naughtier? \uD83D\uDE08", fontSize = 20.sp, color = Color.White)
        Text("(Counts of explicit words)", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(10.dp))
        ComparisonBar("Lefa", stats.lefaNaughty, "Owami", stats.owamiNaughty, DeepLove, Gold)

        Spacer(modifier = Modifier.height(40.dp))

        // Annoyed
        Text("Who gets Annoyed? \uD83D\uDE12", fontSize = 20.sp, color = Color.White)
        Text("(Counts of 'k', 'fine', 'whatever')", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(10.dp))
        ComparisonBar("Lefa", stats.lefaAnnoyed, "Owami", stats.owamiAnnoyed, DeepLove, Gold)

        Spacer(modifier = Modifier.height(40.dp))

        // Sorry
        Text("Who says Sorry? \uD83E\uDD7A", fontSize = 20.sp, color = Color.White)
        val sorryStats = stats.routine["sorry"] ?: (0 to 0)
        ComparisonBar("Lefa", sorryStats.first, "Owami", sorryStats.second, DeepLove, Gold)
    }
}


// --- Page 5: Interactive Animation ---
@Composable
fun PageInteractive(onComplete: () -> Unit) {
    var splashCount by remember { mutableStateOf(0) }
    var showCheekyEmoji by remember { mutableStateOf(false) }

    // Animation for movement
    val infiniteTransition = rememberInfiniteTransition()
    val offset by infiniteTransition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                splashCount++
                if (splashCount == 3) showCheekyEmoji = true
                if (splashCount >= 5) onComplete()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Touch us to make a splash...",
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(50.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Peach
                Text(
                    "🍑",
                    fontSize = 80.sp,
                    modifier = Modifier.graphicsLayer { translationX = offset }
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Eggplant
                Text(
                    "🍆",
                    fontSize = 80.sp,
                    modifier = Modifier.graphicsLayer { translationX = -offset }
                )
            }
        }

        // Splashes
        if (splashCount > 0) {
             Text(
                "💦",
                fontSize = 60.sp,
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = (-50).dp)
            )
        }

        if (showCheekyEmoji) {
             Text(
                "🤭",
                fontSize = 100.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp)
            )
        }
    }
}

// --- Page 6: Verdict ---
@Composable
fun PageVerdict(stats: Stats) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("THE VERDICT", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Gold)
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "After analyzing ${stats.totalMessages} messages...",
            color = Color.LightGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            "You are both obsessed with each other.",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DeepLove,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            "Lefa initiates the naughtiness.",
            color = Color.White
        )
        Text(
            "Owami initiates the sleep.",
            color = Color.White
        )

        Spacer(modifier = Modifier.height(30.dp))

        Text("FOREVER TO GO ❤️", fontSize = 20.sp, color = Gold)
    }
}

// --- Shared Components ---

@Composable
fun StatCard(title: String, value: String, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.DarkGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = Color.LightGray)
            Text(value, color = color, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        }
    }
}

@Composable
fun ComparisonBar(label1: String, value1: Int, label2: String, value2: Int, color1: Color, color2: Color) {
    val total = (value1 + value2).toFloat().coerceAtLeast(1f)
    val p1 = value1 / total
    val p2 = value2 / total

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().height(30.dp)) {
            Box(
                modifier = Modifier
                    .weight(if (p1 < 0.05f) 0.05f else p1)
                    .fillMaxHeight()
                    .background(color1, RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("$value1", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Box(
                modifier = Modifier
                    .weight(if (p2 < 0.05f) 0.05f else p2)
                    .fillMaxHeight()
                    .background(color2, RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("$value2", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label1, color = color1)
            Text(label2, color = color2)
        }
    }
}

// Extension to capitalize string
fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
