package com.example.flashcardapp.presentation.feature.statistics

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max

class StatisticFormatter {
    private val numberFormat = NumberFormat.getNumberInstance(Locale.US)

    fun formatNumber(value: Int): String = numberFormat.format(value)

    fun formatPercent(value: Int): String = "${value.coerceIn(0, 100)}%"

    fun formatMinutes(totalMinutes: Int): String {
        val normalized = max(0, totalMinutes)
        val hours = normalized / 60
        val minutes = normalized % 60
        return "${hours}h ${minutes}m"
    }
}

