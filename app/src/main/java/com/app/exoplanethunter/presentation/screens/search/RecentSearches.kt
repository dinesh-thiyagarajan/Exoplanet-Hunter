package com.app.exoplanethunter.presentation.screens.search

import android.content.Context

/** The last few searches, newest first, kept on the device. */
class RecentSearches(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun load(): List<String> =
        prefs.getString(KEY, null)?.split(SEPARATOR)?.filter { it.isNotBlank() }.orEmpty()

    /** Moves [query] to the front, dropping duplicates (any case) and anything past [MAX]. */
    fun add(query: String): List<String> {
        val q = query.trim()
        if (q.isEmpty()) return load()
        val updated = (listOf(q) + load().filterNot { it.equals(q, ignoreCase = true) }).take(MAX)
        prefs.edit().putString(KEY, updated.joinToString(SEPARATOR)).apply()
        return updated
    }

    fun clear() {
        prefs.edit().remove(KEY).apply()
    }

    companion object {
        const val MAX = 6
        private const val PREFS_NAME = "search_prefs"
        private const val KEY = "recent"
        private const val SEPARATOR = "\u001F"
    }
}
