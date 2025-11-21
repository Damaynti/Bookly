package com.example.book.utils

import java.text.SimpleDateFormat
import java.util.*

fun formatDate(dateString: String): String {
    val formats = listOf(
        "EEE MMM dd HH:mm:ss z yyyy",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd HH:mm:ss",
        "yyyy-MM-dd"
    )

    for (pattern in formats) {
        try {
            val inputFormat = SimpleDateFormat(pattern, Locale.ENGLISH)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(dateString)
            if (date != null) {
                val outputFormat = SimpleDateFormat("dd.MM.yyyy", Locale("ru"))
                return outputFormat.format(date)
            }
        } catch (_: Exception) { }
    }
    return dateString
}
