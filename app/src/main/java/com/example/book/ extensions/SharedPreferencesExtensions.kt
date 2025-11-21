// SharedPreferencesExtensions.kt
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

// Функции для работы с темой
fun Context.isDarkThemeEnabled(): Boolean {
    return getSharedPreferences("settings", Context.MODE_PRIVATE)
        .getBoolean("darkTheme", false)
}

fun Context.setDarkThemeEnabled(isDark: Boolean) {
    getSharedPreferences("settings", Context.MODE_PRIVATE)
        .edit()
        .putBoolean("darkTheme", isDark)
        .apply()
}

// Функция для применения темы к Activity
fun AppCompatActivity.applySavedTheme() {
    val isDark = isDarkThemeEnabled()
    AppCompatDelegate.setDefaultNightMode(
        if (isDark) AppCompatDelegate.MODE_NIGHT_YES
        else AppCompatDelegate.MODE_NIGHT_NO
    )
}