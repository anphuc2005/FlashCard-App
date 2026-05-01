package com.example.flashcardapp.presentation.feature.statistics.model

// Model hiển thị cho một thành tích trong UI.

import androidx.annotation.DrawableRes
import java.io.Serializable

data class StatisticAchievementItem(
    val title: String,
    val description: String,
    val introduction: String,
    val condition: String,
    @field:DrawableRes val iconResId: Int,
    val isUnlocked: Boolean,
    val progressPercent: Int = 0
) : Serializable
