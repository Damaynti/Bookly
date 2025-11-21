package com.example.book.data

enum class ThemeType(val value: String) {
    LIGHT("Theme"),
    DARK("darkTheme");

    companion object {
        fun fromValue(value: String): ThemeType {
            return when (value) {
                "Theme" -> LIGHT
                "darkTheme" -> DARK
                else -> LIGHT// значение по умолчанию
            }
        }
    }
}