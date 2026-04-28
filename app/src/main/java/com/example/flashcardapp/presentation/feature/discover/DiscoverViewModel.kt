package com.example.flashcardapp.presentation.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.model.Category
import com.example.flashcardapp.domain.model.Deck
import com.example.flashcardapp.domain.usecase.category.GetAllCategoriesUseCase
import com.example.flashcardapp.domain.usecase.deck.CloneDeckUseCase
import com.example.flashcardapp.domain.usecase.deck.GetExploreDecksFromApiUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.Locale

class DiscoverViewModel(
    private val getAllDecksFromApiUseCase: GetExploreDecksFromApiUseCase,
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val cloneDeckUseCase: CloneDeckUseCase
) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _courses = MutableStateFlow<List<Deck>>(emptyList())
    val courses: StateFlow<List<Deck>> = _courses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _cloneSuccess = MutableSharedFlow<String>()
    val cloneSuccess: SharedFlow<String> = _cloneSuccess.asSharedFlow()

    private var allCourses: List<Deck> = emptyList()
    private var selectedCategoryId: String? = null
    private var searchQuery: String = ""

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Fetch categories
                val categoryResult = getAllCategoriesUseCase()
                if (categoryResult.isSuccess) {
                    _categories.value = categoryResult.getOrNull() ?: emptyList()
                } else {
                    _error.value = categoryResult.exceptionOrNull()?.message
                }

                // Fetch courses (explore decks or all decks)
                val deckResult = getAllDecksFromApiUseCase()
                if (deckResult.isSuccess) {
                    val fetchedDecks = deckResult.getOrNull() ?: emptyList()
                    allCourses = fetchedDecks
                    applyFilters()
                } else {
                    _error.value = deckResult.exceptionOrNull()?.message
                }

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterCoursesByCategory(categoryId: String?) {
        selectedCategoryId = categoryId
        applyFilters()
    }

    fun updateSearchQuery(query: String) {
        searchQuery = query
        applyFilters()
    }

    fun cloneDeck(deckId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = cloneDeckUseCase(deckId)
                if (result.isSuccess) {
                    _cloneSuccess.emit("Đã lưu bộ thẻ thành công!")
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Lỗi khi lưu bộ thẻ"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun applyFilters() {
        val categoryFiltered = selectedCategoryId?.let { categoryId ->
            allCourses.filter { it.categoryId == categoryId }
        } ?: allCourses

        val normalizedQuery = normalizeForSearch(searchQuery)
        val searched = if (normalizedQuery.isBlank()) {
            categoryFiltered
        } else {
            categoryFiltered.filter { deck ->
                val normalizedName = normalizeForSearch(deck.name)
                val normalizedDescription = normalizeForSearch(deck.description.orEmpty())
                normalizedName.contains(normalizedQuery) || normalizedDescription.contains(normalizedQuery)
            }
        }

        _courses.value = searched
    }

    private fun normalizeForSearch(raw: String): String {
        val normalized = Normalizer.normalize(raw.lowercase(Locale.getDefault()), Normalizer.Form.NFD)
        return normalized.replace("\\p{M}+".toRegex(), "").trim()
    }
}
