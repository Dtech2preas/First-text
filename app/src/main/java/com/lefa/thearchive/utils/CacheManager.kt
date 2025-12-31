package com.lefa.thearchive.utils

import android.content.Context
import com.lefa.thearchive.model.Stats
import java.io.File
import java.io.ObjectInputStream
import java.io.ObjectOutputStream

object CacheManager {
    private const val CACHE_FILE = "stats_cache.ser"

    fun saveStats(context: Context, stats: Stats) {
        try {
            val file = File(context.filesDir, CACHE_FILE)
            ObjectOutputStream(file.outputStream()).use {
                it.writeObject(stats)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadStats(context: Context): Stats? {
        val file = File(context.filesDir, CACHE_FILE)
        if (!file.exists()) return null

        return try {
            ObjectInputStream(file.inputStream()).use {
                it.readObject() as? Stats
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun clearCache(context: Context) {
        try {
            val file = File(context.filesDir, CACHE_FILE)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
