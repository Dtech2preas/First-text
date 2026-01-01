package com.lefa.thearchive.model

import java.io.Serializable

/**
 * A lightweight version of Stats used for JSON persistence.
 * This class excludes derived data structures that cause massive duplication (like ReplyPair lists of Messages).
 */
data class PersistedData(
    val messages: List<Message>, // The single source of truth for message content
    // Metrics (Cheap to store)
    val totalMessages: Int,
    val dateRange: String,
    val messageCounts: Map<String, Int>,
    val loveCounts: Map<String, Int>,
    val specificWordCounts: Map<String, Map<String, Int>>,
    val hourlyActivity: Map<Int, Int>,
    val firstOccurrences: Map<String, Pair<String, java.util.Date>>,
    val nocturnalMessages: Int,
    // New Fields
    val lefaMsgs: Int = 0,
    val owamiMsgs: Int = 0,
    val lefaChars: Int = 0,
    val owamiChars: Int = 0,
    val lefaWords: Int = 0,
    val owamiWords: Int = 0,
    val lefaEmojis: Int = 0,
    val owamiEmojis: Int = 0,
    val lefaMedia: Int = 0,
    val owamiMedia: Int = 0,
    val lefaLove: Int = 0,
    val owamiLove: Int = 0,
    val lefaNaughty: Int = 0,
    val owamiNaughty: Int = 0,
    val lefaAnnoyed: Int = 0,
    val owamiAnnoyed: Int = 0,
    val lefaConsecutive: Int = 0,
    val owamiConsecutive: Int = 0,
    val lefaRoutine: Map<String, Int> = emptyMap(),
    val owamiRoutine: Map<String, Int> = emptyMap(),
    val lefaActions: Map<String, Int> = emptyMap(),
    val owamiActions: Map<String, Int> = emptyMap(),
    val firstSigns: List<String> = emptyList()
) : Serializable
