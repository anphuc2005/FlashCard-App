package com.example.flashcardapp.presentation.feature.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.model.Category
import com.example.flashcardapp.domain.model.Deck
import com.example.flashcardapp.domain.model.DeckExplorePage
import com.example.flashcardapp.domain.usecase.category.GetAllCategoriesUseCase
import com.example.flashcardapp.domain.usecase.deck.CloneDeckUseCase
import com.example.flashcardapp.domain.usecase.deck.GetExploreDecksFromApiUseCase
import com.example.flashcardapp.domain.usecase.report.SubmitDeckReportUseCase
import java.text.Normalizer
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiscoverViewModel(
    private val getAllDecksFromApiUseCase: GetExploreDecksFromApiUseCase,
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val cloneDeckUseCase: CloneDeckUseCase,
    private val submitDeckReportUseCase: SubmitDeckReportUseCase
) : ViewModel() {

    private companion object {
        const val DEFAULT_PAGE_SIZE = 5
        const val FULL_PAGE_SIZE = 50
    }

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

    private val _reportSuccess = MutableSharedFlow<String>()
    val reportSuccess: SharedFlow<String> = _reportSuccess.asSharedFlow()

    private var allCourses: List<Deck> = emptyList()
    private var latestPage: DeckExplorePage = DeckExplorePage()
    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()
    private val _currentCoursePage = MutableStateFlow(1)
    val currentCoursePage: StateFlow<Int> = _currentCoursePage.asStateFlow()
    private val _totalCoursePages = MutableStateFlow(1)
    val totalCoursePages: StateFlow<Int> = _totalCoursePages.asStateFlow()
    private val _isShowingAllCourses = MutableStateFlow(false)
    val isShowingAllCourses: StateFlow<Boolean> = _isShowingAllCourses.asStateFlow()
    private var searchQuery: String = ""
    private var searchJob: Job? = null

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

                loadCoursesPage(page = 0)

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun filterCoursesByCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
        applyFilters()
    }

    fun viewAllCourses() {
        if (_isShowingAllCourses.value) {
            showPagedCourses(page = 0)
            return
        }
        showAllCourses()
    }

    fun updateSearchQuery(query: String) {
        if (searchQuery == query) return
        searchQuery = query
        applyFilters()
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            _error.value = null
            try {
                // Keep local filtering instant and only sync with backend after a brief pause.
                delay(180)
                if (_isShowingAllCourses.value) {
                    loadAllCourses(preserveOnFailure = true)
                } else {
                    loadCoursesPage(page = 0, preserveOnFailure = true)
                }
            } catch (_: Exception) {
                // Ignore cancelled/failed background refresh and keep the current filtered list.
            }
        }
    }

    fun goToNextCoursePage() {
        if (_isLoading.value || _isShowingAllCourses.value || latestPage.last) return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                loadCoursesPage(page = latestPage.currentPage + 1)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun goToPreviousCoursePage() {
        if (_isLoading.value || _isShowingAllCourses.value || latestPage.first) return
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                loadCoursesPage(page = (latestPage.currentPage - 1).coerceAtLeast(0))
            } finally {
                _isLoading.value = false
            }
        }
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

    fun submitDeckReport(deckId: String, reason: String) {
        val trimmedReason = reason.trim()
        if (trimmedReason.length !in 10..500) {
            _error.value = "Lý do báo cáo cần từ 10 đến 500 ký tự."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val result = submitDeckReportUseCase(deckId, trimmedReason)
                if (result.isSuccess) {
                    _reportSuccess.emit(result.getOrDefault("Đã gửi báo cáo thành công!"))
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Không thể gửi báo cáo"
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun applyFilters() {
        val categoryFiltered = _selectedCategoryId.value?.let { categoryId ->
            allCourses.filter { it.categoryId == categoryId }
        } ?: allCourses

        val normalizedQuery = normalizeForSearch(searchQuery)
        _courses.value = if (normalizedQuery.isBlank()) {
            categoryFiltered
        } else {
            categoryFiltered.filter { deck ->
                normalizeForSearch(deck.name).contains(normalizedQuery)
            }
        }
    }

    private suspend fun loadCoursesPage(page: Int, preserveOnFailure: Boolean = false) {
        val deckResult = getAllDecksFromApiUseCase(
            page = page,
            size = DEFAULT_PAGE_SIZE,
            query = searchQuery.takeIf { it.isNotBlank() }
        )

        if (deckResult.isSuccess) {
            val pageResult = deckResult.getOrDefault(DeckExplorePage())
            latestPage = pageResult
            allCourses = pageResult.content
            _isShowingAllCourses.value = false
            _currentCoursePage.value = (pageResult.currentPage + 1).coerceAtLeast(1)
            _totalCoursePages.value = pageResult.totalPages.coerceAtLeast(1)
            applyFilters()
        } else {
            _error.value = deckResult.exceptionOrNull()?.message
            if (!preserveOnFailure) {
                latestPage = DeckExplorePage()
                allCourses = emptyList()
                _isShowingAllCourses.value = false
                _currentCoursePage.value = 1
                _totalCoursePages.value = 1
                applyFilters()
            }
        }
    }

    private fun showPagedCourses(page: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                loadCoursesPage(page = page)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun showAllCourses() {
        _selectedCategoryId.value = null
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                loadAllCourses()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun loadAllCourses(preserveOnFailure: Boolean = false) {
        val firstPageResult = getAllDecksFromApiUseCase(
            page = 0,
            size = FULL_PAGE_SIZE,
            query = searchQuery.takeIf { it.isNotBlank() }
        )

        if (firstPageResult.isFailure) {
            _error.value = firstPageResult.exceptionOrNull()?.message
            if (!preserveOnFailure) {
                latestPage = DeckExplorePage()
                allCourses = emptyList()
                _isShowingAllCourses.value = true
                _currentCoursePage.value = 1
                _totalCoursePages.value = 1
                applyFilters()
            }
            return
        }

        val firstPage = firstPageResult.getOrDefault(DeckExplorePage())
        var combinedCourses = firstPage.content
        val totalPages = firstPage.totalPages.coerceAtLeast(1)

        if (totalPages > 1) {
            for (page in 1 until totalPages) {
                val nextPageResult = getAllDecksFromApiUseCase(
                    page = page,
                    size = FULL_PAGE_SIZE,
                    query = searchQuery.takeIf { it.isNotBlank() }
                )
                if (nextPageResult.isFailure) {
                    _error.value = nextPageResult.exceptionOrNull()?.message
                    break
                }
                combinedCourses += nextPageResult.getOrDefault(DeckExplorePage()).content
            }
        }

        latestPage = firstPage
        allCourses = combinedCourses.distinctBy { it.id }
        _isShowingAllCourses.value = true
        _currentCoursePage.value = 1
        _totalCoursePages.value = totalPages
        applyFilters()
    }

    private fun normalizeForSearch(value: String): String {
        return Normalizer.normalize(value.lowercase(), Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .replace('đ', 'd')
            .trim()
    }
}
