package com.app.exoplanethunter.update

import android.content.Context

/** Remembers when the user last turned down an update, so they aren't asked on every launch. */
class UpdatePreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Epoch millis of the last declined update prompt (0 if never). */
    var lastDeclinedMillis: Long
        get() = prefs.getLong(KEY_LAST_DECLINED, 0L)
        set(value) = prefs.edit().putLong(KEY_LAST_DECLINED, value).apply()

    companion object {
        private const val PREFS_NAME = "update_prefs"
        private const val KEY_LAST_DECLINED = "last_declined_millis"
    }
}
