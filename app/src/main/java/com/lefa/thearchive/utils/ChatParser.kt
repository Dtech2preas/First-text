package com.lefa.thearchive.utils

import com.lefa.thearchive.model.Message
import com.lefa.thearchive.model.PersistedData
import com.lefa.thearchive.model.ReplyPair
import com.lefa.thearchive.model.Stats
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

object ChatParser {

    // Supports "2024/07/26, 9:31 pm" and "2024/07/26, 21:31"
    private val PATTERN = Pattern.compile("^(\\d{4}/\\d{2}/\\d{2}), (\\d{1,2}:\\d{2}).?([ap]m)? - (.*?): (.*)", Pattern.CASE_INSENSITIVE)

    fun parseChat(fileContent: String): List<Message> {
        val lines = fileContent.split("\n")
        val messages = mutableListOf<Message>()

        for (line in lines) {
            // Filter out system messages that might look like chat lines but aren't
            if (line.contains("Messages and calls are end-to-end encrypted") ||
                line.contains("Chats connection", ignoreCase = true)) {
                continue
            }

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
                } else if (sender.contains("owami", ignoreCase = true)) {
                    author = "owami"
                } else {
                    author = "owami"
                }

                val timestamp = parseDate(dateStr, timeStr, ampm)

                messages.add(
                    Message(
                        fullDate = dateStr,
                        time = "$timeStr $ampm".trim(),
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

    /**
     * Generates lightweight PersistedData from the list of messages.
     * This avoids creating heavy derived objects (like ReplyPairs) that cause duplication in JSON.
     */
    fun generatePersistedData(messages: List<Message>): PersistedData {
        val messageCounts = mutableMapOf("lefa" to 0, "owami" to 0)
        val loveCounts = mutableMapOf("lefa" to 0, "owami" to 0)
        val wordCounts = mutableMapOf<String, MutableMap<String, Int>>()
        val hourlyActivity = mutableMapOf<Int, Int>()
        val firstOccurrences = mutableMapOf<String, Pair<String, Date>>()
        var nocturnalMessages = 0

        // Additional Stats
        var lefaMsgs = 0
        var owamiMsgs = 0
        var lefaChars = 0
        var owamiChars = 0
        var lefaWords = 0
        var owamiWords = 0
        var lefaEmojis = 0
        var owamiEmojis = 0
        var lefaMedia = 0
        var owamiMedia = 0
        var lefaLove = 0
        var owamiLove = 0
        var lefaNaughty = 0
        var owamiNaughty = 0
        var lefaAnnoyed = 0
        var owamiAnnoyed = 0

        val specialWords = listOf("baby", "my love", "babe", "honey", "miss you")
        val firstWordsToTrack = listOf("i love you", "baby", "my love")

        val naughtyWords = listOf("cum", "vagina", "dick", "hoohaa", "sugar cane", "sex", "porn", "nipples", "boobs")
        val annoyanceWords = listOf("k", "whatever", "nvm", "fine")

        specialWords.forEach { word ->
            wordCounts[word] = mutableMapOf("lefa" to 0, "owami" to 0)
        }

        val calendar = Calendar.getInstance()

        for (msg in messages) {
            val contentLower = msg.content.lowercase()
            val charCount = msg.content.length
            val wordCount = msg.content.split("\\s+".toRegex()).size
            val emojiCount = msg.content.count { Character.getType(it).toByte() == Character.SURROGATE } / 2 // Rough estimate

            // Message Count
            messageCounts[msg.author] = (messageCounts[msg.author] ?: 0) + 1

            if (msg.author == "lefa") {
                lefaMsgs++
                lefaChars += charCount
                lefaWords += wordCount
                lefaEmojis += emojiCount
                if (msg.content.contains("<Media omitted>")) lefaMedia++
            } else if (msg.author == "owami") {
                owamiMsgs++
                owamiChars += charCount
                owamiWords += wordCount
                owamiEmojis += emojiCount
                if (msg.content.contains("<Media omitted>")) owamiMedia++
            }

            // Love Count
            if (contentLower.contains("i love you")) {
                loveCounts[msg.author] = (loveCounts[msg.author] ?: 0) + 1
                if (msg.author == "lefa") lefaLove++ else if (msg.author == "owami") owamiLove++
            }

            // Naughty & Annoyance
            if (naughtyWords.any { contentLower.contains(it) }) {
                if (msg.author == "lefa") lefaNaughty++ else if (msg.author == "owami") owamiNaughty++
            }
            if (annoyanceWords.any { contentLower == it }) { // Exact match for annoyance usually
                 if (msg.author == "lefa") lefaAnnoyed++ else if (msg.author == "owami") owamiAnnoyed++
            }

            // Word Counts
            specialWords.forEach { word ->
                if (contentLower.contains(word)) {
                    wordCounts[word]?.let {
                        it[msg.author] = (it[msg.author] ?: 0) + 1
                    }
                }
            }

            // First Occurrences
            firstWordsToTrack.forEach { word ->
                if (contentLower.contains(word) && !firstOccurrences.containsKey(word)) {
                    firstOccurrences[word] = Pair(msg.author, msg.timestamp)
                }
            }

            // Hourly Activity & Nocturnal
            calendar.time = msg.timestamp
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            hourlyActivity[hour] = (hourlyActivity[hour] ?: 0) + 1

            if (hour in 0..4) {
                nocturnalMessages++
            }
        }

        val dateRange = if (messages.isNotEmpty()) {
            val start = messages.first().fullDate
            val end = messages.last().fullDate
            "$start - $end"
        } else ""

        return PersistedData(
            messages = messages,
            totalMessages = messages.size,
            dateRange = dateRange,
            messageCounts = messageCounts,
            loveCounts = loveCounts,
            specificWordCounts = wordCounts,
            hourlyActivity = hourlyActivity,
            firstOccurrences = firstOccurrences,
            nocturnalMessages = nocturnalMessages,
            // Populating detailed stats
            lefaMsgs = lefaMsgs,
            owamiMsgs = owamiMsgs,
            lefaChars = lefaChars,
            owamiChars = owamiChars,
            lefaWords = lefaWords,
            owamiWords = owamiWords,
            lefaEmojis = lefaEmojis,
            owamiEmojis = owamiEmojis,
            lefaMedia = lefaMedia,
            owamiMedia = owamiMedia,
            lefaLove = lefaLove,
            owamiLove = owamiLove,
            lefaNaughty = lefaNaughty,
            owamiNaughty = owamiNaughty,
            lefaAnnoyed = lefaAnnoyed,
            owamiAnnoyed = owamiAnnoyed
        )
    }

    /**
     * Reconstructs the full Stats object from PersistedData.
     * Derived fields like ReplyPairs and MessagesByDate are calculated in-memory here.
     */
    fun reconstructStats(data: PersistedData): Stats {
        val messagesByDate = mutableMapOf<String, MutableList<Message>>()
        val dayFormat = SimpleDateFormat("MM-dd", Locale.US)
        val replyPairs = mutableListOf<ReplyPair>()

        // 1. Group Messages by Date
        for (msg in data.messages) {
            val dayKey = dayFormat.format(msg.timestamp)
            messagesByDate.getOrPut(dayKey) { mutableListOf() }.add(msg)
        }

        // 2. Generate Reply Pairs (In-Memory Only)
        // This logic is fast enough to run on load
        val messages = data.messages
        for (i in 0 until messages.size - 1) {
            val msg = messages[i]
            val nextMsg = messages[i + 1]
            if (msg.author != nextMsg.author && msg.author != "unknown" && nextMsg.author != "unknown") {

                val block = mutableListOf<Message>()
                block.add(msg)

                var backIndex = i - 1
                var count = 0
                while (backIndex >= 0 && messages[backIndex].author == msg.author && count < 5) {
                    block.add(0, messages[backIndex])
                    backIndex--
                    count++
                }

                val timeDiff = nextMsg.timestamp.time - msg.timestamp.time
                val combinedLength = block.sumOf { it.content.length }

                if (timeDiff < 3600000 && combinedLength > 5 && nextMsg.content.length > 5) {
                    replyPairs.add(ReplyPair(block, nextMsg))
                }
            }
        }

        val startDate = if (data.messages.isNotEmpty()) data.messages.first().timestamp else Date()

        return Stats(
            totalMessages = data.totalMessages,
            dateRange = data.dateRange,
            startDate = startDate,
            messageCounts = data.messageCounts,
            loveCounts = data.loveCounts,
            specificWordCounts = data.specificWordCounts,
            hourlyActivity = data.hourlyActivity,
            replyPairs = replyPairs,
            firstOccurrences = data.firstOccurrences,
            nocturnalMessages = data.nocturnalMessages,
            messagesByDate = messagesByDate,
            // Pass through new fields if they exist in PersistedData
            lefaMsgs = data.lefaMsgs,
            owamiMsgs = data.owamiMsgs,
            lefaChars = data.lefaChars,
            owamiChars = data.owamiChars,
            lefaWords = data.lefaWords,
            owamiWords = data.owamiWords,
            lefaEmojis = data.lefaEmojis,
            owamiEmojis = data.owamiEmojis,
            lefaMedia = data.lefaMedia,
            owamiMedia = data.owamiMedia,
            lefaLove = data.lefaLove,
            owamiLove = data.owamiLove,
            lefaNaughty = data.lefaNaughty,
            owamiNaughty = data.owamiNaughty,
            lefaAnnoyed = data.lefaAnnoyed,
            owamiAnnoyed = data.owamiAnnoyed,
            lefaConsecutive = data.lefaConsecutive,
            owamiConsecutive = data.owamiConsecutive,
            lefaRoutine = data.lefaRoutine,
            owamiRoutine = data.owamiRoutine,
            lefaActions = data.lefaActions,
            owamiActions = data.owamiActions,
            firstSigns = data.firstSigns
        )
    }

    private fun parseDate(dateStr: String, timeStr: String, ampm: String): Date {
        try {
            val formattedDateStr = dateStr.replace("/", "-")
            val rawTime = if (ampm.isNotEmpty()) "$formattedDateStr $timeStr $ampm" else "$formattedDateStr $timeStr"
            val pattern = if (ampm.isNotEmpty()) "yyyy-MM-dd h:mm a" else "yyyy-MM-dd HH:mm"
            val format = SimpleDateFormat(pattern, Locale.US)
            return format.parse(rawTime) ?: Date()
        } catch (e: Exception) {
            return Date()
        }
    }
}
