package com.example.flashcardapp.data.repository

import android.content.Context
import android.util.Log
import com.example.flashcardapp.data.datasource.local.dao.FlashCardDao
import com.example.flashcardapp.data.datasource.local.dao.StudyReviewDao
import com.example.flashcardapp.data.datasource.local.entity.FlashCardEntity
import com.example.flashcardapp.data.datasource.local.entity.toEntity
import com.example.flashcardapp.data.datasource.local.session.StudyStreakStore
import com.example.flashcardapp.data.datasource.remote.api.StudyApiService
import com.example.flashcardapp.data.datasource.remote.model.UpsertStudySessionRequestDto
import com.example.flashcardapp.data.datasource.remote.model.toDomain
import com.example.flashcardapp.data.datasource.remote.model.toDto
import com.example.flashcardapp.domain.model.FlashCard
import com.example.flashcardapp.domain.model.study.StudyReview
import com.example.flashcardapp.domain.model.study.StudyRecentSession
import com.example.flashcardapp.domain.model.study.StudySessionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private const val STUDY_SYNC_PREFS = "study_sync_prefs"
private const val KEY_LAST_SYNC_TIME = "lastSyncTime"
private const val TAG = "StudyRepository"

class StudyRepository(
    private val studyApiService: StudyApiService,
    private val studyReviewDao: StudyReviewDao,
    private val flashCardDao: FlashCardDao,
    private val applicationContext: Context
) {

    suspend fun getSessionByDeck(deckId: String, mode: String): Result<StudySessionState?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = studyApiService.getSessionByDeck(deckId, mode)
                if (response.isSuccess()) {
                    Result.success(response.data?.toDomain())
                } else {
                    val message = response.message.orEmpty()
                    if (message.contains("not found", ignoreCase = true)) {
                        Result.success(null)
                    } else {
                        Result.failure(Exception(message.ifBlank { "Failed to load study session by deck" }))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun getRecentSession(): Result<StudyRecentSession?> {
        return withContext(Dispatchers.IO) {
            try {
                val response = studyApiService.getRecentSession()
                if (response.isSuccess()) {
                    Result.success(response.data?.toDomain())
                } else {
                    val message = response.message.orEmpty()
                    if (message.contains("not found", ignoreCase = true)) {
                        Result.success(null)
                    } else {
                        Result.failure(Exception(message.ifBlank { "Failed to load recent session" }))
                    }
                }
            } catch (_: Exception) {
                // Không chặn trải nghiệm Home/Learning nếu API recent bị lỗi.
                Result.success(null)
            }
        }
    }

    suspend fun getSessionCards(deckId: String, mode: String): Result<List<FlashCard>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = studyApiService.getSessionCards(deckId, mode)
                if (response.isSuccess() && response.data != null) {
                    val normalizedCards = response.data.map { dto ->
                        dto.toDomain().copy(deckId = deckId)
                    }
                    val entities = normalizedCards.map { card ->
                        FlashCardEntity(
                            id = card.id,
                            question = card.question,
                            answer = card.answer,
                            imageUrl = card.imageUrl,
                            deckId = deckId,
                            isSynced = true
                        )
                    }
                    flashCardDao.insertAllCards(entities)
                    Result.success(normalizedCards)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to load study session"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun upsertSession(
        deckId: String,
        mode: String,
        cardSequence: List<String>,
        currentIndex: Int
    ): Result<StudySessionState> {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpsertStudySessionRequestDto(
                    deckId = deckId,
                    mode = mode,
                    cardSequence = cardSequence,
                    currentIndex = currentIndex
                )
                val response = studyApiService.upsertSession(request)
                if (response.isSuccess() && response.data != null) {
                    Log.d(
                        TAG,
                        "upsertSession success: deckId=$deckId, mode=$mode, cards=${cardSequence.size}, currentIndex=${response.data.currentIndex}, totalCards=${response.data.totalCards}"
                    )
                    Result.success(response.data.toDomain())
                } else {
                    Log.e(
                        TAG,
                        "upsertSession failed: deckId=$deckId, mode=$mode, cards=${cardSequence.size}, message=${response.message}"
                    )
                    Result.failure(
                        Exception(response.message ?: "Failed to save study session state")
                    )
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteSessionByDeck(deckId: String, mode: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = studyApiService.deleteSessionByDeck(deckId, mode)
                if (response.isSuccessful) {
                    // Backend may return 204 No Content for DELETE.
                    Result.success(Unit)
                } else {
                    val message = response.body()?.message.orEmpty()
                    if (message.contains("not found", ignoreCase = true)) {
                        Result.success(Unit)
                    } else {
                        Result.failure(
                            Exception(
                                message.ifBlank {
                                    "Failed to delete study session (HTTP ${response.code()})"
                                }
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e("DELETE STUDY", "deleteSessionByDeck failed: deckId=$deckId, mode=$mode", e)
                Result.failure(e)
            }
        }
    }

    suspend fun saveReview(review: StudyReview): Result<StudyReview> {
        return withContext(Dispatchers.IO) {
            try {
                studyReviewDao.insertReview(review.toEntity())
                StudyStreakStore.recordStudyEvent(applicationContext, review.studiedAt)
                Result.success(review)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun observeReviewsByDeck(deckId: String): Flow<List<StudyReview>> {
        return studyReviewDao.observeReviewsByDeck(deckId).map { reviews ->
            reviews.map { it.toDomain() }
        }
    }

    suspend fun getReviewedCardIds(deckId: String): Result<Set<String>> {
        return withContext(Dispatchers.IO) {
            try {
                Result.success(studyReviewDao.getReviewedCardIds(deckId).toSet())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun syncReviews(): Result<Int> {
        return withContext(Dispatchers.IO) {
            try {
                val unsyncedReviews = studyReviewDao.getUnsyncedReviews()
                if (unsyncedReviews.isEmpty()) {
                    return@withContext Result.success(0)
                }

                val reviews = unsyncedReviews.map { it.toDomain() }
                val response = studyApiService.syncReviews(reviews.map { it.toDto() })

                if (response.isSuccess() && response.data != null) {
                    val syncedReviews = unsyncedReviews.map { it.copy(isSynced = true) }
                    studyReviewDao.insertReviews(syncedReviews)
                    saveLastSyncTime(response.data.syncedAt)
                    Result.success(unsyncedReviews.size)
                } else {
                    Result.failure(Exception(response.message ?: "Failed to sync study reviews"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    fun getCurrentStreak(): Int {
        return StudyStreakStore.getSnapshot(applicationContext).currentStreak
    }

    fun hasStudiedToday(): Boolean {
        return StudyStreakStore.hasStudiedToday(applicationContext)
    }

    private fun saveLastSyncTime(syncedAt: String) {
        applicationContext
            .getSharedPreferences(STUDY_SYNC_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_SYNC_TIME, syncedAt)
            .apply()
    }
}
