package com.example.flashcardapp.presentation.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.model.Deck
import com.example.flashcardapp.domain.model.Shortcut
import com.example.flashcardapp.core.utils.MockDeckData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val isLoading: Boolean = false,
    val activeDeck: Deck? = null,
    val recentDecks: List<Deck> = emptyList(),
    val shortcuts: List<Shortcut> = emptyList(),
    val error: String? = null,
    val userStreak: Int = 0,
    val userGreeting: String = "",
    val userAvatarUrl: String? = null,
    val userProgress: Int = 0
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadMockData()
        initializeShortcuts()
    }

    private fun loadMockData() {
        val mockDatas = MockDeckData.getMockDecks()

        _uiState.value = HomeUiState(
            isLoading = false,
            recentDecks = mockDatas,
            activeDeck = mockDatas.first(),
            userProgress = 65,
            userStreak = 12
        )
    }

    private fun loadHomeData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    activeDeck = null,
                    recentDecks = emptyList(),
                    userStreak = 12,
                    userProgress = 85
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Có lỗi xảy ra"
                )
            }
        }
    }

    private fun initializeShortcuts() {
        val shortcuts = listOf(
            Shortcut(
                id = "1",
                title = "Tạo mới",
                iconResId = com.example.flashcardapp.R.drawable.ic_create,
                backgroundResId = com.example.flashcardapp.R.color.md_icon_blue_background,
                action = "CREATE"
            ),
            Shortcut(
                id = "2",
                title = "Tìm kiếm",
                iconResId = com.example.flashcardapp.R.drawable.ic_search_shortcut,
                backgroundResId = com.example.flashcardapp.R.color.md_icon_red_background,
                action = "SEARCH"
            ),
            Shortcut(
                id = "3",
                title = "Danh sách",
                iconResId = com.example.flashcardapp.R.drawable.ic_deck_shortcut,
                backgroundResId = com.example.flashcardapp.R.color.md_icon_orange_background,
                action = "LIST"
            ),
            Shortcut(
                id = "4",
                title = "Cài đặt",
                iconResId = com.example.flashcardapp.R.drawable.ic_setting_shortcut,
                backgroundResId = com.example.flashcardapp.R.color.md_icon_purple_background,
                action = "SETTINGS"
            ),
            Shortcut(
                id = "5",
                title = "Thông báo",
                iconResId = com.example.flashcardapp.R.drawable.ic_notif_shortcut,
                backgroundResId = com.example.flashcardapp.R.color.md_icon_yellow_background,
                action = "SETTINGS"
            ),
            Shortcut(
                id = "6",
                title = "Thống kê",
                iconResId = com.example.flashcardapp.R.drawable.ic_stats_shortcut,
                backgroundResId = com.example.flashcardapp.R.color.md_icon_green_background,
                action = "SETTINGS"
            ),
            Shortcut(
                id = "7",
                title = "Cá nhân",
                iconResId = com.example.flashcardapp.R.drawable.ic_account_shortcut,
                backgroundResId = com.example.flashcardapp.R.color.md_icon_blue_background,
                action = "SETTINGS"
            )
        )

        _uiState.value = _uiState.value.copy(shortcuts = shortcuts)
    }

    fun refreshData() {
        loadHomeData()
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

