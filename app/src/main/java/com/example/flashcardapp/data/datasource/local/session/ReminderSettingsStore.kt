package com.example.flashcardapp.data.datasource.local.session

import android.content.Context

object ReminderSettingsStore {
    private const val PREFS_NAME = "reminder_settings_prefs"
    private const val KEY_REMINDER_HOUR = "reminder_hour"
    private const val KEY_REMINDER_MINUTE = "reminder_minute"
    private const val KEY_REMINDER_ENABLED = "reminder_enabled"
    private const val KEY_NOTIF_STUDY = "notif_study"
    private const val KEY_NOTIF_NEW_DECK = "notif_new_deck"
    private const val KEY_NOTIF_ACHIEVEMENT = "notif_achievement"

    data class ReminderSettings(
        val hour: Int = 8,
        val minute: Int = 0,
        val enabled: Boolean = true
    )

    data class NotificationSettings(
        val study: Boolean = true,
        val newDeck: Boolean = false,
        val achievement: Boolean = true
    )

    fun getReminderSettings(context: Context): ReminderSettings {
        val prefs = prefs(context)
        return ReminderSettings(
            hour = prefs.getInt(KEY_REMINDER_HOUR, 8).coerceIn(0, 23),
            minute = prefs.getInt(KEY_REMINDER_MINUTE, 0).coerceIn(0, 59),
            enabled = prefs.getBoolean(KEY_REMINDER_ENABLED, true)
        )
    }

    fun getNotificationSettings(context: Context): NotificationSettings {
        val prefs = prefs(context)
        return NotificationSettings(
            study = prefs.getBoolean(KEY_NOTIF_STUDY, true),
            newDeck = prefs.getBoolean(KEY_NOTIF_NEW_DECK, false),
            achievement = prefs.getBoolean(KEY_NOTIF_ACHIEVEMENT, true)
        )
    }

    fun saveReminderSettings(context: Context, hour: Int, minute: Int, enabled: Boolean) {
        prefs(context).edit()
            .putInt(KEY_REMINDER_HOUR, hour.coerceIn(0, 23))
            .putInt(KEY_REMINDER_MINUTE, minute.coerceIn(0, 59))
            .putBoolean(KEY_REMINDER_ENABLED, enabled)
            .apply()
    }

    fun saveStudyNotificationEnabled(context: Context, studyEnabled: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_NOTIF_STUDY, studyEnabled)
            .apply()
    }

    fun saveNotificationSettings(
        context: Context,
        study: Boolean,
        newDeck: Boolean,
        achievement: Boolean
    ) {
        prefs(context).edit()
            .putBoolean(KEY_NOTIF_STUDY, study)
            .putBoolean(KEY_NOTIF_NEW_DECK, newDeck)
            .putBoolean(KEY_NOTIF_ACHIEVEMENT, achievement)
            .apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
