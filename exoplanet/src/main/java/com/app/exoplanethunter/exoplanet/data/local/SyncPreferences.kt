package com.app.exoplanethunter.exoplanet.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sync_prefs")

class SyncPreferences(private val context: Context) {
    
    companion object {
        val LAST_SYNC_TIME = longPreferencesKey("last_sync_time")
        val IS_INITIAL_ASSET_COPIED = booleanPreferencesKey("is_initial_asset_copied")
    }

    val lastSyncTime: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_SYNC_TIME] ?: 0L
        }

    val isInitialAssetCopied: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_INITIAL_ASSET_COPIED] ?: false
        }

    suspend fun saveLastSyncTime(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_SYNC_TIME] = timestamp
        }
    }

    suspend fun setInitialAssetCopied(copied: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_INITIAL_ASSET_COPIED] = copied
        }
    }
}
