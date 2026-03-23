package com.example.flashcardapp.ui.feature.auth.data

import com.example.flashcardapp.AppSessionManager

class AuthSessionStoreImpl(
    private val sessionManager: AppSessionManager
) : AuthSessionStore {

    override fun saveLoginSession(accessToken: String?, refreshToken: String?) {
        sessionManager.saveLoginSession(accessToken, refreshToken)
    }

    override fun clearLoginSession() {
        sessionManager.clearLoginSession()
    }
}
