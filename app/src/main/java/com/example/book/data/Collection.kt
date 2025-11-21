
package com.example.book.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "collections")
data class BookCollection(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val coverImage: String,
    val createdAt: String,
    val bookIds: List<String> // Список ID книг в коллекции
)

// TypeConverter для списка книг
class StringListConverter {
    @TypeConverter
    fun fromString(value: String): List<String> {
        return value.split(",").filter { it.isNotEmpty() }
    }

    @TypeConverter
    fun toString(list: List<String>): String {
        return list.joinToString(",")
    }
}