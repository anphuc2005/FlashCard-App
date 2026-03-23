package com.example.flashcardapp.network

import android.content.Context
import com.example.flashcardapp.AppSessionManager
import com.example.flashcardapp.utils.Constants
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

// RetrofitClient tạo service dùng chung và tự gắn token vào request.
object RetrofitClient {

    private lateinit var appContext: Context

    fun initialize(context: Context) {
        appContext = context.applicationContext
    }

    private val defaultClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            // Tự gắn access token mới nhất trước khi gọi API.
            .addInterceptor(AuthTokenInterceptor(::provideAccessToken))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(defaultClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val geminiRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(defaultClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApiService: AuthApiService by lazy { retrofit.create(AuthApiService::class.java) }
    val deckApiService: DeckApiService by lazy { retrofit.create(DeckApiService::class.java) }
    val geminiApiService: GeminiApiService by lazy { geminiRetrofit.create(GeminiApiService::class.java) }

    private fun provideAccessToken(): String? {
        check(::appContext.isInitialized) {
            "RetrofitClient must be initialized from Application before use."
        }
        // Luôn đọc token mới nhất từ local trước khi gửi request.
        return AppSessionManager(appContext).accessToken
    }
}
