package com.example.flashcardapp.presentation.common.dialog.accountDialog

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.annotation.RequiresPermission
import com.example.flashcardapp.data.datasource.local.session.StudyStreakStore

/** Receives alarm events and shows reminder notification when enabled. */
class ReminderReceiver : BroadcastReceiver() {
    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_REMINDER) return

        val studyEnabled = intent.getBooleanExtra(EXTRA_STUDY_ENABLED, false)
        val hour = intent.getIntExtra(EXTRA_HOUR, 8)
        val minute = intent.getIntExtra(EXTRA_MINUTE, 0)

        if (studyEnabled && !StudyStreakStore.hasStudiedToday(context)) {
            ReminderNotificationHelper.showStudyReminder(context)
        }

        ReminderScheduler.schedule(context, hour, minute, enabled = true, studyEnabled = studyEnabled)
    }

    companion object {
        const val ACTION_REMINDER = "com.example.flashcardapp.ACTION_STUDY_REMINDER"
        const val EXTRA_HOUR = "extra_hour"
        const val EXTRA_MINUTE = "extra_minute"
        const val EXTRA_STUDY_ENABLED = "extra_study_enabled"
    }
}
