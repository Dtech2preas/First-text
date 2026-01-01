package com.lefa.thearchive.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lefa.thearchive.data.SearchResult
import com.lefa.thearchive.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: MainViewModel,
    onClose: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    val searchResult by viewModel.searchResult.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val focusManager = LocalFocusManager.current

    // Reset search result on open if desired, or keep previous state.
    // We'll keep it so user can see previous search if they toggle back and forth.

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .clickable(enabled = true) { /* Consume clicks */ }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 40.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Search Memories",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Search Bar
            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Type something meaningful...") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25.dp),
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = PureWhite,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                trailingIcon = {
                    IconButton(onClick = {
                        viewModel.search(query)
                        focusManager.clearFocus()
                    }) {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = DeepLove)
                    }
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = {
                    viewModel.search(query)
                    focusManager.clearFocus()
                })
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (isSearching) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DeepLove)
                }
            } else {
                searchResult?.let { result ->
                    if (result.found) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                // Search Stats Card
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = RoseSurface),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            "Search Results for \"${result.query}\"",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 18.sp,
                                            color = TextPrimary
                                        )
                                        Spacer(modifier = Modifier.height(10.dp))

                                        StatRow("First said by:", result.firstUser.replaceFirstChar { it.uppercase() })
                                        StatRow("Days since start:", "${result.daysSinceStart} days")
                                        StatRow("Partner Response Time:", result.partnerResponseTime)
                                        Divider(color = Color.LightGray, modifier = Modifier.padding(vertical = 8.dp))
                                        StatRow("Total Lefa Count:", "${result.lefaCount}")
                                        StatRow("Total Owami Count:", "${result.owamiCount}")
                                    }
                                }
                            }

                            item {
                                Text(
                                    "Occurrences (${result.messages.size})",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }

                            items(result.messages) { msg ->
                                MessageItem(msg, result.query)
                            }
                        }
                    } else {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No memories found for \"${result.query}\"",
                                color = Color.White,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = TextSecondary, fontSize = 14.sp)
        Text(value, color = DeepLove, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun MessageItem(msg: com.lefa.thearchive.model.Message, query: String) {
    val isLefa = msg.author.lowercase() == "lefa"
    val align = if (isLefa) Alignment.End else Alignment.Start
    val bgColor = if (isLefa) DeepLove.copy(alpha = 0.2f) else SoftAccent.copy(alpha = 0.2f)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        Surface(
            color = bgColor,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    msg.content,
                    color = TextPrimary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${msg.author} • ${msg.fullDate}",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
