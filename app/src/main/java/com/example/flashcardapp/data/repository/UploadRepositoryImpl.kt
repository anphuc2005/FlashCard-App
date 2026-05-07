package com.example.flashcardapp.data.repository

import com.example.flashcardapp.data.datasource.remote.api.UploadApiService
import com.example.flashcardapp.domain.repository.UploadRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class UploadRepositoryImpl(
    private val uploadApiService: UploadApiService
) : UploadRepository {
    override suspend fun uploadImage(file: File): Result<String> {
        return try {
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

            val response = uploadApiService.uploadFile(part)
            Result.success(response.url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
