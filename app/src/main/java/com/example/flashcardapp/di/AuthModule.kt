package com.example.flashcardapp.di

import android.content.Context
import com.example.flashcardapp.AppSessionManager
import com.example.flashcardapp.data.datasource.local.session.AuthSessionStoreImpl
import com.example.flashcardapp.data.datasource.remote.auth.AuthRemoteDataSourceImpl
import com.example.flashcardapp.data.datasource.remote.api.RetrofitClient
import com.example.flashcardapp.data.repository.AuthRepositoryImpl
import com.example.flashcardapp.domain.usecase.auth.AuthUseCases
import com.example.flashcardapp.domain.usecase.auth.ForgotPasswordUseCase
import com.example.flashcardapp.domain.usecase.auth.LoginUseCase
import com.example.flashcardapp.domain.usecase.auth.RegisterUseCase

object AuthModule {
    
    fun provideAuthUseCases(context: Context): AuthUseCases {
        val sessionManager = AppSessionManager(context)
        val sessionStore = AuthSessionStoreImpl(sessionManager)
        val remoteDataSource = AuthRemoteDataSourceImpl(RetrofitClient.authApiService)
        val repository = AuthRepositoryImpl(remoteDataSource, sessionStore)
        
        return AuthUseCases(
            login = LoginUseCase(repository),
            register = RegisterUseCase(repository),
            forgotPassword = ForgotPasswordUseCase(repository)
        )
    }
}

