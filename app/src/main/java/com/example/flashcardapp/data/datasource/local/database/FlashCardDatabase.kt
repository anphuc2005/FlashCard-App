package com.example.flashcardapp.data.datasource.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.flashcardapp.data.datasource.local.dao.ChatMessageDao
import com.example.flashcardapp.data.datasource.local.dao.FlashCardDao
import com.example.flashcardapp.data.datasource.local.dao.StudyReviewDao
import com.example.flashcardapp.data.datasource.local.entity.ChatMessageEntity
import com.example.flashcardapp.data.datasource.local.entity.FlashCardEntity
import com.example.flashcardapp.data.datasource.local.entity.StudyReviewEntity

@Database(
    entities = [FlashCardEntity::class, ChatMessageEntity::class, StudyReviewEntity::class],
    version = 8,
    exportSchema = false
)
abstract class FlashCardDatabase : RoomDatabase() {

    abstract fun flashCardDao(): FlashCardDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun studyReviewDao(): StudyReviewDao

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
                    .fallbackToDestructiveMigration()
                    .build()
                instance = db
                db
            }
        }
    }
}
