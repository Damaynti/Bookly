package com.example.book.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserBookDao {
    @Query("SELECT * FROM user_books ORDER BY createdAt DESC")
    fun getAllBooks(): Flow<List<UserBook>>

    @Query("SELECT * FROM user_books WHERE isFavorite = 1")
    fun getFavoriteBooks(): Flow<List<UserBook>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: UserBook)

    @Delete
    suspend fun deleteBook(book: UserBook)

    @Update
    suspend fun updateBook(book: UserBook)

    @Query("""
        SELECT * FROM user_books
        WHERE LOWER(title) LIKE '%' || LOWER(:query) || '%'
        OR LOWER(author) LIKE '%' || LOWER(:query) || '%'
        OR LOWER(genre) LIKE '%' || LOWER(:query) || '%'
        ORDER BY createdAt DESC
    """)
    fun searchBooks(query: String): Flow<List<UserBook>>
}
