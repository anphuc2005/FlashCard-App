package com.example.flashcardapp

import android.app.Application
import com.example.flashcardapp.di.AppContainer
import com.example.flashcardapp.presentation.common.dialog.accountDialog.ReminderScheduler

class FlashcardApp : Application() {

    // Đây là container chứa toàn bộ các dependency dùng chung (Singleton)
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ReminderScheduler.restoreFromSavedSettings(this)
    }
}
