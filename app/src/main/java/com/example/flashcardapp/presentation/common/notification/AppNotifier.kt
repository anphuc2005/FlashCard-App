package com.example.flashcardapp.presentation.common.notification

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.ViewAppNotificationBinding
import java.util.Locale

enum class AppNotificationType {
    SUCCESS,
    ERROR,
    WARNING
}

fun Fragment.showAppSuccess(message: String, title: String? = null) {
    requireContext().showAppNotification(AppNotificationType.SUCCESS, message, title)
}

fun Fragment.showAppError(message: String?, title: String? = null) {
    requireContext().showAppNotification(AppNotificationType.ERROR, message, title)
}

fun Fragment.showAppWarning(message: String?, title: String? = null) {
    requireContext().showAppNotification(AppNotificationType.WARNING, message, title)
}

fun Context.showAppSuccess(message: String, title: String? = null) {
    showAppNotification(AppNotificationType.SUCCESS, message, title)
}

fun Context.showAppError(message: String?, title: String? = null) {
    showAppNotification(AppNotificationType.ERROR, message, title)
}

fun Context.showAppWarning(message: String?, title: String? = null) {
    showAppNotification(AppNotificationType.WARNING, message, title)
}

private fun Context.showAppNotification(
    type: AppNotificationType,
    message: String?,
    title: String?
) {
    val binding = ViewAppNotificationBinding.inflate(LayoutInflater.from(this))
    binding.textTitle.text = title ?: defaultTitle(type)
    binding.textMessage.text = sanitizeNotificationMessage(message, type)
    binding.iconStatus.setImageResource(iconRes(type))
    binding.iconContainer.backgroundTintList = ContextCompat.getColorStateList(this, iconBackgroundRes(type))

    Toast(applicationContext).apply {
        duration = Toast.LENGTH_SHORT
        view = binding.root
        setGravity(Gravity.TOP or Gravity.CENTER_HORIZONTAL, 0, resources.getDimensionPixelSize(R.dimen.app_notification_margin_top))
    }.show()
}

private fun Context.defaultTitle(type: AppNotificationType): String {
    return when (type) {
        AppNotificationType.SUCCESS -> getString(R.string.app_notification_success_title)
        AppNotificationType.ERROR -> getString(R.string.app_notification_error_title)
        AppNotificationType.WARNING -> getString(R.string.app_notification_warning_title)
    }
}

private fun Context.defaultMessage(type: AppNotificationType): String {
    return when (type) {
        AppNotificationType.SUCCESS -> getString(R.string.app_notification_success_default)
        AppNotificationType.ERROR -> getString(R.string.app_notification_error_default)
        AppNotificationType.WARNING -> getString(R.string.app_notification_warning_default)
    }
}

private fun Context.iconRes(type: AppNotificationType): Int {
    return when (type) {
        AppNotificationType.SUCCESS -> R.drawable.ic_check_circle
        AppNotificationType.ERROR -> R.drawable.ic_app_notification_error
        AppNotificationType.WARNING -> R.drawable.ic_app_notification_warning
    }
}

private fun Context.iconBackgroundRes(type: AppNotificationType): Int {
    return when (type) {
        AppNotificationType.SUCCESS -> R.color.app_notification_success
        AppNotificationType.ERROR -> R.color.app_notification_error
        AppNotificationType.WARNING -> R.color.app_notification_warning
    }
}

private fun Context.sanitizeNotificationMessage(rawMessage: String?, type: AppNotificationType): String {
    val message = rawMessage.orEmpty().trim().replace(Regex("\\s+"), " ")
    if (message.isBlank()) return defaultMessage(type)

    val normalized = message.lowercase(Locale.getDefault())
    val cleaned = message
        .removePrefix("Error:")
        .removePrefix("error:")
        .removePrefix("Lỗi:")
        .trim()

    return when {
        normalized.contains("401") || normalized.contains("unauthorized") ->
            getString(R.string.app_notification_invalid_credentials)
        normalized.contains("403") || normalized.contains("forbidden") ->
            getString(R.string.app_notification_permission_denied)
        normalized.contains("404") || normalized.contains("not found") ->
            getString(R.string.app_notification_data_not_found)
        normalized.contains("timeout") ||
            normalized.contains("failed to connect") ||
            normalized.contains("unable to resolve host") ||
            normalized.contains("network") ||
            normalized.contains("socket") ||
            normalized.contains("connection") ->
            getString(R.string.app_notification_server_unreachable)
        normalized.contains("500") ||
            normalized.contains("server error") ||
            normalized.contains("internal server") ||
            normalized.contains("retrofit") ||
            normalized.contains("http") ->
            getString(R.string.app_notification_system_busy)
        normalized.contains("exception") ||
            normalized.contains("java.") ||
            normalized.contains("kotlin.") ||
            normalized.contains("nullpointer") ||
            normalized.contains("illegalstate") ||
            normalized.contains("stacktrace") ||
            normalized.contains("trace") ||
            normalized.contains("unexpected") ->
            defaultMessage(type)
        cleaned.isBlank() -> defaultMessage(type)
        else -> cleaned
    }
}
