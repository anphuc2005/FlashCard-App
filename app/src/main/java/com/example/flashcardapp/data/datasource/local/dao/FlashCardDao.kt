package com.example.flashcardapp.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.flashcardapp.data.datasource.local.entity.FlashCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashCardDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: FlashCardEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCards(cards: List<FlashCardEntity>)

    @Update
    suspend fun updateCard(card: FlashCardEntity)

    @Delete
    suspend fun deleteCard(card: FlashCardEntity)

    @Query("SELECT * FROM flashcard_table WHERE id = :id")
    suspend fun getCardById(id: String): FlashCardEntity?

    @Query("SELECT * FROM flashcard_table WHERE deckId = :deckId")
    fun getCardsByDeckId(deckId: String): Flow<List<FlashCardEntity>>

    @Query("SELECT * FROM flashcard_table WHERE deckId = :deckId")
    suspend fun getCardsSnapshotByDeckId(deckId: String): List<FlashCardEntity>

    @Query("SELECT * FROM flashcard_table")
    fun getAllCards(): Flow<List<FlashCardEntity>>

    @Query("SELECT * FROM flashcard_table")
    suspend fun getAllCardsSnapshot(): List<FlashCardEntity>

    @Query("SELECT * FROM flashcard_table WHERE isSynced = 0")
    suspend fun getUnsyncedCards(): List<FlashCardEntity>

    @Query("DELETE FROM flashcard_table WHERE deckId = :deckId")
    suspend fun deleteCardsByDeckId(deckId: String)
}
