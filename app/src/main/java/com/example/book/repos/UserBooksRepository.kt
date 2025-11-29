package com.example.book.repos

import android.content.Context
import android.util.Log
import com.example.book.data.AppDatabase
import com.example.book.data.AppSettings
import com.example.book.data.ExportedData
import com.example.book.model.BookCollection
import com.example.book.data.UserBook
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Date
import java.util.UUID

class UserBooksRepository(context: Context) {
    private val dao = AppDatabase.Companion.getInstance(context).userBookDao()
    private val TAG = "DATABASE_DEBUG"

    suspend fun getBookById(bookId: String): UserBook? {
        return dao.getBookById(bookId)
    }

    suspend fun updateBook(book: UserBook) {
        dao.updateBook(book)
    }

    suspend fun deleteBook(book: UserBook) {
        dao.deleteBook(book)
    }

    suspend fun getCollectionById(collectionId: String): BookCollection? {
        return dao.getCollectionById(collectionId)
    }

    suspend fun deleteAllData() {
        dao.deleteAllBooks()
        dao.deleteAllCollections()
    }

    suspend fun exportData(): String {
        val books = dao.getAllBooks().first()
        val collections = dao.getAllCollections().first()
        val exportedData = ExportedData(books, collections)
        return Gson().toJson(exportedData)
    }

    suspend fun importData(json: String) {
        val exportedData = Gson().fromJson(json, ExportedData::class.java)
        exportedData.books.forEach { dao.insertBook(it) }
        exportedData.collections.forEach { dao.insertCollection(it) }
    }

    fun saveUserBook(book: UserBook) {
        scope.launch {
            dao.insertBook(book)
            Log.d(TAG, "Книга сохранена: ${book.title}")
        }
    }
    fun toggleFavorite(book: UserBook) {
        scope.launch {
            val updated = book.copy(isFavorite = !book.isFavorite)
            dao.updateBook(updated)
            Log.d(TAG, "Избранное изменено: ${book.title}")
        }
    }

    fun searchUserBooks(query: String): Flow<List<UserBook>> =
        if (query.isBlank()) dao.getAllBooks()
        else dao.searchBooks(query)

    fun getFavoriteBooks(): Flow<List<UserBook>> =
        dao.getFavoriteBooks()


    private val scope = CoroutineScope(Dispatchers.IO)

    val books: StateFlow<List<UserBook>> =
        dao.getAllBooks().stateIn(scope, SharingStarted.Companion.Lazily, emptyList())


    val collections: StateFlow<List<BookCollection>> =
        dao.getAllCollections().stateIn(scope, SharingStarted.Companion.Lazily, emptyList())

    val appSettings: StateFlow<AppSettings?> =
        dao.getAppSettings().stateIn(scope, SharingStarted.Companion.Lazily, null)


    init {
        Log.d(TAG, "Инициализация UserBooksRepository")

        scope.launch {
            try {
                val booksCount = dao.getAllBooks().first().size
                Log.d(TAG, "Книг в базе: $booksCount")

                if (dao.getAppSettings().first() == null) {
                    Log.d(TAG, "Настройки не найдены. Создаём")
                    dao.insertAppSettings(AppSettings())
                }

//                if (booksCount == 0) {
//                    initializeSampleData()
//                    Log.d(TAG, "Демо-данные добавлены.")
//                }

            } catch (e: Exception) {
                Log.e(TAG, "Ошибка при инициализации БД: ${e.message}", e)
            }
        }
    }



    fun getAllUserBooks(): List<UserBook> = try {
        runBlocking {
            dao.getAllBooks().first()
        }
    } catch (e: Exception) {
        Log.e(TAG, "Ошибка получения книг: ${e.message}", e)
        emptyList()
    }



    fun deleteUserBook(book: UserBook) {
        scope.launch {
            dao.deleteBook(book)
            Log.d(TAG, "Книга удалена: ${book.title}")
        }
    }

    fun updateUserBook(book: UserBook) {
        scope.launch {
            dao.updateBook(book)
            Log.d(TAG, "Книга обновлена: ${book.title}")
        }
    }



    fun insertCollection(collection: BookCollection) {
        scope.launch {
            dao.insertCollection(collection)
            Log.d(TAG, "Коллекция добавлена: ${collection.title}")
        }
    }

    fun createCollection(
        title: String,
        description: String = "",
        coverBase64: String? = null
    ) {
        scope.launch {
            val collection = BookCollection(
                id = UUID.randomUUID().toString(),
                title = title,
                description = description,
                coverImage = coverBase64 ?: "",
                bookIds = emptyList(),
                createdAt = Date().toString()
            )

            dao.insertCollection(collection)
            Log.d(TAG, "Создана коллекция: ${collection.title}")
        }
    }

    fun updateCollection(collection: BookCollection) {
        scope.launch {
            val old = dao.getCollectionById(collection.id)
            if (old == null) {
                Log.e(TAG, "Коллекция не найдена: ${collection.id}")
                return@launch
            }

            dao.updateCollection(collection)
            Log.d(TAG, "Коллекция обновлена: ${collection.title}")
        }
    }

    fun deleteCollection(collectionId: String) {
        scope.launch {
            val col = dao.getCollectionById(collectionId)
            if (col != null) {
                dao.deleteCollection(col)
                Log.d(TAG, "Коллекция удалена: ${col.title}")
            } else {
                Log.e(TAG, "Попытка удалить несуществующую коллекцию: $collectionId")
            }
        }
    }

    suspend fun getCollectionWithBooks(
        collectionId: String
    ): Pair<BookCollection, List<UserBook>>? {
        return try {
            val col = dao.getCollectionById(collectionId) ?: return null
            val books = dao.getBooksByIds(col.bookIds)
            col to books
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка получения коллекции: ${e.message}")
            null
        }
    }

    fun addBooksToCollection(collectionId: String, bookIdsToAdd: List<String>) {
        scope.launch {
            val collection = dao.getCollectionById(collectionId) ?: return@launch
            val currentBookIds = collection.bookIds.toMutableSet()
            currentBookIds.addAll(bookIdsToAdd)
            val updatedCollection = collection.copy(bookIds = currentBookIds.toList())
            dao.updateCollection(updatedCollection)
            Log.d(TAG, "Добавлены книги в '${collection.title}'")
        }
    }

    fun removeBookFromCollection(bookId: String, collectionId: String) {
        scope.launch {
            val col = dao.getCollectionById(collectionId) ?: return@launch

            if (bookId !in col.bookIds) {
                Log.d(TAG, "Книги нет в коллекции: $bookId")
                return@launch
            }

            val updated = col.copy(bookIds = col.bookIds - bookId)
            dao.updateCollection(updated)

            Log.d(TAG, "Книга удалена $bookId из '${col.title}'")
        }
    }

    fun searchCollections(query: String): Flow<List<BookCollection>> =
        collections.map { list ->
            if (query.isBlank()) list
            else list.filter { it.title.contains(query, ignoreCase = true) }
        }


    fun updateAppSettings(settings: AppSettings) {
        scope.launch {
            dao.updateAppSettings(settings)
            Log.d(TAG, "Настройки обновлены: ${settings.theme}")
        }
    }

    fun setTheme(theme: String) {
        scope.launch {
            val current = dao.getAppSettings().first() ?: AppSettings()
            dao.updateAppSettings(current.copy(theme = theme))
        }
    }



    fun initializeSampleData() {
        scope.launch {
            if (dao.getAllBooks().first().isNotEmpty()) return@launch

            val now = Date().toString()

            val list = listOf(
                UserBook(
                    id = "1",
                    title = "Мастер и Маргарита",
                    author = "М. Булгаков",
                    genre = "Классика",
                    summary = "Философский роман о добре и зле.",
                    coverImage = "book",
                    rating = 5,
                    createdAt = now,
                    isFavorite = true
                ),
                UserBook(
                    id = "2",
                    title = "Властелин колец",
                    author = "Дж. Толкин",
                    genre = "Фэнтези",
                    summary = "Эпическая сага.",
                    coverImage = "book",
                    rating = 5,
                    createdAt = now
                ),
                UserBook(
                    id = "3",
                    title = "1984",
                    author = "Дж. Оруэлл",
                    genre = "Антиутопия",
                    summary = "Роман о тоталитаризме.",
                    coverImage = "book",
                    rating = 4,
                    createdAt = now,
                    isFavorite = true
                )
            )

            list.forEach { dao.insertBook(it) }
        }
    }
}
