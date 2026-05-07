package com.example.flashcardapp.data.repository

import com.example.flashcardapp.data.datasource.remote.api.CategoriesApiService
import com.example.flashcardapp.domain.model.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CategoryRepository(
    private val categoriesApiService: CategoriesApiService
) {
    suspend fun getAllCategories(): Result<List<Category>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = categoriesApiService.getAllCategories()
                if (response.isSuccess() && response.data != null) {
                    Result.success(response.data.map { it.toDomain() })
                } else {
                    Result.failure(Exception(response.message ?: "Failed to load categories"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}

