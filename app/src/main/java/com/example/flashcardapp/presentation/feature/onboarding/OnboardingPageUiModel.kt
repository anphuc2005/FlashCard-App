package com.example.flashcardapp.presentation.feature.onboarding

import androidx.annotation.DrawableRes

data class OnboardingPageUiModel(
    @DrawableRes val iconResId: Int,
    val title: String,
    val description: String,
    val buttonText: String
)

