package com.example.flashcardapp.presentation.feature.addDeck

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.model.Category
import com.example.flashcardapp.domain.model.Deck
import com.example.flashcardapp.domain.usecase.deck.AddDeckUseCase
import com.example.flashcardapp.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AddDeckState {
    object Idle : AddDeckState()
    object Loading : AddDeckState()
    data class Success(val deck: Deck) : AddDeckState()
    data class Error(val message: String) : AddDeckState()
}

class AddDeckViewModel(
    private val addDeckUseCase: AddDeckUseCase,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AddDeckState>(AddDeckState.Idle)
    val uiState: StateFlow<AddDeckState> = _uiState.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val result = categoryRepository.getAllCategories()
            if (result.isSuccess) {
                val cats = result.getOrDefault(emptyList())
                _categories.value = cats
                if (cats.isNotEmpty()) {
                    _selectedCategoryId.value = cats.first().id
                }
            }
        }
    }

    fun selectCategory(id: String) {
        _selectedCategoryId.value = id
    }

    fun resetState() {
        _uiState.value = AddDeckState.Idle
    }

    fun createDeck(name: String, description: String, isPublic: Boolean) {
        if (name.isBlank()) {
            _uiState.value = AddDeckState.Error("Tên bộ thẻ không được để trống")
            return
        }
        val categoryId = _selectedCategoryId.value
        if (categoryId.isNullOrBlank()) {
            _uiState.value = AddDeckState.Error("Vui lòng chọn chủ đề")
            return
        }

        viewModelScope.launch {
            _uiState.value = AddDeckState.Loading

            val result = addDeckUseCase(name, description, isPublic, categoryId)

            result.fold(
                onSuccess = { deck ->
                    _uiState.value = AddDeckState.Success(deck)
                },
                onFailure = { error ->
                    _uiState.value = AddDeckState.Error(error.message ?: "Có lỗi xảy ra khi tạo bộ thẻ")
                }
            )
        }
    }
}
