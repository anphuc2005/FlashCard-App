package com.example.flashcardapp.domain.repository

import java.io.File

interface UploadRepository {
    suspend fun uploadImage(file: File): Result<String>
}
