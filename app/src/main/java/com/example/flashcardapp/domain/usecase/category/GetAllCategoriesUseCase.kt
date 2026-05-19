package com.example.flashcardapp.domain.usecase.category

import com.example.flashcardapp.data.repository.CategoryRepository
import com.example.flashcardapp.domain.model.Category

class GetAllCategoriesUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke(): Result<List<Category>> {
        return categoryRepository.getAllCategories()
    }
}

