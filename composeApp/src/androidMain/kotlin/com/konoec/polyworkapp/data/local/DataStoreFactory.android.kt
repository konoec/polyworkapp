package com.konoec.polyworkapp.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.core.DataStore

lateinit var appContext: Context
    actual fun createDataStorePreferences(): DataStore<Preferences> {

        return createDataStore {
            appContext.filesDir.resolve(dataStoreFileName).absolutePath
        }
    }







