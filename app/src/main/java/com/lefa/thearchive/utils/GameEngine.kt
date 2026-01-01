package com.lefa.thearchive.utils

import com.lefa.thearchive.model.Message
import com.lefa.thearchive.model.Stats
import kotlin.random.Random
import java.util.Date

data class Question(
    val type: QuestionType,
    val text: String,
    val correctAnswer: String,
    val options: List<String>,
    val context: String? = null
)

enum class QuestionType {
    WHO_SAID_IT
}

object GameEngine {

    // Hardcoded Stats
    val stats = Stats(
        totalMessages = 42072 + 45543,
        dateRange = "2024/07/26 - 2025/12/31",
        startDate = Date(1721952000000), // Approx 2024/07/26
        messageCounts = mapOf("lefa" to 42072, "owami" to 45543),
        loveCounts = mapOf("lefa" to 448, "owami" to 398),
        specificWordCounts = emptyMap(),
        hourlyActivity = emptyMap(),
        replyPairs = emptyList(),
        firstOccurrences = emptyMap(),
        nocturnalMessages = 0,
        messagesByDate = emptyMap(),
        lefaMsgs = 42072,
        owamiMsgs = 45543,
        lefaChars = 1186243,
        owamiChars = 1319604,
        lefaWords = 230997,
        owamiWords = 269265,
        lefaEmojis = 21056,
        owamiEmojis = 41251,
        lefaMedia = 5936,
        owamiMedia = 7299,
        lefaLove = 448,
        owamiLove = 398,
        lefaNaughty = 629,
        owamiNaughty = 1278,
        lefaAnnoyed = 178,
        owamiAnnoyed = 287,
        lefaConsecutive = 33,
        owamiConsecutive = 55,
        lefaRoutine = mapOf(
            "sleep" to 607,
            "eat" to 588,
            "cook" to 122,
            "pee" to 106,
            "poop" to 41,
            "going somewhere" to 5,
        ),
        owamiRoutine = mapOf(
            "pee" to 138,
            "sleep" to 415,
            "eat" to 543,
            "cook" to 156,
            "poop" to 49,
            "going somewhere" to 1,
        ),
        lefaActions = mapOf(
            "how are you" to 40,
            "sorry" to 318,
            "good morning" to 53,
            "good night" to 69,
        ),
        owamiActions = mapOf(
            "how are you" to 150,
            "sorry" to 204,
            "good morning" to 218,
            "good night" to 254,
        ),
        firstSigns = listOf(
            "2024/07/26 - lefa: 😹I like you 😹",
            "2024/08/03 - lefa: 🖤I just remembered that I like you",
            "2024/09/11 - lefa: I like your voice a lot",
            "2024/09/11 - owami: I like yours too.",
            "2024/09/12 - lefa: Cz I like you 🙂",
            "2024/09/20 - lefa: I like you more n more by the day",
            "2024/09/20 - lefa: 🙂🖤I like your boobs",
            "2024/10/18 - lefa: I like your confidence <This message was edited>",
            "2024/12/03 - lefa: I like you... you're truly different.",
            "2025/01/04 - owami: First pic you look different n I like your boobs",
            "2025/02/20 - owami: I like your smile ♥️🤭",
            "2025/06/08 - owami: I like your chair",
            "2025/06/18 - lefa: Why are you asking... you were busy saying \"i like you\" before that, then shift to \"i slightly love you\" then boom a html.",
            "2025/06/20 - owami: I love you because your you and that's more than enough , but considering that yours also you and I know your you I should probably be more precise 😮‍💨 okay let's see , _why do I love you_ the way you talk to me and the way we talk to each other makes me feel happy and comfortable like a wife at home after a long day of work who welcomes and takes care of me I love that about you ❤️ , personality is quite lovely as well you can cope with a lot of random things I say or do and we just processed to be normal that make me feel even more at home when I talk to you , I like your eyes they look cute and also they do a good job of hiding that naughty side you have ❤️ and obviously as you know I've taken a liking to your boobs(the shape , size , textures, and nipples🤭) so that plays a role too , wasn't completely aware up until recently but you have an astonishing figure, buts that small belly thing _your eyes , boobs , and your body_ combined make you the woman to my dearest of dreams 🫂and obviously we can't forget the face 😹❤️you have everything doing on there may it be cute, funny, unknown or something else your by far and always be beautiful in my eyes 🫂 ..😮‍💨 now what did I leave out .. ohh yes I like your voice just the way you say things brings a smile to my face like for instance the iconic \"oh yes oh yes\" or the \" c'mon\" 🌹 , yh n I Also love that naughty side that pops up once in a while , as well as the caring side you always have doing in and most importantly your ability to adapt is what fascinates me the most . And all that and more is why I love you ❤️",
            "2025/06/23 - lefa: I like your nickname",
        )
    )

    // 1. Who Said It (Classic)
    fun generateWhoSaidIt(messages: List<Message>, count: Int = 1): List<Question> {
        val candidates = messages.filter {
            !it.content.contains("<Media omitted>") &&
            it.content.length > 20 &&
            !it.content.contains("Messages and calls are end-to-end encrypted")
        }.toMutableList()

        val questions = mutableListOf<Question>()
        repeat(count) {
            if (candidates.isEmpty()) return@repeat
            val randomIndex = Random.nextInt(candidates.size)
            val msg = candidates[randomIndex]

            questions.add(
                Question(
                    type = QuestionType.WHO_SAID_IT,
                    text = msg.content,
                    correctAnswer = msg.author,
                    options = listOf("lefa", "owami")
                )
            )
            candidates.removeAt(randomIndex)
        }
        return questions
    }
}
