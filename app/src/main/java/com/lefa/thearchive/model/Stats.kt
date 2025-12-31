package com.lefa.thearchive.model

import java.io.Serializable
import java.util.Date

data class Stats(
    val totalMessages: Int,
    val dateRange: String,
    val startDate: Date, // Added for calculations
    val messageCounts: Map<String, Int>, // "lefa" -> 100, "owami" -> 120
    val loveCounts: Map<String, Int>, // "lefa" -> 50, "owami" -> 60 (said "I love you")
    val specificWordCounts: Map<String, Map<String, Int>>, // "Baby" -> {"lefa": 10, ...}
    val hourlyActivity: Map<Int, Int>, // Hour (0-23) -> Count
    val replyPairs: List<ReplyPair>, // For "Guess the Reply" game
    // New Fields
    val firstOccurrences: Map<String, Pair<String, Date>>, // "I love you" -> ("lefa", Date)
    val nocturnalMessages: Int,
    val messagesByDate: Map<String, List<Message>> // "MM-dd" -> List<Message> for "On This Day"
) : Serializable

data class ReplyPair(
    val original: List<Message>, // Changed to list for consecutive messages
    val reply: Message
) : Serializable
