package com.lefa.thearchive.model

import java.io.Serializable
import java.util.Date

data class Message(
    val fullDate: String,
    val time: String,
    val timestamp: Date,
    val author: String,
    val content: String,
    val originalSender: String
) : Serializable
