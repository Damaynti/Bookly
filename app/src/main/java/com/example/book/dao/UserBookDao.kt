package com.example.book.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.book.data.AppSettings
import com.example.book.model.BookCollection
import com.example.book.data.UserBook
import kotlinx.coroutines.flow.Flow

@Dao
interface UserBookDao {
    @Query("SELECT * FROM user_books ORDER BY createdAt DESC")
    fun getAllBooks(): Flow<List<UserBook>>

    @Query("SELECT * FROM user_books WHERE isFavorite = 1")
    fun getFavoriteBooks(): Flow<List<UserBook>>

    @Query("SELECT * FROM user_books WHERE id = :bookId")
    suspend fun getBookById(bookId: String): UserBook?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: UserBook)

    @Delete
    suspend fun deleteBook(book: UserBook)

    @Update
    suspend fun updateBook(book: UserBook)

    @Query("DELETE FROM user_books")
    suspend fun deleteAllBooks()

    @Query("""
        SELECT * FROM user_books
        WHERE LOWER(title) LIKE '%' || LOWER(:query) || '%'
        OR LOWER(author) LIKE '%' || LOWER(:query) || '%'
        OR LOWER(genre) LIKE '%' || LOWER(:query) || '%'
        ORDER BY createdAt DESC
    """)
    fun searchBooks(query: String): Flow<List<UserBook>>

    // Новые методы для коллекций
    @Query("SELECT * FROM collections ORDER BY createdAt DESC")
    fun getAllCollections(): Flow<List<BookCollection>>

    @Query("SELECT * FROM collections WHERE id = :collectionId")
    suspend fun getCollectionById(collectionId: String): BookCollection?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollection(collection: BookCollection)

    @Delete
    suspend fun deleteCollection(collection: BookCollection)

    @Update
    suspend fun updateCollection(collection: BookCollection)

    @Query("DELETE FROM collections")
    suspend fun deleteAllCollections()

    // Методы для получения книг по ID
    @Query("SELECT * FROM user_books WHERE id IN (:bookIds)")
    suspend fun getBooksByIds(bookIds: List<String>): List<UserBook>

    // Методы для настроек
    @Query("SELECT * FROM app_settings WHERE id = 'default_settings'")
    fun getAppSettings(): Flow<AppSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppSettings(settings: AppSettings)

    @Update
    suspend fun updateAppSettings(settings: AppSettings)
}
