package com.example.flashcardapp.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.flashcardapp.data.dao.ChatMessageDao
import com.example.flashcardapp.data.entity.ChatMessageEntity

@Database(entities = [ChatMessageEntity::class], version = 2)
abstract class ChatMessageDatabase : RoomDatabase(){
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: ChatMessageDatabase? = null

        fun getDatabase(context: Context): ChatMessageDatabase {
            return INSTANCE?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChatMessageDatabase::class.java,
                    "chat_message_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}