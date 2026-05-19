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
import com.example.flashcardapp.data.datasource.local.dao.UserProfileDao
import com.example.flashcardapp.data.datasource.local.entity.ChatMessageEntity
import com.example.flashcardapp.data.datasource.local.entity.DeckEntity
import com.example.flashcardapp.data.datasource.local.entity.FlashCardEntity
import com.example.flashcardapp.data.datasource.local.entity.StudyReviewEntity
import com.example.flashcardapp.data.datasource.local.entity.UserProfileEntity

@Database(
    entities = [
        FlashCardEntity::class,
        ChatMessageEntity::class,
        StudyReviewEntity::class,
        DeckEntity::class,
        UserProfileEntity::class
    ],
    version = 11,
    exportSchema = false
)
abstract class FlashCardDatabase : RoomDatabase() {

    abstract fun flashCardDao(): FlashCardDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun studyReviewDao(): StudyReviewDao
    abstract fun deckDao(): DeckDao
    abstract fun userProfileDao(): UserProfileDao

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
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `profile_table` (
                        `id` TEXT NOT NULL,
                        `email` TEXT NOT NULL,
                        `displayName` TEXT NOT NULL,
                        `avatarUrl` TEXT,
                        `createdAt` TEXT,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                migrateToVersion11(db)
            }
        }

        private val LEGACY_MIGRATIONS_TO_11: Array<Migration> = (1..9)
            .map { oldVersion ->
                object : Migration(oldVersion, 11) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        migrateToVersion11(db)
                    }
                }
            }
            .toTypedArray()

        fun getInstance(context: Context): FlashCardDatabase {
            return instance ?: synchronized(this) {
                val db = Room.databaseBuilder(
                    context.applicationContext,
                    FlashCardDatabase::class.java,
                    "flashcard_database"
                )
                    .addMigrations(
                        MIGRATION_8_9,
                        MIGRATION_9_10,
                        MIGRATION_10_11,
                        *LEGACY_MIGRATIONS_TO_11
                    )
                    .build()
                instance = db
                db
            }
        }

        private fun migrateToVersion11(db: SupportSQLiteDatabase) {
            createCurrentTablesIfMissing(db)
            addMissingCurrentColumns(db)
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_flashcard_table_deckId` ON `flashcard_table` (`deckId`)"
            )
        }

        private fun createCurrentTablesIfMissing(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `flashcard_table` (
                    `id` TEXT NOT NULL,
                    `question` TEXT NOT NULL,
                    `answer` TEXT NOT NULL,
                    `imageUrl` TEXT,
                    `localImagePath` TEXT,
                    `deckId` TEXT NOT NULL,
                    `interval` INTEGER NOT NULL DEFAULT 0,
                    `repetition` INTEGER NOT NULL DEFAULT 0,
                    `easeFactor` REAL NOT NULL DEFAULT 2.5,
                    `nextReviewDate` TEXT,
                    `isSynced` INTEGER NOT NULL DEFAULT 1,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `chat_message_table` (
                    `id` TEXT NOT NULL,
                    `text` TEXT NOT NULL,
                    `isUser` INTEGER NOT NULL,
                    `timestamp` INTEGER NOT NULL DEFAULT 0,
                    `status` TEXT NOT NULL DEFAULT 'SUCCESS',
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `study_review_table` (
                    `id` TEXT NOT NULL,
                    `cardId` TEXT NOT NULL,
                    `deckId` TEXT NOT NULL,
                    `studyMode` TEXT NOT NULL,
                    `grade` INTEGER NOT NULL,
                    `studiedAt` TEXT NOT NULL,
                    `durationSeconds` INTEGER,
                    `isSynced` INTEGER NOT NULL DEFAULT 0,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
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
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `profile_table` (
                    `id` TEXT NOT NULL,
                    `email` TEXT NOT NULL,
                    `displayName` TEXT NOT NULL,
                    `avatarUrl` TEXT,
                    `createdAt` TEXT,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
        }

        private fun addMissingCurrentColumns(db: SupportSQLiteDatabase) {
            addColumnIfMissing(db, "flashcard_table", "imageUrl", "TEXT")
            addColumnIfMissing(db, "flashcard_table", "localImagePath", "TEXT")
            addColumnIfMissing(db, "flashcard_table", "deckId", "TEXT NOT NULL DEFAULT ''")
            addColumnIfMissing(db, "flashcard_table", "interval", "INTEGER NOT NULL DEFAULT 0")
            addColumnIfMissing(db, "flashcard_table", "repetition", "INTEGER NOT NULL DEFAULT 0")
            addColumnIfMissing(db, "flashcard_table", "easeFactor", "REAL NOT NULL DEFAULT 2.5")
            addColumnIfMissing(db, "flashcard_table", "nextReviewDate", "TEXT")
            addColumnIfMissing(db, "flashcard_table", "isSynced", "INTEGER NOT NULL DEFAULT 1")

            addColumnIfMissing(db, "chat_message_table", "timestamp", "INTEGER NOT NULL DEFAULT 0")
            addColumnIfMissing(db, "chat_message_table", "status", "TEXT NOT NULL DEFAULT 'SUCCESS'")

            addColumnIfMissing(db, "study_review_table", "durationSeconds", "INTEGER")
            addColumnIfMissing(db, "study_review_table", "isSynced", "INTEGER NOT NULL DEFAULT 0")

            addColumnIfMissing(db, "deck_table", "categoryId", "TEXT")
            addColumnIfMissing(db, "deck_table", "description", "TEXT")
            addColumnIfMissing(db, "deck_table", "themeColor", "TEXT")
            addColumnIfMissing(db, "deck_table", "isPublic", "INTEGER NOT NULL DEFAULT 0")
            addColumnIfMissing(db, "deck_table", "createdAt", "TEXT")
            addColumnIfMissing(db, "deck_table", "updatedAt", "TEXT")
            addColumnIfMissing(db, "deck_table", "cardCount", "INTEGER NOT NULL DEFAULT 0")

            addColumnIfMissing(db, "profile_table", "avatarUrl", "TEXT")
            addColumnIfMissing(db, "profile_table", "createdAt", "TEXT")
        }

        private fun addColumnIfMissing(
            db: SupportSQLiteDatabase,
            tableName: String,
            columnName: String,
            columnDefinition: String
        ) {
            db.query("PRAGMA table_info(`$tableName`)").use { cursor ->
                val nameColumnIndex = cursor.getColumnIndex("name")
                while (cursor.moveToNext()) {
                    if (cursor.getString(nameColumnIndex) == columnName) {
                        return
                    }
                }
            }
            db.execSQL("ALTER TABLE `$tableName` ADD COLUMN `$columnName` $columnDefinition")
        }
    }
}
