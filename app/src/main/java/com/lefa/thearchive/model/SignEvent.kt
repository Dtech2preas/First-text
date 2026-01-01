package com.lefa.thearchive.model

import java.io.Serializable

data class SignEvent(val date: String, val sender: String, val phrase: String, val message: String) : Serializable
