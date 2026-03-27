package com.example.flashcardapp.data.datasource.local.session

import com.example.flashcardapp.AppSessionManager

class AuthSessionStoreImpl(
    private val sessionManager: AppSessionManager
) : AuthSessionStore {

    override fun saveLoginSession(accessToken: String?) {
        sessionManager.saveLoginSession(accessToken)
    }

    override fun clearLoginSession() {
        sessionManager.clearLoginSession()
    }
}

