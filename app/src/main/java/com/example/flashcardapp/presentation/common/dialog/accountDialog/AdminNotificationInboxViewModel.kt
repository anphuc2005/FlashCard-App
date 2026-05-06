package com.example.flashcardapp.presentation.common.dialog.accountDialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.model.UserNotification
import com.example.flashcardapp.domain.usecase.notification.GetNotificationsPageUseCase
import com.example.flashcardapp.domain.usecase.notification.MarkNotificationReadUseCase
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdminNotificationInboxViewModel(
    private val getNotificationsPageUseCase: GetNotificationsPageUseCase,
    private val markNotificationReadUseCase: MarkNotificationReadUseCase
) : ViewModel() {

    private companion object {
        const val DEFAULT_PAGE_SIZE = 20
    }

    private val _notifications = MutableStateFlow<List<UserNotification>>(emptyList())
    val notifications: StateFlow<List<UserNotification>> = _notifications.asStateFlow()

    private val _isInitialLoading = MutableStateFlow(false)
    val isInitialLoading: StateFlow<Boolean> = _isInitialLoading.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error.asSharedFlow()

    private val _readSuccess = MutableSharedFlow<String>()
    val readSuccess: SharedFlow<String> = _readSuccess.asSharedFlow()

    private var currentPage = 0
    private var isLastPage = true
    private var hasLoadedOnce = false
    private val markingIds = mutableSetOf<String>()

    fun loadInitial(force: Boolean = false) {
        if (_isInitialLoading.value) return
        if (hasLoadedOnce && !force) return
        fetchNotifications(page = 0, append = false)
    }

    fun loadMore() {
        if (_isInitialLoading.value || _isLoadingMore.value || isLastPage) return
        fetchNotifications(page = currentPage + 1, append = true)
    }

    fun markAsRead(notificationId: String) {
        val current = _notifications.value.firstOrNull { it.id == notificationId } ?: return
        if (current.isRead || markingIds.contains(notificationId)) return

        markingIds.add(notificationId)
        viewModelScope.launch {
            markNotificationReadUseCase(notificationId)
                .onSuccess {
                    _notifications.update { currentItems ->
                        currentItems.map { item ->
                            if (item.id == notificationId) item.copy(isRead = true) else item
                        }
                    }
                    _readSuccess.emit(notificationId)
                }
                .onFailure { throwable ->
                    _error.emit(throwable.message ?: "Không thể cập nhật trạng thái thông báo")
                }

            markingIds.remove(notificationId)
        }
    }

    private fun fetchNotifications(page: Int, append: Boolean) {
        viewModelScope.launch {
            if (append) {
                _isLoadingMore.value = true
            } else {
                _isInitialLoading.value = true
            }

            getNotificationsPageUseCase(page = page, size = DEFAULT_PAGE_SIZE)
                .onSuccess { pageResult ->
                    currentPage = pageResult.currentPage
                    isLastPage = pageResult.isLast
                    hasLoadedOnce = true
                    _notifications.value = if (append) {
                        (_notifications.value + pageResult.content).distinctBy { it.id }
                    } else {
                        pageResult.content
                    }
                }
                .onFailure { throwable ->
                    _error.emit(throwable.message ?: "Không thể tải danh sách thông báo")
                }

            _isInitialLoading.value = false
            _isLoadingMore.value = false
        }
    }
}
