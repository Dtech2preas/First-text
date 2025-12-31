package com.lefa.thearchive.utils

import android.content.Context
import android.content.SharedPreferences

object PrefsManager {
    private const val PREFS_NAME = "the_archive_prefs"
    private const val KEY_GAME_WON = "game_won_15_points"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun setGameWon(context: Context, won: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_GAME_WON, won).apply()
    }

    fun isGameWon(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_GAME_WON, false)
    }
}
