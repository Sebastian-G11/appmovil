package com.example.firebase_testapp.loginLogic

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit

import com.example.firebase_testapp.sessionDataStore
import kotlinx.coroutines.flow.first

class SessionManager(private val context: Context) {

    private val LOGGED_IN = booleanPreferencesKey("logged_in")

    suspend fun saveLoginState(isLoggedIn: Boolean) {
        context.sessionDataStore.edit {
            it[LOGGED_IN] = isLoggedIn
        }
    }

    suspend fun isLoggedIn(): Boolean {
        val prefs = context.sessionDataStore.data.first()
        return prefs[LOGGED_IN] ?: false
    }
}
