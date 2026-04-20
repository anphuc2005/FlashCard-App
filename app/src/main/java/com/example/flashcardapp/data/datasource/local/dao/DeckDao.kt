package com.example.flashcardapp.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.flashcardapp.data.datasource.local.entity.DeckEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeckDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeck(deck: DeckEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllDecks(decks: List<DeckEntity>)

    @Update
    suspend fun updateDeck(deck: DeckEntity)

    @Delete
    suspend fun deleteDeck(deck: DeckEntity)

    @Query("SELECT * FROM deck_table WHERE id = :id")
    suspend fun getDeckById(id: String): DeckEntity?

    @Query("SELECT * FROM deck_table")
    fun getAllDecks(): Flow<List<DeckEntity>>

    @Query("SELECT d.*, (SELECT COUNT(id) FROM flashcard_table f WHERE f.deckId = d.id) AS cardCount FROM deck_table d")
    fun getAllDecksWithCardCount(): Flow<List<com.example.flashcardapp.data.datasource.local.entity.DeckWithCardCount>>

    @Query("DELETE FROM deck_table")
    suspend fun deleteAllDecks()
}
