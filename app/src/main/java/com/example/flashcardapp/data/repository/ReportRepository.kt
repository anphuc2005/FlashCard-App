package com.example.flashcardapp.data.repository

import com.example.flashcardapp.core.utils.NetworkErrorHandler
import com.example.flashcardapp.core.utils.UserMessageMapper
import com.example.flashcardapp.data.datasource.remote.api.ReportApiService
import com.example.flashcardapp.data.datasource.remote.model.SubmitReportRequestDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportRepository(
    private val reportApiService: ReportApiService
) {

    suspend fun submitDeckReport(targetDeckId: String, reason: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = reportApiService.submitReport(
                    SubmitReportRequestDto(
                        targetDeckId = targetDeckId,
                        reason = reason
                    )
                )

                if (response.isSuccess()) {
                    Result.success(response.message ?: "Report submitted successfully")
                } else {
                    val message = UserMessageMapper.extractReadableMessage(response.message)
                        ?: "Không thể gửi báo cáo"
                    Result.failure(IllegalStateException(message))
                }
            } catch (throwable: Throwable) {
                Result.failure(IllegalStateException(NetworkErrorHandler.getErrorMessage(throwable)))
            }
        }
    }
}
