package com.example.flashcardapp.data.datasource.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.flashcardapp.data.datasource.local.entity.UserProfileEntity
import com.example.flashcardapp.data.datasource.local.entity.UserProfileEntity.Companion.PROFILE_ID

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProfile(profile: UserProfileEntity)

    @Query("SELECT * FROM profile_table WHERE id = :id LIMIT 1")
    suspend fun getProfile(id: String = PROFILE_ID): UserProfileEntity?

    @Query("DELETE FROM profile_table")
    suspend fun deleteProfile()
}
