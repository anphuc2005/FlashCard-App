package com.example.flashcardapp

import android.content.Context

class AppSessionManager(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    val hasOnboarded: Boolean
        get() = prefs.getBoolean(KEY_HAS_ONBOARDED, prefs.getBoolean(LEGACY_KEY_ONBOARDING_COMPLETED, false))

    val isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    val accessToken: String?
        get() = prefs.getString(KEY_ACCESS_TOKEN, null)

    val refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)

    val isAuthExpired: Boolean
        get() = prefs.getBoolean(KEY_AUTH_EXPIRED, false)

    fun markOnboardingCompleted() {
        prefs.edit()
            .putBoolean(KEY_HAS_ONBOARDED, true)
            .putBoolean(LEGACY_KEY_ONBOARDING_COMPLETED, true)
            .apply()
    }

    fun saveLoginSession(accessToken: String?, refreshToken: String? = null) {
        prefs.edit().apply {
            putBoolean(KEY_IS_LOGGED_IN, true)
            putBoolean(KEY_AUTH_EXPIRED, false)
            if (accessToken.isNullOrBlank()) {
                remove(KEY_ACCESS_TOKEN)
            } else {
                putString(KEY_ACCESS_TOKEN, accessToken)
            }
            if (!refreshToken.isNullOrBlank()) {
                putString(KEY_REFRESH_TOKEN, refreshToken)
            }
            apply()
        }
    }

    fun markAuthExpired() {
        prefs.edit()
            .putBoolean(KEY_AUTH_EXPIRED, true)
            .apply()
    }

    fun clearLoginSession() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .putBoolean(KEY_AUTH_EXPIRED, false)
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
    }

    companion object {
        private const val PREFS_NAME = "flashcard_prefs"
        private const val KEY_HAS_ONBOARDED = "has_onboarded"
        private const val LEGACY_KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_AUTH_EXPIRED = "auth_expired"
    }
}
