package com.example.flashcardapp.presentation.common.dialog.accountDialog

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.flashcardapp.data.datasource.local.session.ReminderSettingsStore
import java.util.Calendar

/** Schedules/cancels the daily reminder alarm. */
object ReminderScheduler {
    private const val REQUEST_CODE_REMINDER = 10001

    @SuppressLint("ScheduleExactAlarm")
    fun schedule(context: Context, hour: Int, minute: Int, enabled: Boolean, studyEnabled: Boolean) {
        val appContext = context.applicationContext
        ReminderSettingsStore.saveReminderSettings(appContext, hour, minute, enabled)
        ReminderSettingsStore.saveStudyNotificationEnabled(appContext, studyEnabled)

        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(appContext, hour, minute, studyEnabled)

        alarmManager.cancel(pendingIntent)

        if (!enabled || !studyEnabled) return

        val triggerAt = nextTriggerMillis(hour, minute)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
            return
        }

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    }

    fun restoreFromSavedSettings(context: Context) {
        val appContext = context.applicationContext
        val reminder = ReminderSettingsStore.getReminderSettings(appContext)
        val notification = ReminderSettingsStore.getNotificationSettings(appContext)
        schedule(
            context = appContext,
            hour = reminder.hour,
            minute = reminder.minute,
            enabled = reminder.enabled,
            studyEnabled = notification.study
        )
    }

    private fun buildPendingIntent(context: Context, hour: Int, minute: Int, studyEnabled: Boolean): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            action = ReminderReceiver.ACTION_REMINDER
            putExtra(ReminderReceiver.EXTRA_HOUR, hour)
            putExtra(ReminderReceiver.EXTRA_MINUTE, minute)
            putExtra(ReminderReceiver.EXTRA_STUDY_ENABLED, studyEnabled)
        }
        val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        return PendingIntent.getBroadcast(context, REQUEST_CODE_REMINDER, intent, flags)
    }

    private fun nextTriggerMillis(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_YEAR, 1)
        }
        return trigger.timeInMillis
    }
}
