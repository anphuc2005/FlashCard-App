package com.example.flashcardapp.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.flashcardapp.data.datasource.local.entity.StudyReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyReviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: StudyReviewEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<StudyReviewEntity>)

    @Query("SELECT * FROM study_review_table WHERE isSynced = 0 ORDER BY studiedAt ASC")
    suspend fun getUnsyncedReviews(): List<StudyReviewEntity>

    @Query("SELECT * FROM study_review_table WHERE deckId = :deckId ORDER BY studiedAt DESC")
    fun observeReviewsByDeck(deckId: String): Flow<List<StudyReviewEntity>>

    @Query("SELECT DISTINCT cardId FROM study_review_table WHERE deckId = :deckId")
    suspend fun getReviewedCardIds(deckId: String): List<String>

    @Query(
        """
        SELECT deckId
        FROM study_review_table
        GROUP BY deckId
        ORDER BY MAX(studiedAt) DESC
        """
    )
    suspend fun getRecentlyStudiedDeckIds(): List<String>

    @Query("DELETE FROM study_review_table WHERE id IN (:ids)")
    suspend fun deleteReviews(ids: List<String>)

    @Query("DELETE FROM study_review_table")
    suspend fun deleteAllReviews()
}
