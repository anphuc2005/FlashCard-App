package com.example.flashcardapp.data.datasource.remote.api

import com.example.flashcardapp.core.constants.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val defaultClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(defaultClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val groqRetrofit = Retrofit.Builder()
        .baseUrl(Constants.GROQ_BASE_URL)
        .client(defaultClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApiService: AuthApiService = retrofit.create(AuthApiService::class.java)
    val deckApiService: DeckApiService = retrofit.create(DeckApiService::class.java)
    val groqApiService: GroqApiService = groqRetrofit.create(GroqApiService::class.java)
}

