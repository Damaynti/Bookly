
package com.example.book.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: String = "default_settings",
    val theme: String = "Theme",
    val syncEnabled: Boolean = true,
    val notificationsEnabled: Boolean = true
)