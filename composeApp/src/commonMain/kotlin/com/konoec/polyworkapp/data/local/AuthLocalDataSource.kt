package com.konoec.polyworkapp.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AuthLocalDataSource(
    private val dataStore: DataStore<Preferences>
) {

    private val TOKEN_KEY = stringPreferencesKey("auth_token")
    private val USER_ID_KEY = stringPreferencesKey("user_id")
    private val USER_DNI_KEY = stringPreferencesKey("user_dni")
    private val USER_NAME_KEY = stringPreferencesKey("user_name")

    suspend fun saveAuthData(token: String, userId: String, dni: String, name: String) {
        dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
            preferences[USER_DNI_KEY] = dni
            preferences[USER_NAME_KEY] = name
        }
    }

    fun getToken(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    fun getUserId(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }

    fun getUserDni(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_DNI_KEY]
    }

    fun getUserName(): Flow<String?> = dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }

    suspend fun clearAuthData() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}

