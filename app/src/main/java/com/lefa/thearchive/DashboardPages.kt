package com.lefa.thearchive

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lefa.thearchive.ui.theme.*
import com.lefa.thearchive.utils.GameEngine
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardPages(onNavigateBack: () -> Unit) {
    val stats = GameEngine.stats
    val pagerState = rememberPagerState(pageCount = { 5 })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(RoseBackground, PureWhite)))
    ) {
        // --- Pager ---
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> OverviewPage(stats)
                1 -> LoveStatsPage(stats)
                2 -> ActivityStatsPage(stats)
                3 -> NaughtyStatsPage(stats)
                4 -> FinalePage(onNavigateBack)
            }
        }

        // --- Indicators ---
        Row(
            Modifier
                .height(50.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(5) { iteration ->
                val color = if (pagerState.currentPage == iteration) DeepLove else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(10.dp)
                        .clickable {
                            scope.launch {
                                pagerState.animateScrollToPage(iteration)
                            }
                        }
                )
            }
        }
    }
}

// --- REUSABLE COMPONENTS ---

@Composable
fun InfoCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit // FIXED: Added space between @Composable and ColumnScope
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
fun StatItem(label: String, valueLefa: String, valueOwami: String, isHeader: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            color = if (isHeader) TextPrimary else TextSecondary,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            valueLefa,
            modifier = Modifier.weight(0.5f),
            textAlign = TextAlign.Center,
            color = if (isHeader) DeepLove else TextPrimary,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            valueOwami,
            modifier = Modifier.weight(0.5f),
            textAlign = TextAlign.Center,
            color = if (isHeader) DeepLove else TextPrimary,
            fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// --- PAGES ---

@Composable
fun OverviewPage(stats: com.lefa.thearchive.model.Stats) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            "Overview",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = DeepLove,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        InfoCard("Total Interaction") {
            StatItem("", "Lefa", "Owami", isHeader = true)
            HorizontalDivider(color = RoseSurface, modifier = Modifier.padding(vertical = 8.dp))
            StatItem("Messages", "${stats.lefaMsgs}", "${stats.owamiMsgs}")
            StatItem("Words", "${stats.lefaWords}", "${stats.owamiWords}")
            StatItem("Characters", "${stats.lefaChars}", "${stats.owamiChars}")
            StatItem("Emojis", "${stats.lefaEmojis}", "${stats.owamiEmojis}")
            StatItem("Media", "${stats.lefaMedia}", "${stats.owamiMedia}")
        }

        InfoCard("Consecutive Messages") {
             StatItem("Most in a row", "${stats.lefaConsecutive}", "${stats.owamiConsecutive}")
        }
    }
}

@Composable
fun LoveStatsPage(stats: com.lefa.thearchive.model.Stats) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Favorite, contentDescription = null, tint = DeepLove)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Romance",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = DeepLove
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        InfoCard("Expressions of Love") {
            StatItem("", "Lefa", "Owami", isHeader = true)
            HorizontalDivider(color = RoseSurface, modifier = Modifier.padding(vertical = 8.dp))
            StatItem("'I Love You'", "${stats.lefaLove}", "${stats.owamiLove}")
        }

        InfoCard("First Signs") {
             Text(
                "Who said it first?",
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            stats.firstSigns.forEach { sign ->
                 Text("• $sign", color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

@Composable
fun ActivityStatsPage(stats: com.lefa.thearchive.model.Stats) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
         Text(
            "Routine",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = DeepLove,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        InfoCard("Daily Life") {
             StatItem("", "Lefa", "Owami", isHeader = true)
             HorizontalDivider(color = RoseSurface, modifier = Modifier.padding(vertical = 8.dp))

             val allKeys = (stats.lefaRoutine.keys + stats.owamiRoutine.keys).distinct()

             allKeys.forEach { key ->
                 StatItem(
                     key.replaceFirstChar { it.uppercase() },
                     "${stats.lefaRoutine[key] ?: 0}",
                     "${stats.owamiRoutine[key] ?: 0}"
                 )
             }
        }

         InfoCard("Comparisons") {
             val allActions = (stats.lefaActions.keys + stats.owamiActions.keys).distinct()
             allActions.forEach { key ->
                  StatItem(
                     key.replaceFirstChar { it.uppercase() },
                     "${stats.lefaActions[key] ?: 0}",
                     "${stats.owamiActions[key] ?: 0}"
                 )
             }
         }
    }
}

@Composable
fun NaughtyStatsPage(stats: com.lefa.thearchive.model.Stats) {
    val infiniteTransition = rememberInfiniteTransition()
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = RepeatMode.Reverse
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
         Text(
            "Late Night 😈",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = DeepLove,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        InfoCard("Heat Level") {
             StatItem("", "Lefa", "Owami", isHeader = true)
             HorizontalDivider(color = RoseSurface, modifier = Modifier.padding(vertical = 8.dp))
             StatItem("Spice Count", "${stats.lefaNaughty}", "${stats.owamiNaughty}")
             StatItem("Slightly Mad", "${stats.lefaAnnoyed}", "${stats.owamiAnnoyed}")
        }

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(RoseSurface.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "🍆 🍑 💦",
                fontSize = 50.sp,
                color = Color.White.copy(alpha = alpha)
            )
        }
    }
}

@Composable
fun FinalePage(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "One Last Thing...",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = DeepLove
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onNavigateBack,
            colors = ButtonDefaults.buttonColors(containerColor = DeepLove),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            elevation = ButtonDefaults.buttonElevation(8.dp)
        ) {
            Text("READ FINAL LETTER", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}
