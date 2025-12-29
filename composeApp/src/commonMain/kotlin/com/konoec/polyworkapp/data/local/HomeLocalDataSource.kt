package com.konoec.polyworkapp.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first

class HomeLocalDataSource(
    private val dataStore: DataStore<Preferences>
) {

    private val SHIFT_ID_KEY = stringPreferencesKey("shift_id")
    private val SHIFT_STATUS_KEY = stringPreferencesKey("shift_status")
    private val SHIFT_START_KEY = stringPreferencesKey("shift_start")
    private val SHIFT_END_KEY = stringPreferencesKey("shift_end")
    private val SHIFT_NEXT_KEY = stringPreferencesKey("shift_next")
    private val SHIFT_CACHE_TIME_KEY = longPreferencesKey("shift_cache_time")

    private val STATS_JSON_KEY = stringPreferencesKey("stats_json")
    private val STATS_CACHE_TIME_KEY = longPreferencesKey("stats_cache_time")

    // Cache válido por 30 minutos (en milisegundos)
    private val CACHE_VALIDITY_MS = 30 * 60 * 1000L

    private fun getCurrentTimeMillis(): Long {
        return kotlin.time.Clock.System.now().toEpochMilliseconds()
    }

    // --- SHIFT CACHE ---
    suspend fun saveShiftCache(
        id: String,
        status: String,
        scheduledStartTime: String,
        scheduledEndTime: String,
        nextShiftTime: String?
    ) {
        dataStore.edit { preferences ->
            preferences[SHIFT_ID_KEY] = id
            preferences[SHIFT_STATUS_KEY] = status
            preferences[SHIFT_START_KEY] = scheduledStartTime
            preferences[SHIFT_END_KEY] = scheduledEndTime
            nextShiftTime?.let { preferences[SHIFT_NEXT_KEY] = it }
            preferences[SHIFT_CACHE_TIME_KEY] = getCurrentTimeMillis()
        }
    }

    suspend fun getCachedShift(): CachedShift? {
        val preferences = dataStore.data.first()
        val cacheTime = preferences[SHIFT_CACHE_TIME_KEY] ?: return null

        // Verificar si el cache sigue siendo válido
        if (getCurrentTimeMillis() - cacheTime > CACHE_VALIDITY_MS) {
            return null
        }

        val id = preferences[SHIFT_ID_KEY] ?: return null
        val status = preferences[SHIFT_STATUS_KEY] ?: return null
        val start = preferences[SHIFT_START_KEY] ?: return null
        val end = preferences[SHIFT_END_KEY] ?: return null
        val next = preferences[SHIFT_NEXT_KEY]

        return CachedShift(id, status, start, end, next)
    }

    suspend fun clearShiftCache() {
        dataStore.edit { preferences ->
            preferences.remove(SHIFT_ID_KEY)
            preferences.remove(SHIFT_STATUS_KEY)
            preferences.remove(SHIFT_START_KEY)
            preferences.remove(SHIFT_END_KEY)
            preferences.remove(SHIFT_NEXT_KEY)
            preferences.remove(SHIFT_CACHE_TIME_KEY)
        }
    }

    // --- STATS CACHE ---
    suspend fun saveStatsCache(statsJson: String) {
        dataStore.edit { preferences ->
            preferences[STATS_JSON_KEY] = statsJson
            preferences[STATS_CACHE_TIME_KEY] = getCurrentTimeMillis()
        }
    }

    suspend fun getCachedStats(): String? {
        val preferences = dataStore.data.first()
        val cacheTime = preferences[STATS_CACHE_TIME_KEY] ?: return null

        // Verificar si el cache sigue siendo válido
        if (getCurrentTimeMillis() - cacheTime > CACHE_VALIDITY_MS) {
            return null
        }

        return preferences[STATS_JSON_KEY]
    }

    suspend fun clearStatsCache() {
        dataStore.edit { preferences ->
            preferences.remove(STATS_JSON_KEY)
            preferences.remove(STATS_CACHE_TIME_KEY)
        }
    }

    suspend fun clearAllHomeCache() {
        clearShiftCache()
        clearStatsCache()
    }
}

data class CachedShift(
    val id: String,
    val status: String,
    val scheduledStartTime: String,
    val scheduledEndTime: String,
    val nextShiftTime: String?
)

