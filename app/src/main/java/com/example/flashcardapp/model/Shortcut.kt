package com.example.flashcardapp.model

import androidx.annotation.DrawableRes
import androidx.annotation.ColorRes

@Suppress("UnusedReceiverParameter")
data class Shortcut(
    val id: String,
    val title: String,
    @DrawableRes val iconResId: Int,
    @ColorRes val backgroundResId: Int? = null,
    val action: String? = null
)

