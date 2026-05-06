package com.example.flashcardapp.presentation.common.dialog.accountDialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flashcardapp.domain.usecase.notification.GetNotificationsPageUseCase
import com.example.flashcardapp.domain.usecase.notification.MarkNotificationReadUseCase

class AdminNotificationInboxViewModelFactory(
    private val getNotificationsPageUseCase: GetNotificationsPageUseCase,
    private val markNotificationReadUseCase: MarkNotificationReadUseCase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminNotificationInboxViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminNotificationInboxViewModel(
                getNotificationsPageUseCase,
                markNotificationReadUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
