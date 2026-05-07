package com.example.flashcardapp.data.datasource.remote.api

import com.example.flashcardapp.data.datasource.remote.model.ApiResponse
import com.example.flashcardapp.data.datasource.remote.model.CategoryDto
import retrofit2.http.GET
import retrofit2.http.Path

interface CategoriesApiService {
    @GET("categories")
    suspend fun getAllCategories(): ApiResponse<List<CategoryDto>>

    @GET("categories/{id}")
    suspend fun getCategoryById(@Path("id") id: String): ApiResponse<CategoryDto>

}