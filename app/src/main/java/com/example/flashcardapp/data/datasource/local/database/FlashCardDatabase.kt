package com.example.flashcardapp.data.datasource.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.flashcardapp.data.datasource.local.dao.ChatMessageDao
import com.example.flashcardapp.data.datasource.local.dao.DeckDao
import com.example.flashcardapp.data.datasource.local.dao.FlashCardDao
import com.example.flashcardapp.data.datasource.local.dao.StudyReviewDao
import com.example.flashcardapp.data.datasource.local.entity.ChatMessageEntity
import com.example.flashcardapp.data.datasource.local.entity.DeckEntity
import com.example.flashcardapp.data.datasource.local.entity.FlashCardEntity
import com.example.flashcardapp.data.datasource.local.entity.StudyReviewEntity

@Database(
    entities = [
        FlashCardEntity::class,
        ChatMessageEntity::class,
        StudyReviewEntity::class,
        DeckEntity::class
    ],
    version = 11,
    exportSchema = false
)
abstract class FlashCardDatabase : RoomDatabase() {

    abstract fun flashCardDao(): FlashCardDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun studyReviewDao(): StudyReviewDao
    abstract fun deckDao(): DeckDao

    companion object {
        @Volatile
        private var instance: FlashCardDatabase? = null

        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `deck_table` (
                        `id` TEXT NOT NULL,
                        `categoryId` TEXT,
                        `name` TEXT NOT NULL,
                        `description` TEXT,
                        `themeColor` TEXT,
                        `isPublic` INTEGER NOT NULL DEFAULT 0,
                        `createdAt` TEXT,
                        `updatedAt` TEXT,
                        `cardCount` INTEGER NOT NULL DEFAULT 0,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `deck_table` ADD COLUMN `isSynced` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("ALTER TABLE `deck_table` ADD COLUMN `pendingOperation` TEXT")
                db.execSQL("ALTER TABLE `flashcard_table` ADD COLUMN `isDeleted` INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE `flashcard_table` ADD COLUMN `createdAt` TEXT")
                db.execSQL("ALTER TABLE `flashcard_table` ADD COLUMN `updatedAt` TEXT")
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `deck_table` ADD COLUMN `isDeleted` INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getInstance(context: Context): FlashCardDatabase {
            return instance ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    FlashCardDatabase::class.java,
                    "flashcard_database"
                )
                    .addMigrations(MIGRATION_8_9, MIGRATION_9_10, MIGRATION_10_11)
                    .build()
                instance = db
                db
            }
        }
    }
}
