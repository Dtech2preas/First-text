package com.lefa.thearchive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lefa.thearchive.model.Stats
import com.lefa.thearchive.ui.theme.*

@Composable
fun DashboardScreen(stats: Stats, onContinue: () -> Unit) {
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "LOVE REPORT",
                color = DeepLove,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(vertical = 20.dp)
            )

            // Total Messages
            StatCard("Total Memories", stats.totalMessages.toString())

            // Message Count Split
            val lefaCount = stats.messageCounts["lefa"] ?: 0
            val owamiCount = stats.messageCounts["owami"] ?: 0
            ComparisonCard("Messages Sent", lefaCount, owamiCount)

            // "I Love You" Count
            val lefaLove = stats.loveCounts["lefa"] ?: 0
            val owamiLove = stats.loveCounts["owami"] ?: 0
            ComparisonCard("Said 'I Love You'", lefaLove, owamiLove)

            // Specific Words
            stats.specificWordCounts.forEach { (word, counts) ->
                ComparisonCard("Said '$word'", counts["lefa"] ?: 0, counts["owami"] ?: 0)
            }

            // Most Active Hour
            val maxHour = stats.hourlyActivity.maxByOrNull { it.value }?.key ?: 0
            val timeStr = if (maxHour < 12) "$maxHour AM" else if (maxHour == 12) "12 PM" else "${maxHour-12} PM"
            StatCard("Most Active Time", timeStr)

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = DeepLove),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text("SEE THE MESSAGE", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
fun StatCard(title: String, value: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, color = TextSecondary, fontSize = 14.sp)
            Text(value, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ComparisonCard(title: String, lefaVal: Int, owamiVal: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = PureWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, color = TextSecondary, fontSize = 14.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Lefa", color = TextSecondary, fontSize = 12.sp)
                    Text(lefaVal.toString(), color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                // Simple Bar Viz
                val total = (lefaVal + owamiVal).toFloat().coerceAtLeast(1f)
                val lefaPct = lefaVal / total
                val owamiPct = owamiVal / total

                Row(
                    modifier = Modifier
                        .width(150.dp)
                        .height(10.dp)
                        .background(RoseSurface, RoundedCornerShape(5.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(if (lefaPct == 0f) 0.001f else lefaPct)
                            .background(SoftAccent, RoundedCornerShape(topStart = 5.dp, bottomStart = 5.dp))
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .weight(if (owamiPct == 0f) 0.001f else owamiPct)
                            .background(DeepLove, RoundedCornerShape(topEnd = 5.dp, bottomEnd = 5.dp))
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Owami", color = TextSecondary, fontSize = 12.sp)
                    Text(owamiVal.toString(), color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
