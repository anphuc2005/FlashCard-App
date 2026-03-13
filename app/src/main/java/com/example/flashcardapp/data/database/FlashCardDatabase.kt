package com.example.flashcardapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.flashcardapp.data.dao.ChatMessageDao
import com.example.flashcardapp.data.dao.DeckDao
import com.example.flashcardapp.data.dao.FlashCardDao
import com.example.flashcardapp.data.entity.ChatMessageEntity
import com.example.flashcardapp.data.entity.DeckEntity
import com.example.flashcardapp.data.entity.FlashCardEntity

@Database(
    entities = [DeckEntity::class, FlashCardEntity::class, ChatMessageEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FlashCardDatabase : RoomDatabase() {

    abstract fun deckDao(): DeckDao
    abstract fun flashCardDao(): FlashCardDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var instance: FlashCardDatabase? = null

        fun getInstance(context: Context): FlashCardDatabase {
            return instance ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    FlashCardDatabase::class.java,
                    "flashcard_database"
                ).build()
                instance = db
                db
            }
        }
    }
}

