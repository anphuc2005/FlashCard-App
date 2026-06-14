package com.example.flashcardapp.presentation.common.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.flashcardapp.R
import com.example.flashcardapp.presentation.main.MainActivity

class LocalUpdateNotificationHelper(context: Context) {
    private val appContext = context.applicationContext

    fun showNewDecks(deckNames: List<String>) {
        if (deckNames.isEmpty() || !canPostNotifications()) return
        val message = if (deckNames.size == 1) {
            appContext.getString(R.string.notification_new_deck_body_single, deckNames.first())
        } else {
            appContext.getString(R.string.notification_new_deck_body_multiple, deckNames.size)
        }
        show(
            notificationId = NOTIFICATION_ID_NEW_DECK,
            title = appContext.getString(R.string.notification_new_deck_title),
            message = message
        )
    }

    fun showAchievement(title: String) {
        if (!canPostNotifications()) return
        show(
            notificationId = NOTIFICATION_ID_ACHIEVEMENT,
            title = appContext.getString(R.string.notification_achievement_title),
            message = appContext.getString(R.string.notification_achievement_body, title)
        )
    }

    private fun show(notificationId: Int, title: String, message: String) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        ensureChannel()
        val contentIntent = PendingIntent.getActivity(
            appContext,
            notificationId,
            Intent(appContext, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(appContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notif_shortcut)
            .setContentTitle(title)
            .setContentText(message)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        try {
            NotificationManagerCompat.from(appContext).notify(notificationId, notification)
        } catch (_: SecurityException) {
            // Permission can be revoked between the check and notify call.
        }
    }

    private fun canPostNotifications(): Boolean {
        if (!NotificationManagerCompat.from(appContext).areNotificationsEnabled()) return false
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                appContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            appContext.getString(R.string.notification_updates_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = appContext.getString(R.string.notification_updates_channel_description)
        }
        val manager = appContext.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private companion object {
        const val CHANNEL_ID = "learning_updates_channel"
        const val NOTIFICATION_ID_NEW_DECK = 2001
        const val NOTIFICATION_ID_ACHIEVEMENT = 2002
    }
}
