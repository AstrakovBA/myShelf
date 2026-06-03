package com.myshelf.myshelf_app.data.remote

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.myshelf.myshelf_app.util.Constants

class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_PRIVATE)

    fun saveToken(token: String) {
        prefs.edit {
            putString(Constants.PREF_ACCESS_TOKEN, token)
            putBoolean(Constants.PREF_IS_LOGGED_IN, true)
        }
    }

    fun getToken(): String? {
        return prefs.getString(Constants.PREF_ACCESS_TOKEN, null)
    }

    fun getBearerToken(): String? {
        val token = getToken() ?: return null
        return if (token.startsWith(BEARER_PREFIX)) token else "$BEARER_PREFIX$token"
    }

    fun saveUserId(userId: String) {
        prefs.edit { putString(Constants.PREF_USER_ID, userId) }
    }

    fun getUserId(): String? {
        return prefs.getString(Constants.PREF_USER_ID, null)
    }

    fun saveUserEmail(email: String) {
        prefs.edit { putString(Constants.PREF_USER_EMAIL, email) }
    }

    fun getUserEmail(): String? {
        return prefs.getString(Constants.PREF_USER_EMAIL, null)
    }

    fun saveAuthSession(token: String, userId: String, email: String?) {
        prefs.edit {
            putString(Constants.PREF_ACCESS_TOKEN, token)
            putString(Constants.PREF_USER_ID, userId)
            email?.let { putString(Constants.PREF_USER_EMAIL, it) }
            putBoolean(Constants.PREF_IS_LOGGED_IN, true)
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(Constants.PREF_IS_LOGGED_IN, false) && !getToken().isNullOrBlank()
    }

    fun clearSession() {
        prefs.edit {
            remove(Constants.PREF_ACCESS_TOKEN)
            remove(Constants.PREF_REFRESH_TOKEN)
            remove(Constants.PREF_USER_ID)
            remove(Constants.PREF_USER_EMAIL)
            putBoolean(Constants.PREF_IS_LOGGED_IN, false)
        }
    }

    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }
}
