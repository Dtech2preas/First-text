package com.lefa.thearchive.utils

import com.lefa.thearchive.model.Message
import com.lefa.thearchive.model.ReplyPair
import com.lefa.thearchive.model.Stats
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

object ChatParser {

    private val PATTERN = Pattern.compile("^(\\d{4}/\\d{2}/\\d{2}), (\\d{1,2}:\\d{2}).?([ap]m) - (.*?): (.*)", Pattern.CASE_INSENSITIVE)

    fun parseChat(fileContent: String): List<Message> {
        val lines = fileContent.split("\n")
        val messages = mutableListOf<Message>()

        for (line in lines) {
            val matcher = PATTERN.matcher(line)
            if (matcher.find()) {
                val dateStr = matcher.group(1) ?: ""
                val timeStr = matcher.group(2) ?: ""
                val ampm = matcher.group(3) ?: ""
                val sender = matcher.group(4) ?: ""
                val content = matcher.group(5)?.trim() ?: ""

                var author = "unknown"
                if (sender.contains("lefa", ignoreCase = true)) {
                    author = "lefa"
                } else {
                    author = "owami"
                }

                val timestamp = parseDate(dateStr, timeStr, ampm)

                messages.add(
                    Message(
                        fullDate = dateStr,
                        time = "$timeStr $ampm",
                        timestamp = timestamp,
                        author = author,
                        content = content,
                        originalSender = sender
                    )
                )
            } else {
                if (messages.isNotEmpty()) {
                    val lastMsg = messages.last()
                    val newContent = lastMsg.content + "\n" + line
                    messages[messages.lastIndex] = lastMsg.copy(content = newContent)
                }
            }
        }
        return messages
    }

    fun generateStats(messages: List<Message>): Stats {
        val messageCounts = mutableMapOf("lefa" to 0, "owami" to 0)
        val loveCounts = mutableMapOf("lefa" to 0, "owami" to 0)
        val wordCounts = mutableMapOf<String, MutableMap<String, Int>>()
        val hourlyActivity = mutableMapOf<Int, Int>()
        val replyPairs = mutableListOf<ReplyPair>()

        val specialWords = listOf("baby", "my love", "babe", "honey", "miss you")
        specialWords.forEach { word ->
            wordCounts[word] = mutableMapOf("lefa" to 0, "owami" to 0)
        }

        val calendar = Calendar.getInstance()

        for (i in messages.indices) {
            val msg = messages[i]
            val contentLower = msg.content.lowercase()

            // Message Count
            messageCounts[msg.author] = (messageCounts[msg.author] ?: 0) + 1

            // Love Count
            if (contentLower.contains("i love you")) {
                loveCounts[msg.author] = (loveCounts[msg.author] ?: 0) + 1
            }

            // Word Counts
            specialWords.forEach { word ->
                if (contentLower.contains(word)) {
                    wordCounts[word]?.let {
                        it[msg.author] = (it[msg.author] ?: 0) + 1
                    }
                }
            }

            // Hourly Activity
            calendar.time = msg.timestamp
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            hourlyActivity[hour] = (hourlyActivity[hour] ?: 0) + 1

            // Reply Pairs (for Game)
            // Logic: If msg[i] is Lefa and msg[i+1] is Owami (or vice versa) and time diff < 1 hour
            if (i < messages.size - 1) {
                val nextMsg = messages[i + 1]
                if (msg.author != nextMsg.author && msg.author != "unknown" && nextMsg.author != "unknown") {
                    val timeDiff = nextMsg.timestamp.time - msg.timestamp.time
                    // Check if within 1 hour (3600000 ms) and content is substantial
                    if (timeDiff < 3600000 && msg.content.length > 5 && nextMsg.content.length > 5) {
                        replyPairs.add(ReplyPair(msg, nextMsg))
                    }
                }
            }
        }

        val dateRange = if (messages.isNotEmpty()) {
            val start = messages.first().fullDate
            val end = messages.last().fullDate
            "$start - $end"
        } else ""

        return Stats(
            totalMessages = messages.size,
            dateRange = dateRange,
            messageCounts = messageCounts,
            loveCounts = loveCounts,
            specificWordCounts = wordCounts,
            hourlyActivity = hourlyActivity,
            replyPairs = replyPairs
        )
    }

    private fun parseDate(dateStr: String, timeStr: String, ampm: String): Date {
        try {
            val formattedDateStr = dateStr.replace("/", "-")
            val rawTime = "$formattedDateStr $timeStr $ampm"
            val format = SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.US)
            return format.parse(rawTime) ?: Date()
        } catch (e: Exception) {
            return Date()
        }
    }
}
