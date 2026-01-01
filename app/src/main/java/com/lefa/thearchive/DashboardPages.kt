package com.lefa.thearchive

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lefa.thearchive.model.Stats
import com.lefa.thearchive.ui.theme.DeepLove
import com.lefa.thearchive.ui.theme.RoseBackground
import com.lefa.thearchive.ui.theme.SoftAccent
import com.lefa.thearchive.ui.theme.Gold
import com.lefa.thearchive.utils.GameEngine
import kotlinx.coroutines.delay

// Colors (Hardcoded if Theme not available)
val RoseBg = Color(0xFFFFF0F5)
val LoveRed = Color(0xFFE91E63)
val AccentPink = Color(0xFFFF80AB)
val GoldColor = Color(0xFFFFD700)

@Composable
fun DashboardPages(
    onNavigateBack: () -> Unit
) {
    val stats = GameEngine.stats
    var currentPage by remember { mutableStateOf(0) }
    val totalPages = 5

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(RoseBg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Our Story ❤️",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = LoveRed,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            when (currentPage) {
                0 -> PageOne(stats)
                1 -> PageTwo(stats)
                2 -> PageThree(stats)
                3 -> PageFour(stats)
                4 -> PageFive(stats)
            }

            Spacer(modifier = Modifier.height(80.dp)) // Space for FAB
        }

        // Next Button
        if (currentPage < totalPages - 1) {
            FloatingActionButton(
                onClick = { currentPage++ },
                containerColor = LoveRed,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Text("Next ➡️", modifier = Modifier.padding(horizontal = 16.dp))
            }
        } else {
            // Finish Button on Last Page
            FloatingActionButton(
                onClick = onNavigateBack,
                containerColor = GoldColor,
                contentColor = Color.Black,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Text("Finale ✨", modifier = Modifier.padding(horizontal = 16.dp))
            }
        }

        // Back Button (if not on first page)
        if (currentPage > 0) {
             FloatingActionButton(
                onClick = { currentPage-- },
                containerColor = SoftAccent, // Use distinct color
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text("⬅️ Back", modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
fun StatCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = LoveRed)
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
fun PageOne(stats: Stats) {
    StatCard("📊 The Totals") {
        Text("Total Messages: ${stats.totalMessages}")
        Text("Lefa: ${stats.lefaMsgs} | Owami: ${stats.owamiMsgs}")
        Spacer(modifier = Modifier.height(4.dp))
        Text("Total Characters Sent:")
        Text("Lefa: ${stats.lefaChars}")
        Text("Owami: ${stats.owamiChars}")
        Spacer(modifier = Modifier.height(4.dp))
        Text("Total Words:")
        Text("Lefa: ${stats.lefaWords}")
        Text("Owami: ${stats.owamiWords}")
    }

    StatCard("📸 Media & Emojis") {
        Text("Pics/Videos Sent:")
        Text("Lefa: ${stats.lefaMedia}")
        Text("Owami: ${stats.owamiMedia}")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Total Emojis:")
        Text("Lefa: ${stats.lefaEmojis}")
        Text("Owami: ${stats.owamiEmojis}")
    }

    StatCard("❤️ Love Meter") {
        Text("Number of 'Love You's:")
        Text("Lefa: ${stats.lefaLove} ❤️")
        Text("Owami: ${stats.owamiLove} ❤️")
        if (stats.lefaLove > stats.owamiLove) {
            Text("Winner: Lefa! (He loves you more 😜)", fontWeight = FontWeight.Bold)
        } else {
            Text("Winner: Owami! (She loves you more 😜)", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PageTwo(stats: Stats) {
    StatCard("😈 Naughty & Nice") {
        Text("Naughty Words Score:")
        Text("Lefa: ${stats.lefaNaughty} 😈")
        Text("Owami: ${stats.owamiNaughty} 😈")
        Text("Winner: ${if(stats.owamiNaughty > stats.lefaNaughty) "Owami" else "Lefa"} is naughtier!")

        Spacer(modifier = Modifier.height(8.dp))
        Text("Annoyance (K, whatever, nvm):")
        Text("Lefa: ${stats.lefaAnnoyed}")
        Text("Owami: ${stats.owamiAnnoyed}")
    }

    StatCard("📅 Routines") {
        Text("Times mentioned:")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Lefa", fontWeight = FontWeight.Bold)
                stats.lefaRoutine.forEach { (k, v) -> Text("$k: $v") }
            }
            Column {
                Text("Owami", fontWeight = FontWeight.Bold)
                stats.owamiRoutine.forEach { (k, v) -> Text("$k: $v") }
            }
        }
    }
}

@Composable
fun PageThree(stats: Stats) {
    StatCard("💬 Comparisons") {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Lefa", fontWeight = FontWeight.Bold)
                Text("Asked 'How are you': ${stats.lefaActions["how are you"]}")
                Text("Said 'Sorry': ${stats.lefaActions["sorry"]}")
                Text("Good Morning: ${stats.lefaActions["good morning"]}")
                Text("Good Night: ${stats.lefaActions["good night"]}")
            }
            Column {
                Text("Owami", fontWeight = FontWeight.Bold)
                Text("Asked 'How are you': ${stats.owamiActions["how are you"]}")
                Text("Said 'Sorry': ${stats.owamiActions["sorry"]}")
                Text("Good Morning: ${stats.owamiActions["good morning"]}")
                Text("Good Night: ${stats.owamiActions["good night"]}")
            }
        }
    }

    StatCard("🔥 Streaks") {
        Text("Most Consecutive Texts:")
        Text("Lefa: ${stats.lefaConsecutive}")
        Text("Owami: ${stats.owamiConsecutive}")
    }

    StatCard("💘 First Signs") {
        stats.firstSigns.take(5).forEach {
            Text("• $it", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
        }
        if (stats.firstSigns.size > 5) {
            Text("...and many more moments.", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun PageFour(stats: Stats) {
    // Animation Page
    StatCard("🍆🍑 The Special Animation") {
        AnimationPlayground()
    }
}

@Composable
fun PageFive(stats: Stats) {
    StatCard("👑 The Final Verdict") {
        val lefaScore = stats.lefaLove + stats.lefaMsgs / 100
        val owamiScore = stats.owamiLove + stats.owamiMsgs / 100

        Text("Based on our chat history...", fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Lefa is the protector, the consistent lover.")
        Text("Owami is the spark, the naughty energy.")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Together: A perfect match ❤️")
    }
}

@Composable
fun AnimationPlayground() {
    var splashCount by remember { mutableStateOf(0) }
    var showEmoji by remember { mutableStateOf(false) }
    var offset by remember { mutableStateOf(0f) }

    // Simple animation logic simulation (click to animate)
    // In a real app, use Animatable

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier
            .height(200.dp)
            .fillMaxWidth()
            .clickable {
                splashCount++
                if (splashCount >= 3) showEmoji = true
                if (splashCount >= 5) {
                    // Reset or Next Logic
                    splashCount = 0
                    showEmoji = false
                }
            },
            contentAlignment = Alignment.Center
        ) {
            Text("🍆", fontSize = 60.sp, modifier = Modifier.offset(x = (-50 + (splashCount * 10)).dp))
            Text("🍑", fontSize = 60.sp, modifier = Modifier.offset(x = (50 - (splashCount * 10)).dp))

            if (splashCount > 0) {
                Text("💦", fontSize = 40.sp, modifier = Modifier.offset(y = (-50).dp))
            }

            if (showEmoji) {
                Text("🤭", fontSize = 40.sp, modifier = Modifier.offset(y = 50.dp))
            }
        }
        Text("Tap to play! (${splashCount}/5)", color = Color.Gray)
    }
}
