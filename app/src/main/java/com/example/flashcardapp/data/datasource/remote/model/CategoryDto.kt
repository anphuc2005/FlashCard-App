package com.example.flashcardapp.data.datasource.remote.model

import com.example.flashcardapp.domain.model.Category
import com.google.gson.annotations.SerializedName

data class CategoryDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String? = null
) {
    fun toDomain(): Category {
        return Category(
            id = id,
            name = name,
            description = description
        )
    }
}

fun Category.toDto(): CategoryDto {
    return CategoryDto(
        id = id,
        name = name,
        description = description
    )
}
