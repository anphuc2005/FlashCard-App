package com.example.flashcardapp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.flashcardapp.data.datasource.local.session.NotificationEventStore
import com.example.flashcardapp.data.datasource.local.session.ReminderSettingsStore
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NotificationSettingsInstrumentedTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        clearState()
    }

    @After
    fun cleanup() {
        clearState()
    }

    @Test
    fun firstDeckSnapshotIsBaselineAndNewDeckIsReportedOnce() {
        assertTrue(
            NotificationEventStore.updateKnownDeckIds(
                context,
                setOf("deck-1")
            ).isEmpty()
        )
        assertEquals(
            setOf("deck-2"),
            NotificationEventStore.updateKnownDeckIds(
                context,
                setOf("deck-1", "deck-2")
            )
        )
        assertTrue(
            NotificationEventStore.updateKnownDeckIds(
                context,
                setOf("deck-1", "deck-2")
            ).isEmpty()
        )
    }

    @Test
    fun notificationSettingsArePersistedAndReminderDefaultsDisabled() {
        assertFalse(ReminderSettingsStore.getReminderSettings(context).enabled)

        ReminderSettingsStore.saveNotificationSettings(
            context,
            study = true,
            newDeck = true,
            achievement = false
        )

        val settings = ReminderSettingsStore.getNotificationSettings(context)
        assertTrue(settings.study)
        assertTrue(settings.newDeck)
        assertFalse(settings.achievement)
    }

    private fun clearState() {
        NotificationEventStore.clear(context)
        context.getSharedPreferences("reminder_settings_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }
}
