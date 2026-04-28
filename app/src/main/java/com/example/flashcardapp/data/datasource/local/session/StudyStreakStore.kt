package com.example.flashcardapp.data.datasource.local.session

import android.content.Context
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object StudyStreakStore {
    private const val STREAK_PREFS = "study_streak_prefs"
    private const val KEY_CURRENT_STREAK = "current_streak"
    private const val KEY_BEST_STREAK = "best_streak"
    private const val KEY_LAST_STUDY_DATE = "last_study_date"

    data class Snapshot(
        val currentStreak: Int,
        val bestStreak: Int,
        val lastStudyDate: LocalDate?
    )

    fun getSnapshot(context: Context): Snapshot {
        val prefs = context.getSharedPreferences(STREAK_PREFS, Context.MODE_PRIVATE)
        val current = prefs.getInt(KEY_CURRENT_STREAK, 0).coerceAtLeast(0)
        val best = prefs.getInt(KEY_BEST_STREAK, 0).coerceAtLeast(0)
        val lastDate = prefs.getString(KEY_LAST_STUDY_DATE, null)
            ?.let { runCatching { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) }.getOrNull() }
        return Snapshot(currentStreak = current, bestStreak = best, lastStudyDate = lastDate)
    }

    fun hasStudiedToday(context: Context): Boolean {
        val today = LocalDate.now()
        return getSnapshot(context).lastStudyDate == today
    }

    fun recordStudyEvent(context: Context, studiedAt: String) {
        val prefs = context.getSharedPreferences(STREAK_PREFS, Context.MODE_PRIVATE)
        val snapshot = getSnapshot(context)
        val studyDate = parseToLocalDate(studiedAt)
        val today = LocalDate.now()
        val effectiveDate = if (studyDate.isAfter(today)) today else studyDate

        val newCurrent = when {
            snapshot.lastStudyDate == null -> 1
            snapshot.lastStudyDate == effectiveDate -> snapshot.currentStreak
            snapshot.lastStudyDate == effectiveDate.minusDays(1) -> snapshot.currentStreak + 1
            else -> 1
        }
        val newBest = maxOf(snapshot.bestStreak, newCurrent)

        prefs.edit()
            .putInt(KEY_CURRENT_STREAK, newCurrent)
            .putInt(KEY_BEST_STREAK, newBest)
            .putString(KEY_LAST_STUDY_DATE, effectiveDate.toString())
            .apply()
    }

    private fun parseToLocalDate(raw: String): LocalDate {
        return runCatching {
            Instant.parse(raw)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }.getOrElse {
            LocalDate.now()
        }
    }
}
