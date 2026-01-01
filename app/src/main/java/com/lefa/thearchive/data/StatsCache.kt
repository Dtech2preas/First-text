package com.lefa.thearchive.data

import android.content.Context
import com.google.gson.Gson
import com.lefa.thearchive.model.Stats
import java.io.File

object StatsCache {
    private const val FILENAME = "stats_cache.json"
    private val gson = Gson()

    fun saveStats(context: Context, stats: Stats) {
        val file = File(context.filesDir, FILENAME)
        val json = gson.toJson(stats)
        file.writeText(json)
    }

    fun loadStats(context: Context): Stats? {
        val file = File(context.filesDir, FILENAME)
        if (!file.exists()) return null
        return try {
            val json = file.readText()
            gson.fromJson(json, Stats::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearCache(context: Context) {
        val file = File(context.filesDir, FILENAME)
        if (file.exists()) file.delete()
    }
}
