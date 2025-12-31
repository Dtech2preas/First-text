package com.lefa.thearchive.utils

import com.lefa.thearchive.model.Message
import java.util.Date
import kotlin.random.Random

data class Question(
    val type: QuestionType,
    val text: String,
    val correctAnswer: String,
    val options: List<String>
)

enum class QuestionType {
    WHO_SAID_IT,
    WHEN_WAS_IT
}

object GameEngine {

    fun generateWhoSaidIt(messages: MutableList<Message>, count: Int = 1): List<Question> {
        val candidates = messages.filter {
            !it.content.contains("<Media omitted>") &&
            it.content.length > 10 &&
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

    fun generateWhenWasIt(messages: MutableList<Message>, count: Int = 1): List<Question> {
        val candidates = messages.filter {
            !it.content.contains("<Media omitted>") &&
            it.content.length > 15
        }.toMutableList()

        val questions = mutableListOf<Question>()
        repeat(count) {
            if (candidates.isEmpty()) return@repeat
            val randomIndex = Random.nextInt(candidates.size)
            val msg = candidates[randomIndex]
            val trueDate = msg.timestamp

            // Format trueDate to ISO string equivalent for comparison (simplified)
            // Original JS used toISOString(). We'll use a standard format or just milliseconds string for uniqueness
            // but the UI expects to parse it back. Let's use ISO-8601 like string.
            val isoFormat = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US)
            isoFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
            val correctAnsStr = isoFormat.format(trueDate)

            val options = mutableListOf<String>()
            options.add(correctAnsStr)

            while (options.size < 4) {
                val randomOffset = (Random.nextLong(10000000000L) * if (Random.nextBoolean()) 1 else -1)
                val fakeDate = Date(trueDate.time + randomOffset)
                val fakeStr = isoFormat.format(fakeDate)

                // Ensure unique options (simplistic check)
                if (!options.contains(fakeStr)) {
                    options.add(fakeStr)
                }
            }

            questions.add(
                Question(
                    type = QuestionType.WHEN_WAS_IT,
                    text = "\"" + msg.content.take(50) + "...\"",
                    correctAnswer = correctAnsStr,
                    options = options.shuffled()
                )
            )
            candidates.removeAt(randomIndex)
        }
        return questions
    }
}
