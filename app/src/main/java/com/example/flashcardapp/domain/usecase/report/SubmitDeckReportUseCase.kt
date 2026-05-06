package com.example.flashcardapp.domain.usecase.report

import com.example.flashcardapp.data.repository.ReportRepository

class SubmitDeckReportUseCase(
    private val reportRepository: ReportRepository
) {
    suspend operator fun invoke(targetDeckId: String, reason: String): Result<String> {
        return reportRepository.submitDeckReport(targetDeckId = targetDeckId, reason = reason)
    }
}
