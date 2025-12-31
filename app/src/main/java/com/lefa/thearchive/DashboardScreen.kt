package com.lefa.thearchive

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lefa.thearchive.model.Message
import com.lefa.thearchive.model.Stats
import com.lefa.thearchive.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(stats: Stats, onContinue: () -> Unit) {
    val scrollState = rememberScrollState()
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

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

            // --- SEARCH SECTION ---
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search our memories...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = DeepLove) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    focusedBorderColor = DeepLove,
                    unfocusedBorderColor = SoftAccent
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(15.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            if (searchQuery.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                SearchResultCard(stats, searchQuery)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- FIRST TIME SECTION ---
            Text("FIRST TIMES", color = TextSecondary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            stats.firstOccurrences.forEach { (word, pair) ->
                FirstTimeCard(word, pair.first, pair.second, stats.startDate)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- EXISTING STATS ---
            StatCard("Total Memories", stats.totalMessages.toString())

            val lefaCount = stats.messageCounts["lefa"] ?: 0
            val owamiCount = stats.messageCounts["owami"] ?: 0
            ComparisonCard("Messages Sent", lefaCount, owamiCount)

            val lefaLove = stats.loveCounts["lefa"] ?: 0
            val owamiLove = stats.loveCounts["owami"] ?: 0
            ComparisonCard("Said 'I Love You'", lefaLove, owamiLove)

            // --- BONUS STATS ---
            Text("FUN FACTS", color = TextSecondary, fontSize = 18.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 20.dp))

            StatCard("Nocturnal Chats (12AM-4AM)", "${stats.nocturnalMessages} messages")

            // On This Day Logic
            val todayKey = SimpleDateFormat("MM-dd", Locale.US).format(Date())
            val memoriesToday = stats.messagesByDate[todayKey]
            if (!memoriesToday.isNullOrEmpty()) {
                val randomMemory = memoriesToday.random()
                MemoryCard("ON THIS DAY", randomMemory.content, randomMemory.fullDate)
            }

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

data class SearchMetrics(
    val lefaCount: Int,
    val owamiCount: Int,
    val firstEver: Message?,
    val lefaFirst: Message?,
    val owamiFirst: Message?
)

@Composable
fun SearchResultCard(stats: Stats, query: String) {
    val lowerQuery = query.lowercase()

    // Wrap heavy calculation in remember to prevent re-computation on every frame
    val metrics = remember(query, stats) {
        var lefaCount = 0
        var owamiCount = 0
        var lefaFirst: Message? = null
        var owamiFirst: Message? = null
        var firstEver: Message? = null

        val sortedKeys = stats.messagesByDate.keys.sorted()
        for (key in sortedKeys) {
            val daysMessages = stats.messagesByDate[key] ?: continue
            for (msg in daysMessages) {
                if (msg.content.contains(query, ignoreCase = true)) {
                    if (msg.author == "lefa") lefaCount++ else owamiCount++
                    if (firstEver == null) firstEver = msg
                    if (msg.author == "lefa" && lefaFirst == null) lefaFirst = msg
                    if (msg.author == "owami" && owamiFirst == null) owamiFirst = msg
                }
            }
        }
        SearchMetrics(lefaCount, owamiCount, firstEver, lefaFirst, owamiFirst)
    }

    if (metrics.lefaCount == 0 && metrics.owamiCount == 0) {
        Card(
            colors = CardDefaults.cardColors(containerColor = PureWhite),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("No results found for '$query'", color = TextSecondary, fontSize = 14.sp)
            }
        }
    } else {
        Column {
            ComparisonCard("Search: '$query'", metrics.lefaCount, metrics.owamiCount)

            // Detailed Breakdown
            Card(
                colors = CardDefaults.cardColors(containerColor = PureWhite),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Deep Dive: '$query'", color = DeepLove, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    // 1. Who said it first ever?
                    if (metrics.firstEver != null) {
                        val day0 = TimeUnit.DAYS.convert(metrics.firstEver.timestamp.time - stats.startDate.time, TimeUnit.MILLISECONDS)
                        MetricRow("First Ever Said By", metrics.firstEver.author.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }, "Day $day0")
                    }

                    Divider(color = SoftAccent, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                    // 2. Lefa Details
                    if (metrics.lefaFirst != null) {
                        val dayL = TimeUnit.DAYS.convert(metrics.lefaFirst.timestamp.time - stats.startDate.time, TimeUnit.MILLISECONDS)
                        MetricRow("Lefa First Said", SimpleDateFormat("MMM dd, yyyy", Locale.US).format(metrics.lefaFirst.timestamp), "Day $dayL")
                    } else {
                        MetricRow("Lefa First Said", "Never", "-")
                    }

                    // 3. Owami Details
                    if (metrics.owamiFirst != null) {
                        val dayO = TimeUnit.DAYS.convert(metrics.owamiFirst.timestamp.time - stats.startDate.time, TimeUnit.MILLISECONDS)
                        MetricRow("Owami First Said", SimpleDateFormat("MMM dd, yyyy", Locale.US).format(metrics.owamiFirst.timestamp), "Day $dayO")
                    } else {
                        MetricRow("Owami First Said", "Never", "-")
                    }

                    Divider(color = SoftAccent, thickness = 0.5.dp, modifier = Modifier.padding(vertical = 8.dp))

                    // 4. Lag Time
                    if (metrics.lefaFirst != null && metrics.owamiFirst != null) {
                        val diff = Math.abs(metrics.lefaFirst.timestamp.time - metrics.owamiFirst.timestamp.time)
                        val daysLag = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS)
                        val follower = if (metrics.lefaFirst.timestamp.before(metrics.owamiFirst.timestamp)) "Owami" else "Lefa"

                        MetricRow("Lag Time", "$follower took", "$daysLag days later")
                    }
                }
            }
        }
    }
}

@Composable
fun MetricRow(label: String, value: String, detail: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Column(horizontalAlignment = Alignment.End) {
            Text(value, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(detail, color = DeepLove, fontSize = 12.sp)
        }
    }
}

@Composable
fun FirstTimeCard(word: String, who: String, date: Date, startDate: Date) {
    val daysDiff = TimeUnit.DAYS.convert(date.time - startDate.time, TimeUnit.MILLISECONDS)
    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(date)

    Card(
        colors = CardDefaults.cardColors(containerColor = RoseSurface),
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("First '$word'", color = TextSecondary, fontSize = 12.sp)
                Text(who.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }, color = DeepLove, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(dateStr, color = TextPrimary, fontSize = 14.sp)
                Text("Day $daysDiff", color = TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun MemoryCard(title: String, content: String, date: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Gold.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(title, color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(5.dp))
            Text("\"$content\"", color = TextPrimary, fontSize = 16.sp, fontStyle = FontStyle.Italic)
            Spacer(modifier = Modifier.height(5.dp))
            Text("- $date", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.align(Alignment.End))
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
