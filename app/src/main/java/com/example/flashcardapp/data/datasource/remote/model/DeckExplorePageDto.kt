package com.example.flashcardapp.data.datasource.remote.model

import com.example.flashcardapp.domain.model.Deck
import com.example.flashcardapp.domain.model.DeckExplorePage
import com.google.gson.annotations.SerializedName

data class DeckExplorePageDto(
    @SerializedName("content") val content: List<DeckDto> = emptyList(),
    @SerializedName("currentPage") val currentPage: Int = 0,
    @SerializedName("pageSize") val pageSize: Int = 5,
    @SerializedName("totalElements") val totalElements: Long = 0L,
    @SerializedName("totalPages") val totalPages: Int = 0,
    @SerializedName("first") val first: Boolean = true,
    @SerializedName("last") val last: Boolean = true
) {
    fun toDomain(content: List<Deck>): DeckExplorePage {
        return DeckExplorePage(
            content = content,
            currentPage = currentPage,
            pageSize = pageSize,
            totalElements = totalElements,
            totalPages = totalPages,
            first = first,
            last = last
        )
    }
}
