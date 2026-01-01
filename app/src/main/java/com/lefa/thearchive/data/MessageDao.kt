package com.lefa.thearchive.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import java.util.Date

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages ORDER BY timestamp ASC")
    fun getAllMessages(): List<MessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(messages: List<MessageEntity>)

    @Query("SELECT COUNT(*) FROM messages")
    fun getMessageCount(): Int

    @Query("DELETE FROM messages")
    fun clearAll()

    // Search Query: Find messages containing the text (case-insensitive)
    @Query("SELECT * FROM messages WHERE content LIKE '%' || :query || '%' ORDER BY timestamp ASC")
    fun searchMessages(query: String): List<MessageEntity>

    // Find the first message by a specific author containing the text
    @Query("SELECT * FROM messages WHERE author = :author AND content LIKE '%' || :query || '%' ORDER BY timestamp ASC LIMIT 1")
    fun findFirstMessageByAuthor(author: String, query: String): MessageEntity?

    @Query("SELECT * FROM messages ORDER BY timestamp ASC LIMIT 1")
    fun getFirstMessageEver(): MessageEntity?

    // Get count of messages containing query for a specific author
    @Query("SELECT COUNT(*) FROM messages WHERE author = :author AND content LIKE '%' || :query || '%'")
    fun countMessagesByAuthor(author: String, query: String): Int
}
