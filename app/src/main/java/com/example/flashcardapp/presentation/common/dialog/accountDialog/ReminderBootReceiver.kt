package com.example.flashcardapp.presentation.common.dialog.accountDialog

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Re-schedules reminder alarms after reboot or app update. */
class ReminderBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_MY_PACKAGE_REPLACED) {
            ReminderScheduler.restoreFromSavedSettings(context)
        }
    }
}
