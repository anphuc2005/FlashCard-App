package com.example.flashcardapp.data.datasource.remote.api

import com.example.flashcardapp.data.datasource.remote.model.ApiResponse
import com.example.flashcardapp.data.datasource.remote.model.SubmitReportRequestDto
import retrofit2.http.Body
import retrofit2.http.POST

interface ReportApiService {

    @POST("reports")
    suspend fun submitReport(@Body request: SubmitReportRequestDto): ApiResponse<Any?>
}
