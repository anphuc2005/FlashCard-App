package com.example.flashcardapp.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://your-api-domain.com/api/" // Thay đổi URL của bạn

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val deckApiService: DeckApiService = retrofit.create(DeckApiService::class.java)
    val chatbotApiService: ChatbotApiService = retrofit.create(ChatbotApiService::class.java)
}

