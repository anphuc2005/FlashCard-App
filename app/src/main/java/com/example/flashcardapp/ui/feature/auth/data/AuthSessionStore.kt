package com.example.flashcardapp.ui.feature.auth.data

interface AuthSessionStore {
    fun saveLoginSession(accessToken: String?)
    fun clearLoginSession()
}
