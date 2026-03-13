package com.example.flashcarapp.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.flashcarapp.data.entity.DeckEntity
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

    @Query("DELETE FROM deck_table WHERE id = :id")
    suspend fun deleteDeckById(id: String)

    @Query("DELETE FROM deck_table")
    suspend fun deleteAllDecks()
}

