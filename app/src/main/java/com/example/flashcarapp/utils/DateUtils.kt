package com.example.flashcarapp.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateUtils {

    private val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun getCurrentTimeStamp(): String {
        return simpleDateFormat.format(Date())
    }

    fun formatDate(timestamp: String): String {
        return try {
            val date = simpleDateFormat.parse(timestamp)
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date!!)
        } catch (e: Exception) {
            timestamp
        }
    }
}

