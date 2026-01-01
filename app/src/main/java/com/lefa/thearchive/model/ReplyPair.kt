package com.lefa.thearchive.model

import java.io.Serializable

data class ReplyPair(val original: List<Message>, val reply: Message) : Serializable
