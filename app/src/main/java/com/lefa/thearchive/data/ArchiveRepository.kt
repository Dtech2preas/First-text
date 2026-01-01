package com.lefa.thearchive.data

import android.content.Context
import com.lefa.thearchive.R
import com.lefa.thearchive.model.Message
import com.lefa.thearchive.model.Stats
import com.lefa.thearchive.utils.ChatParser
import com.lefa.thearchive.utils.GameEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Date
import java.util.concurrent.TimeUnit

class ArchiveRepository(private val context: Context) {

    private val db = AppDatabase.getDatabase(context)
    private val dao = db.messageDao()

    private val _loadingState = MutableStateFlow<LoadingState>(LoadingState.Idle)
    val loadingState: StateFlow<LoadingState> = _loadingState

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
                // 1. Check JSON Cache for Dashboard Stats
                val cachedStats = StatsCache.loadStats(context)

                // 2. Check DB consistency (basic check: count)
                val dbCount = dao.getMessageCount()

                if (cachedStats != null && dbCount > 0) {
                    // Cache Hit
                    _loadingState.value = LoadingState.Success(cachedStats)
                } else {
                    // Cache Miss or First Run -> Parse
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
        val stats = ChatParser.generateStats(messages)

        // Save to DB
        val entities = messages.map { msg ->
            MessageEntity(
                fullDate = msg.fullDate,
                time = msg.time,
                timestamp = msg.timestamp,
                author = msg.author,
                content = msg.content,
                originalSender = msg.originalSender
            )
        }

        // Chunk insertion to avoid transaction limits if necessary, though Room handles it well usually.
        // 40k rows is fine for a single transaction in modern phones, but let's be safe.
        dao.clearAll()
        // Splitting into chunks of 1000 just in case
        entities.chunked(1000).forEach { chunk ->
            dao.insertAll(chunk)
        }

        // Save Stats to Cache
        StatsCache.saveStats(context, stats)

        _loadingState.value = LoadingState.Success(stats)
    }

    suspend fun search(query: String): SearchResult {
        return withContext(Dispatchers.IO) {
            val allMatches = dao.searchMessages(query)

            if (allMatches.isEmpty()) {
                return@withContext SearchResult(
                    query = query,
                    found = false,
                    messages = emptyList()
                )
            }

            val lefaCount = dao.countMessagesByAuthor("lefa", query)
            val owamiCount = dao.countMessagesByAuthor("owami", query)

            val firstMsg = allMatches.first()
            val firstUser = firstMsg.author

            // Days since start of chat
            val startOfChat = dao.getFirstMessageEver()?.timestamp ?: Date()
            val diffInMillies = firstMsg.timestamp.time - startOfChat.time
            val daysElapsed = TimeUnit.MILLISECONDS.toDays(diffInMillies)

            // Partner Response Time
            // Logic: Find first time User A said it, then find first time User B said it AFTER User A
            // Or just difference between first occurrences of each.
            // User requested: "How long it took the partner to say it... from the time the first partner said it"

            val lefaFirst = dao.findFirstMessageByAuthor("lefa", query)
            val owamiFirst = dao.findFirstMessageByAuthor("owami", query)

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
                messages = allMatches.map { entity ->
                    Message(
                        fullDate = entity.fullDate,
                        time = entity.time,
                        timestamp = entity.timestamp,
                        author = entity.author,
                        content = entity.content,
                        originalSender = entity.originalSender
                    )
                }
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
