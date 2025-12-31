package com.lefa.thearchive.utils

import com.lefa.thearchive.model.Message
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Locale

class ChatParserTest {

    @Test
    fun testParseChat() {
        val sampleChat = """
2024/07/26, 7:41 pm - Messages and calls are end-to-end encrypted. Only people in this chat can read, listen to, or share them. Learn more.
2024/07/26, 9:31 pm - lefa: <Media omitted>
2024/07/26, 9:31 pm - ‎‎ ‎ ‎ ‎ ‎ ‎ ‎ ‎ ‎ ‎ ‎ ‎: Hi Stranger.🙂
2024/07/26, 9:31 pm - lefa: Hello human person how are you 😗
        """.trimIndent()

        val messages = ChatParser.parseChat(sampleChat)

        // Expected messages:
        // 1. lefa: <Media omitted>
        // 2. owami: Hi Stranger.🙂 (Because name is empty/blank/weird chars but NOT lefa)
        // 3. lefa: Hello human person how are you 😗

        // The first line should be skipped or parsed?
        // My regex: ^(\d{4}/\d{2}/\d{2}), (\d{1,2}:\d{2}).?([ap]m) - (.*?): (.*)
        // Line 1: "2024/07/26, 7:41 pm - Messages..." - Does NOT have ": " after the name part.
        // It has "- Messages and calls..."
        // Wait, the regex expects "(.*?): (.*)".
        // "Messages and calls..." does not contain a colon usually, or if it does, the structure " - Name: Message" is key.
        // Line 1: " - Messages ... encrypted." No colon separating name and message.
        // So line 1 should not match the regex and be skipped.

        assertEquals(3, messages.size)

        assertEquals("lefa", messages[0].author)
        assertEquals("<Media omitted>", messages[0].content)

        assertEquals("owami", messages[1].author)
        assertEquals("Hi Stranger.🙂", messages[1].content)

        assertEquals("lefa", messages[2].author)
        assertEquals("Hello human person how are you 😗", messages[2].content)
    }
}
