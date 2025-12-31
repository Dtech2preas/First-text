package com.lefa.thearchive.model

data class Stats(
    val totalMessages: Int,
    val dateRange: String,
    val messageCounts: Map<String, Int>, // "lefa" -> 100, "owami" -> 120
    val loveCounts: Map<String, Int>, // "lefa" -> 50, "owami" -> 60 (said "I love you")
    val specificWordCounts: Map<String, Map<String, Int>>, // "Baby" -> {"lefa": 10, ...}
    val hourlyActivity: Map<Int, Int>, // Hour (0-23) -> Count
    val replyPairs: List<ReplyPair> // For "Guess the Reply" game
)

data class ReplyPair(
    val original: Message,
    val reply: Message
)
