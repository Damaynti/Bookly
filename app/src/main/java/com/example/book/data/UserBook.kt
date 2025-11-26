package com.example.book.data
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "user_books")
data class UserBook(
    @PrimaryKey val id: String,
    val title: String,
    val author: String,
    val genre: String,
    val summary: String,
    val coverImage: String,
    val rating: Int,
    val createdAt: String,
    val isFavorite: Boolean = false
)
