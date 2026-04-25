package com.example.flashcardapp.data.datasource.local.entity

import androidx.room.Embedded
import androidx.room.ColumnInfo
import com.example.flashcardapp.data.datasource.local.entity.DeckEntity

data class DeckWithCardCount(
    @Embedded val deck: DeckEntity,
    @ColumnInfo(name = "cardCount") val cardCount: Int
)
