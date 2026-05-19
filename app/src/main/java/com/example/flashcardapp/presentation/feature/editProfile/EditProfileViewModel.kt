package com.example.flashcardapp.presentation.feature.editProfile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.model.UserProfile
import com.example.flashcardapp.domain.usecase.profile.GetMyProfileUseCase
import com.example.flashcardapp.domain.usecase.profile.UpdateMyProfileUseCase
import com.example.flashcardapp.domain.usecase.upload.UploadImageUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

data class EditProfileUiState(
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val displayName: String = "",
    val email: String = "",
    val avatarUrl: String? = null,
    val createdAt: String? = null,
    val displayNameError: String? = null,
    val loadError: String? = null,
    val saveError: String? = null,
    val isSaved: Boolean = false
)

class EditProfileViewModel(
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val updateMyProfileUseCase: UpdateMyProfileUseCase,
    private val uploadImageUseCase: UploadImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()
    private var serverAvatarUrl: String? = null

    private companion object {
        const val TAG = "EditProfileVM"
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadError = null,
                    saveError = null
                )
            }

            getMyProfileUseCase()
                .onSuccess { profile ->
                    Log.d(
                        TAG,
                        "users/me loaded -> email=${profile.email}, displayName=${profile.displayName}, avatarUrl=${profile.avatarUrl}, createdAt=${profile.createdAt}"
                    )
                    serverAvatarUrl = profile.avatarUrl
                    _uiState.value = mapProfileToState(profile)
                }
                .onFailure { throwable ->
                    Log.e(TAG, "users/me load failed", throwable)
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loadError = throwable.message ?: "Không tải được thông tin cá nhân"
                        )
                    }
                }
        }
    }

    fun onDisplayNameChanged(value: String) {
        _uiState.update {
            it.copy(
                displayName = value,
                displayNameError = null,
                saveError = null,
                isSaved = false
            )
        }
    }

    fun updateAvatarPreview(uriString: String) {
        _uiState.update { it.copy(avatarUrl = uriString) }
    }

    fun submit(imageFile: File? = null) {
        val trimmedName = _uiState.value.displayName.trim()
        if (trimmedName.isBlank()) {
            _uiState.update {
                it.copy(displayNameError = "Họ và tên không được để trống")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    saveError = null,
                    displayNameError = null,
                    isSaved = false
                )
            }

            var imageUrlToSend = serverAvatarUrl
            if (imageFile != null) {
                Log.d(TAG, "Uploading avatar before PATCH users/me")
                val uploadResult = uploadImageUseCase(imageFile)
                val uploadedUrl = uploadResult.getOrElse { throwable ->
                    Log.e(TAG, "Avatar upload failed", throwable)
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveError = throwable.message ?: "Tải ảnh thất bại"
                        )
                    }
                    return@launch
                }
                imageUrlToSend = uploadedUrl
                Log.d(TAG, "Avatar upload success -> imageUrl=$uploadedUrl")
                serverAvatarUrl = uploadedUrl
            }

            updateMyProfileUseCase(
                displayName = trimmedName,
                imageUrl = imageUrlToSend
            )
                .onSuccess { profile ->
                    serverAvatarUrl = profile.avatarUrl
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            displayName = profile.displayName,
                            email = profile.email,
                            avatarUrl = profile.avatarUrl,
                            createdAt = profile.createdAt,
                            isSaved = true
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saveError = throwable.message ?: "Cập nhật thất bại"
                        )
                    }
                }
        }
    }

    fun consumeSavedEvent() {
        _uiState.update { it.copy(isSaved = false) }
    }

    fun consumeLoadError() {
        _uiState.update { it.copy(loadError = null) }
    }

    fun consumeSaveError() {
        _uiState.update { it.copy(saveError = null) }
    }

    private fun mapProfileToState(profile: UserProfile): EditProfileUiState {
        return EditProfileUiState(
            isLoading = false,
            displayName = profile.displayName,
            email = profile.email,
            avatarUrl = profile.avatarUrl,
            createdAt = profile.createdAt
        )
    }
}

class EditProfileViewModelFactory(
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val updateMyProfileUseCase: UpdateMyProfileUseCase,
    private val uploadImageUseCase: UploadImageUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
            return EditProfileViewModel(
                getMyProfileUseCase = getMyProfileUseCase,
                updateMyProfileUseCase = updateMyProfileUseCase,
                uploadImageUseCase = uploadImageUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
