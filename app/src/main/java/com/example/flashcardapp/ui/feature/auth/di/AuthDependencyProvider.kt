package com.example.flashcardapp.ui.feature.auth.di

import android.content.Context
import com.example.flashcardapp.AppSessionManager
import com.example.flashcardapp.network.RetrofitClient
import com.example.flashcardapp.ui.feature.auth.data.AuthRemoteDataSourceImpl
import com.example.flashcardapp.ui.feature.auth.data.AuthRepository
import com.example.flashcardapp.ui.feature.auth.data.AuthRepositoryImpl
import com.example.flashcardapp.ui.feature.auth.data.AuthSessionStoreImpl
import com.example.flashcardapp.ui.feature.auth.presentation.AuthViewModelFactory

object AuthDependencyProvider {

    fun provideViewModelFactory(context: Context): AuthViewModelFactory {
        return AuthViewModelFactory(provideRepository(context))
    }

    private fun provideRepository(context: Context): AuthRepository {
        val appContext = context.applicationContext
        return AuthRepositoryImpl(
            remoteDataSource = AuthRemoteDataSourceImpl(RetrofitClient.authApiService),
            sessionStore = AuthSessionStoreImpl(AppSessionManager(appContext))
        )
    }
}
