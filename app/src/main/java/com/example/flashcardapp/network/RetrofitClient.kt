package com.example.flashcardapp.network

import com.example.flashcardapp.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApiService: AuthApiService = retrofit.create(AuthApiService::class.java)
    val deckApiService: DeckApiService = retrofit.create(DeckApiService::class.java)
    val chatbotApiService: ChatbotApiService = retrofit.create(ChatbotApiService::class.java)
}
