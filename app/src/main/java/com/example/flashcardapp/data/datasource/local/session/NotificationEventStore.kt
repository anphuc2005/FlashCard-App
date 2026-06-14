package com.example.flashcardapp.data.datasource.local.session

import android.content.Context

object NotificationEventStore {
    private const val PREFS_NAME = "notification_event_prefs"
    private const val KEY_DECKS_INITIALIZED = "decks_initialized"
    private const val KEY_ACHIEVEMENTS_INITIALIZED = "achievements_initialized"
    private const val KEY_KNOWN_DECK_IDS = "known_deck_ids"
    private const val KEY_UNLOCKED_ACHIEVEMENT_CODES = "unlocked_achievement_codes"

    fun updateKnownDeckIds(context: Context, currentDeckIds: Set<String>): Set<String> {
        val prefs = prefs(context)
        val isInitialized = prefs.getBoolean(KEY_DECKS_INITIALIZED, false)
        val knownIds = prefs.getStringSet(KEY_KNOWN_DECK_IDS, emptySet()).orEmpty()
        prefs.edit()
            .putBoolean(KEY_DECKS_INITIALIZED, true)
            .putStringSet(KEY_KNOWN_DECK_IDS, currentDeckIds)
            .apply()
        return if (isInitialized) currentDeckIds - knownIds else emptySet()
    }

    fun updateUnlockedAchievements(
        context: Context,
        unlockedCodes: Set<String>
    ): Set<String> {
        val prefs = prefs(context)
        val isInitialized = prefs.getBoolean(KEY_ACHIEVEMENTS_INITIALIZED, false)
        val knownCodes = prefs.getStringSet(KEY_UNLOCKED_ACHIEVEMENT_CODES, emptySet()).orEmpty()
        prefs.edit()
            .putBoolean(KEY_ACHIEVEMENTS_INITIALIZED, true)
            .putStringSet(KEY_UNLOCKED_ACHIEVEMENT_CODES, unlockedCodes)
            .apply()
        return if (isInitialized) unlockedCodes - knownCodes else emptySet()
    }

    fun clear(context: Context) {
        prefs(context).edit().clear().apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
