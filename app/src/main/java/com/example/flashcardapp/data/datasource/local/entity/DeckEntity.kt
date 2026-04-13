package com.example.flashcardapp.data.datasource.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deck_table")
data class DeckEntity(
    @PrimaryKey
    val id: String,
    val categoryId: String? = null,
    val name: String,
    val description: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
