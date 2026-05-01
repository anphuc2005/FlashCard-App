package com.example.flashcardapp.presentation.feature.statistics

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.max

class StatisticFormatter {
    private val numberFormat = NumberFormat.getNumberInstance(Locale.US)
    private val percentFormat = NumberFormat.getNumberInstance(Locale.US).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }

    fun formatNumber(value: Int): String = numberFormat.format(value)
    fun formatNumber(value: Long): String = numberFormat.format(value)

    fun formatPercent(value: Int): String = "${value.coerceIn(0, 100)}%"
    fun formatPercent(value: Double): String = "${percentFormat.format(value.coerceIn(0.0, 100.0))}%"

    fun formatMinutes(totalMinutes: Int): String {
        val normalized = max(0, totalMinutes)
        val hours = normalized / 60
        val minutes = normalized % 60
        return "${hours}h ${minutes}m"
    }
}
