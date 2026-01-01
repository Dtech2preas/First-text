package com.lefa.thearchive.data

import android.content.Context
import com.google.gson.Gson
import com.lefa.thearchive.model.PersistedData
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object JsonStorage {
    private const val FILENAME = "archive_data.json"
    private val gson = Gson()

    fun saveData(context: Context, data: PersistedData) {
        val file = File(context.filesDir, FILENAME)
        // Use streaming write to handle large data without OOM
        FileWriter(file).use { writer ->
            gson.toJson(data, writer)
        }
    }

    fun loadData(context: Context): PersistedData? {
        val file = File(context.filesDir, FILENAME)
        if (!file.exists()) return null

        return try {
            // Use streaming read
            FileReader(file).use { reader ->
                gson.fromJson(reader, PersistedData::class.java)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearData(context: Context) {
        val file = File(context.filesDir, FILENAME)
        if (file.exists()) file.delete()
    }
}
