package com.example.flashcardapp.data.datasource.local.session

interface AuthSessionStore {
    fun saveLoginSession(accessToken: String?)
    fun clearLoginSession()
}

