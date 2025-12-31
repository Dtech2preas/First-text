package com.lefa.thearchive.utils

import com.lefa.thearchive.model.Message
import kotlin.random.Random

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
