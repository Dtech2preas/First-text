package com.lefa.thearchive.data

import android.content.Context
import com.lefa.thearchive.R
import com.lefa.thearchive.model.Message
import com.lefa.thearchive.model.Stats
import com.lefa.thearchive.utils.ChatParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Date
import java.util.concurrent.TimeUnit

class ArchiveRepository(private val context: Context) {

    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val loadingState: StateFlow<LoadingState> = _loadingState

    // In-memory cache of messages for searching
    private var allMessages: List<Message> = emptyList()

    sealed class LoadingState {
        object Idle : LoadingState()
        object Loading : LoadingState()
        data class Success(val stats: Stats) : LoadingState()
        data class Error(val message: String) : LoadingState()
    }

    suspend fun initialize() {
        _loadingState.value = LoadingState.Loading
        withContext(Dispatchers.IO) {
            try {
                // 1. Check if JSON data exists
                val persistedData = JsonStorage.loadData(context)

                if (persistedData != null) {
                    // Cache Hit: Reconstruct Stats from optimized JSON
                    val stats = ChatParser.reconstructStats(persistedData)
                    allMessages = persistedData.messages // Cache for search
                    _loadingState.value = LoadingState.Success(stats)
                } else {
                    // Cache Miss: Parse chat.txt and create JSON
                    performFullParse()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _loadingState.value = LoadingState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private suspend fun performFullParse() {
        val inputStream = context.resources.openRawResource(R.raw.chat)
        val reader = BufferedReader(InputStreamReader(inputStream))
        val content = reader.readText()
        reader.close()

        val messages = ChatParser.parseChat(content)
        val persistedData = ChatParser.generatePersistedData(messages)

        // Save optimized JSON
        JsonStorage.saveData(context, persistedData)

        // Hydrate full Stats object
        val stats = ChatParser.reconstructStats(persistedData)
        allMessages = messages

        _loadingState.value = LoadingState.Success(stats)
    }

    suspend fun search(query: String): SearchResult {
        return withContext(Dispatchers.Default) {
            if (allMessages.isEmpty()) {
                return@withContext SearchResult(query = query, found = false)
            }

            val queryLower = query.lowercase()
            val matches = allMessages.filter { it.content.lowercase().contains(queryLower) }

            if (matches.isEmpty()) {
                return@withContext SearchResult(query = query, found = false)
            }

            val lefaCount = matches.count { it.author == "lefa" }
            val owamiCount = matches.count { it.author == "owami" }

            val firstMsg = matches.first()
            val firstUser = firstMsg.author

            val startOfChat = allMessages.firstOrNull()?.timestamp ?: Date()
            val diffInMillies = firstMsg.timestamp.time - startOfChat.time
            val daysElapsed = TimeUnit.MILLISECONDS.toDays(diffInMillies)

            val lefaFirst = matches.find { it.author == "lefa" }
            val owamiFirst = matches.find { it.author == "owami" }

            var responseTimeStr = "Never"

            if (lefaFirst != null && owamiFirst != null) {
                val diff = kotlin.math.abs(lefaFirst.timestamp.time - owamiFirst.timestamp.time)
                val days = TimeUnit.MILLISECONDS.toDays(diff)
                if (days > 0) {
                    responseTimeStr = "$days days"
                } else {
                    val hours = TimeUnit.MILLISECONDS.toHours(diff)
                    if (hours > 0) {
                        responseTimeStr = "$hours hours"
                    } else {
                        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
                        responseTimeStr = "$minutes minutes"
                    }
                }
            }

            SearchResult(
                query = query,
                found = true,
                firstUser = firstUser,
                daysSinceStart = daysElapsed,
                partnerResponseTime = responseTimeStr,
                lefaCount = lefaCount,
                owamiCount = owamiCount,
                messages = matches
            )
        }
    }
}

data class SearchResult(
    val query: String,
    val found: Boolean,
    val firstUser: String = "",
    val daysSinceStart: Long = 0,
    val partnerResponseTime: String = "",
    val lefaCount: Int = 0,
    val owamiCount: Int = 0,
    val messages: List<Message> = emptyList()
)
