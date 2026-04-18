package com.example.flashcardapp.data.datasource.remote.api

import com.example.flashcardapp.core.constants.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    var tokenProvider: (() -> String?)? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        val requestBuilder = chain.request().newBuilder()
        tokenProvider?.invoke()?.let { token ->
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }
        chain.proceed(requestBuilder.build())
    }

    private val defaultClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
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
    val cardApiService: CardApiService = retrofit.create(CardApiService::class.java)
    val categoriesApiService: CategoriesApiService = retrofit.create(CategoriesApiService::class.java)
    val groqApiService: GroqApiService = groqRetrofit.create(GroqApiService::class.java)
}
