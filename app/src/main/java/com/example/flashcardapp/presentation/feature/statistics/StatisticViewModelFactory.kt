package com.example.flashcardapp.presentation.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.flashcardapp.data.repository.StatisticsRepository

class StatisticViewModelFactory(
    private val statisticsRepository: StatisticsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StatisticViewModel::class.java)) {
            return StatisticViewModel(statisticsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
