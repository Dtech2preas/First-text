package com.lefa.thearchive.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.util.Date

@Entity(tableName = "messages")
@TypeConverters(DateConverter::class)
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fullDate: String,
    val time: String,
    val timestamp: Date,
    val author: String,
    val content: String,
    val originalSender: String
)

class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}
