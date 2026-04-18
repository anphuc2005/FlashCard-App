package com.example.flashcardapp.data.datasource.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.flashcardapp.data.datasource.local.dao.ChatMessageDao
import com.example.flashcardapp.data.datasource.local.dao.DeckDao
import com.example.flashcardapp.data.datasource.local.dao.FlashCardDao
import com.example.flashcardapp.data.datasource.local.entity.ChatMessageEntity
import com.example.flashcardapp.data.datasource.local.entity.DeckEntity
import com.example.flashcardapp.data.datasource.local.entity.FlashCardEntity

@Database(
    entities = [DeckEntity::class, FlashCardEntity::class, ChatMessageEntity::class],
    version = 3,
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
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                instance = db
                db
            }
        }
    }
}

