package com.lefa.thearchive.utils

import com.lefa.thearchive.model.Message
import com.lefa.thearchive.model.ReplyPair
import java.util.Date
import kotlin.random.Random

data class Question(
    val type: QuestionType,
    val text: String,
    val correctAnswer: String,
    val options: List<String>,
    val context: String? = null // For "Guess Reply", context is the original message
)

enum class QuestionType {
    WHO_SAID_IT,
    GUESS_THE_REPLY,
    CHRONOLOGICAL_ORDER,
    COMPLETE_THE_PHRASE
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

    // 2. Guess The Reply (New)
    fun generateGuessTheReply(replyPairs: List<ReplyPair>, allMessages: List<Message>, count: Int = 1): List<Question> {
        val candidates = replyPairs.filter {
            it.reply.content.split(" ").size > 3 // Reply should be substantive
        }.toMutableList()

        val questions = mutableListOf<Question>()
        repeat(count) {
            if (candidates.isEmpty()) return@repeat
            val randomIndex = Random.nextInt(candidates.size)
            val pair = candidates[randomIndex]

            // Generate 3 fake replies
            val options = mutableListOf<String>()
            options.add(pair.reply.content)

            while(options.size < 4) {
                val fake = allMessages.random().content
                if (fake.length > 10 && !options.contains(fake)) {
                    options.add(fake)
                }
            }

            questions.add(
                Question(
                    type = QuestionType.GUESS_THE_REPLY,
                    text = pair.original.content, // The context
                    correctAnswer = pair.reply.content,
                    options = options.shuffled(),
                    context = "What did ${pair.reply.author} reply?"
                )
            )
            candidates.removeAt(randomIndex)
        }
        return questions
    }

    // 3. Complete the Phrase (New)
    fun generateCompleteThePhrase(messages: List<Message>, count: Int = 1): List<Question> {
        val candidates = messages.filter {
            val words = it.content.split(" ")
            words.size in 4..10 && !it.content.contains("<Media omitted>")
        }.toMutableList()

        val questions = mutableListOf<Question>()
        repeat(count) {
            if (candidates.isEmpty()) return@repeat
            val randomIndex = Random.nextInt(candidates.size)
            val msg = candidates[randomIndex]
            val words = msg.content.split(" ").toMutableList()

            // Pick a word to hide (length > 3)
            val hideIndices = words.indices.filter { words[it].length > 3 }
            if (hideIndices.isNotEmpty()) {
                val indexToHide = hideIndices.random()
                val hiddenWord = words[indexToHide]
                val cleanHiddenWord = hiddenWord.replace(Regex("[^a-zA-Z]"), "") // remove punctuation for checking

                words[indexToHide] = "_____"
                val questionText = words.joinToString(" ")

                // Generate fake words
                val options = mutableListOf(hiddenWord)
                val fakeWords = listOf("love", "baby", "home", "now", "never", "always", "funny", "weird", "sweet")
                while (options.size < 4) {
                    val fake = fakeWords.random()
                    if (!options.contains(fake) && fake != cleanHiddenWord) {
                        options.add(fake)
                    }
                }

                questions.add(
                    Question(
                        type = QuestionType.COMPLETE_THE_PHRASE,
                        text = questionText,
                        correctAnswer = hiddenWord,
                        options = options.shuffled(),
                        context = "Complete the text from ${msg.author}"
                    )
                )
            }
            candidates.removeAt(randomIndex)
        }
        return questions
    }

    // 4. Chronological Order (New)
    fun generateChronologicalOrder(messages: List<Message>, count: Int = 1): List<Question> {
        val candidates = messages.filter {
            !it.content.contains("<Media omitted>") &&
            it.content.length > 15
        }.toMutableList()

        val questions = mutableListOf<Question>()
        repeat(count) {
            if (candidates.size < 2) return@repeat

            // Pick 2 messages far apart in time (at least 7 days)
            var msg1 = candidates.random()
            var msg2 = candidates.random()
            var attempts = 0

            while (Math.abs(msg1.timestamp.time - msg2.timestamp.time) < 604800000 && attempts < 10) {
                msg2 = candidates.random()
                attempts++
            }

            if (attempts >= 10) return@repeat // Skip if can't find good pair

            val isMsg1First = msg1.timestamp.before(msg2.timestamp)
            val correct = if (isMsg1First) "Message A" else "Message B"

            questions.add(
                Question(
                    type = QuestionType.CHRONOLOGICAL_ORDER,
                    text = "A: \"${msg1.content.take(50)}...\"\n\nB: \"${msg2.content.take(50)}...\"",
                    correctAnswer = correct,
                    options = listOf("Message A", "Message B"),
                    context = "Which message was sent first?"
                )
            )
        }
        return questions
    }
}
