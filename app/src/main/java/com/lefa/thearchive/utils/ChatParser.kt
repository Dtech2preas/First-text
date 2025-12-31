package com.lefa.thearchive.utils

import com.lefa.thearchive.model.Message
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

object ChatParser {

    // Regex to match: 2024/07/26, 9:31 pm - Name: Message
    // ^(\d{4}/\d{2}/\d{2}), (\d{1,2}:\d{2}).?([ap]m) - (.*?): (.*)
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
                // Multi-line support
                if (messages.isNotEmpty()) {
                    val lastMsg = messages.last()
                    // Re-create last message with appended content
                    val newContent = lastMsg.content + "\n" + line
                    messages[messages.lastIndex] = lastMsg.copy(content = newContent)
                }
            }
        }
        return messages
    }

    private fun parseDate(dateStr: String, timeStr: String, ampm: String): Date {
        // dateStr: 2024/07/26
        // timeStr: 9:31
        // ampm: pm
        // We need 24h format for simpler Date creation or just use SimpleDateFormat
        try {
            val formattedDateStr = dateStr.replace("/", "-")
            val rawTime = "$formattedDateStr $timeStr $ampm"
            // SimpleDateFormat: yyyy-MM-dd h:mm a
            val format = SimpleDateFormat("yyyy-MM-dd h:mm a", Locale.US)
            return format.parse(rawTime) ?: Date()
        } catch (e: Exception) {
            return Date()
        }
    }
}
