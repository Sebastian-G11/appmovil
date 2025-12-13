package com.example.firebase_testapp

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

// DataStore GLOBAL y único
val Context.sessionDataStore by preferencesDataStore(
    name = "session"
)
