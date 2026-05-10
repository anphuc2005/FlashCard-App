package com.example.flashcardapp.data.datasource.local.dao

import androidx.room.Dao
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
    suspend fun insertDecks(decks: List<DeckEntity>)

    @Update
    suspend fun updateDeck(deck: DeckEntity)

    @Query("SELECT * FROM deck_table ORDER BY updatedAt DESC, createdAt DESC, name ASC")
    fun getAllDecks(): Flow<List<DeckEntity>>

    @Query("SELECT * FROM deck_table ORDER BY updatedAt DESC, createdAt DESC, name ASC")
    suspend fun getAllDecksSnapshot(): List<DeckEntity>

    @Query("SELECT * FROM deck_table WHERE id = :id LIMIT 1")
    suspend fun getDeckById(id: String): DeckEntity?

    @Query("DELETE FROM deck_table WHERE id = :id")
    suspend fun deleteDeckById(id: String)
}
