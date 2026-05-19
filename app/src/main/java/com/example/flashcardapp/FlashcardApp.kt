package com.example.flashcardapp

import android.app.Application
import com.example.flashcardapp.di.AppContainer
import com.example.flashcardapp.presentation.common.dialog.accountDialog.ReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FlashcardApp : Application() {

    // Đây là container chứa toàn bộ các dependency dùng chung (Singleton)
    lateinit var container: AppContainer
        private set
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        ReminderScheduler.restoreFromSavedSettings(this)
        applicationScope.launch {
            container.offlineSyncManager.observeAndSync()
        }
    }
}
