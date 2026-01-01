package com.lefa.thearchive.model

import java.io.Serializable
import java.util.Date

data class Stats(
    val totalMessages: Int = 0,
    val totalWords: Int = 0,
    val totalEmojis: Int = 0,
    val mediaCount: Int = 0,
    val lefaMsgs: Int = 0,
    val owamiMsgs: Int = 0,
    val lefaWords: Int = 0,
    val owamiWords: Int = 0,
    val lefaMedia: Int = 0,
    val owamiMedia: Int = 0,
    val lefaLove: Int = 0,
    val owamiLove: Int = 0,
    val lefaNaughty: Int = 0,
    val owamiNaughty: Int = 0,
    val lefaAnnoyed: Int = 0,
    val owamiAnnoyed: Int = 0,
    val lefaStreak: Int = 0,
    val owamiStreak: Int = 0,

    // Routines
    val routine: Map<String, Pair<Int, Int>> = emptyMap(), // Key -> (Lefa, Owami)
    val actions: Map<String, Pair<Int, Int>> = emptyMap(), // Key -> (Lefa, Owami)

    // First Signs
    val firstSigns: List<SignEvent> = emptyList(),

    // Legacy fields (keeping for compatibility if needed, but defaulting to empty/null)
    val dateRange: String = "",
    val startDate: Date = Date(),
    val messageCounts: Map<String, Int> = emptyMap(),
    val loveCounts: Map<String, Int> = emptyMap(),
    val specificWordCounts: Map<String, Map<String, Int>> = emptyMap(),
    val hourlyActivity: Map<Int, Int> = emptyMap(),
    val replyPairs: List<ReplyPair> = emptyList(),
    val firstOccurrences: Map<String, Pair<String, Date>> = emptyMap(),
    val nocturnalMessages: Int = 0,
    val messagesByDate: Map<String, List<Message>> = emptyMap()
) : Serializable

data class SignEvent(
    val date: String,
    val sender: String,
    val phrase: String,
    val message: String
) : Serializable

data class ReplyPair(
    val original: List<Message>,
    val reply: Message
) : Serializable

object StaticStats {
    val data = Stats(
        totalMessages = 87615,
        totalWords = 500967,
        totalEmojis = 51606,
        mediaCount = 13235,
        lefaMsgs = 45666,
        owamiMsgs = 41949,
        lefaWords = 271591,
        owamiWords = 229376,
        lefaMedia = 7319,
        owamiMedia = 5916,
        lefaLove = 375,
        owamiLove = 424,
        lefaNaughty = 1197,
        owamiNaughty = 582,
        lefaAnnoyed = 316,
        owamiAnnoyed = 149,
        lefaStreak = 55,
        owamiStreak = 33,

        routine = mapOf(
            "how_are_you" to (163 to 27),
            "sorry" to (242 to 331),
            "good_morning" to (350 to 297),
            "good_night" to (338 to 285)
        ),
        actions = mapOf(
            "poop" to (50 to 40),
            "cook" to (154 to 124),
            "eat" to (637 to 658),
            "pee" to (133 to 111),
            "sleep" to (499 to 724),
            "heading_out" to (52 to 49),
            "going_somewhere" to (1 to 5)
        ),
        firstSigns = listOf(
            SignEvent("2024/07/26", "lefa", "i like you", "😻I like you 😻"),
            SignEvent("2024/08/03", "lefa", "i like you", "🖤I just remembered that I like you"),
            SignEvent("2024/09/11", "lefa", "i like you", "I like your voice a lot"),
            SignEvent("2024/09/11", "owami", "i like you", "I like yours too"),
            SignEvent("2024/09/12", "lefa", "i like you", "Cz I like you 🙂"),
            SignEvent("2024/09/20", "lefa", "i like you", "I like you more n more by the day"),
            SignEvent("2024/10/02", "owami", "i like you", "I like you... actually i love you🥹"),
            SignEvent("2025/11/29", "lefa", "i like you", "I like you skin colour 🙂❤️🫶")
        )
    )
}
