package com.example.flashcardapp.domain.usecase.upload

import com.example.flashcardapp.domain.repository.UploadRepository
import java.io.File

class UploadImageUseCase(
    private val repository: UploadRepository
) {
    suspend operator fun invoke(file: File): Result<String> {
        return repository.uploadImage(file)
    }
}
