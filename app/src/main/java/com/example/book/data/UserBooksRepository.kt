// UserBooksRepository.kt
package com.example.book.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

class UserBooksRepository(context: Context) {

    private val dao = UserBookDatabase.getInstance(context).userBookDao()
    private val scope = CoroutineScope(Dispatchers.IO)
    private val TAG = "DATABASE_DEBUG"

    // --------- STATEFLOW ---------

    val books: StateFlow<List<UserBook>> =
        dao.getAllBooks().stateIn(scope, SharingStarted.Lazily, emptyList())

    val collections: StateFlow<List<BookCollection>> =
        dao.getAllCollections().stateIn(scope, SharingStarted.Lazily, emptyList())

    val appSettings: StateFlow<AppSettings?> =
        dao.getAppSettings().stateIn(scope, SharingStarted.Lazily, null)


    // --------- ИНИЦИАЛИЗАЦИЯ РЕПОЗИТОРИЯ ---------

    init {
        Log.d(TAG, "🔄 Инициализация UserBooksRepository...")

        scope.launch {
            try {
                val booksCount = dao.getAllBooks().first().size
                Log.d(TAG, "📚 Книг в базе: $booksCount")

                // Настройки по умолчанию
                if (dao.getAppSettings().first() == null) {
                    Log.d(TAG, "⚙️ Настройки не найдены. Создаём...")
                    dao.insertAppSettings(AppSettings())
                }

                // Демо-данные
                if (booksCount == 0) {
                    initializeSampleData()
                    Log.d(TAG, "📘 Демо-данные добавлены.")
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Ошибка при инициализации БД: ${e.message}", e)
            }
        }
    }


    // --------- МЕТОДЫ ДЛЯ КНИГ ---------

    fun getAllUserBooks(): List<UserBook> = try {
        runBlocking {
            dao.getAllBooks().first()
        }
    } catch (e: Exception) {
        Log.e(TAG, "❌ Ошибка получения книг: ${e.message}", e)
        emptyList()
    }

    fun saveUserBook(book: UserBook) {
        scope.launch {
            dao.insertBook(book)
            Log.d(TAG, "💾 Книга сохранена: ${book.title}")
        }
    }

    fun deleteUserBook(book: UserBook) {
        scope.launch {
            dao.deleteBook(book)
            Log.d(TAG, "🗑️ Книга удалена: ${book.title}")
        }
    }

    fun updateUserBook(book: UserBook) {
        scope.launch {
            dao.updateBook(book)
            Log.d(TAG, "✏️ Книга обновлена: ${book.title}")
        }
    }

    fun toggleFavorite(book: UserBook) {
        scope.launch {
            val updated = book.copy(isFavorite = !book.isFavorite)
            dao.updateBook(updated)
            Log.d(TAG, "⭐ Избранное изменено: ${book.title}")
        }
    }

    fun searchUserBooks(query: String): Flow<List<UserBook>> =
        if (query.isBlank()) dao.getAllBooks()
        else dao.searchBooks(query)

    fun getFavoriteBooks(): Flow<List<UserBook>> =
        dao.getFavoriteBooks()


    // ======================================================
    //                    КОЛЛЕКЦИИ
    // ======================================================
    fun insertCollection(collection: BookCollection) {
        scope.launch {
            dao.insertCollection(collection)
            Log.d(TAG, "📚 Коллекция добавлена: ${collection.title}")
        }
    }

    /** Создать коллекцию с авто-генерацией ID */
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
                coverImage = coverBase64.toString(),
                bookIds = emptyList(),
                createdAt = TODO()
            )

            dao.insertCollection(collection)
            Log.d(TAG, "📚 Создана коллекция: ${collection.title}")
        }
    }

    /** Обновить коллекцию (только если она существует) */
    fun updateCollection(collection: BookCollection) {
        scope.launch {
            val old = dao.getCollectionById(collection.id)
            if (old == null) {
                Log.e(TAG, "❌ Коллекция не найдена: ${collection.id}")
                return@launch
            }

            dao.updateCollection(collection)
            Log.d(TAG, "✏️ Коллекция обновлена: ${collection.title}")
        }
    }

    /** Удалить коллекцию */
    fun deleteCollection(collectionId: String) {
        scope.launch {
            val col = dao.getCollectionById(collectionId)
            if (col != null) {
                dao.deleteCollection(col)
                Log.d(TAG, "🗑️ Коллекция удалена: ${col.title}")
            } else {
                Log.e(TAG, "❌ Попытка удалить несуществующую коллекцию: $collectionId")
            }
        }
    }

    /** Получить коллекцию вместе со списком книг */
    suspend fun getCollectionWithBooks(
        collectionId: String
    ): Pair<BookCollection, List<UserBook>>? {
        return try {
            val col = dao.getCollectionById(collectionId) ?: return null
            val books = dao.getBooksByIds(col.bookIds)
            col to books
        } catch (e: Exception) {
            Log.e(TAG, "❌ Ошибка получения коллекции: ${e.message}")
            null
        }
    }

    /** Добавить книгу в коллекцию */
    fun addBookToCollection(bookId: String, collectionId: String) {
        scope.launch {
            val col = dao.getCollectionById(collectionId) ?: return@launch

            if (bookId in col.bookIds) {
                Log.d(TAG, "⚠️ Книга уже есть: $bookId")
                return@launch
            }

            val updated = col.copy(bookIds = col.bookIds + bookId)
            dao.updateCollection(updated)

            Log.d(TAG, "📖 Добавлена книга $bookId в '${col.title}'")
        }
    }

    /** Удалить книгу из коллекции */
    fun removeBookFromCollection(bookId: String, collectionId: String) {
        scope.launch {
            val col = dao.getCollectionById(collectionId) ?: return@launch

            if (bookId !in col.bookIds) {
                Log.d(TAG, "⚠️ Книги нет в коллекции: $bookId")
                return@launch
            }

            val updated = col.copy(bookIds = col.bookIds - bookId)
            dao.updateCollection(updated)

            Log.d(TAG, "📖 Книга удалена $bookId из '${col.title}'")
        }
    }

    /** Полностью заменить список книг коллекции */
    fun replaceBooksInCollection(collectionId: String, newBookIds: List<String>) {
        scope.launch {
            val col = dao.getCollectionById(collectionId) ?: return@launch
            val updated = col.copy(bookIds = newBookIds.distinct())
            dao.updateCollection(updated)

            Log.d(TAG, "🔄 Обновлён список книг коллекции '${col.title}'")
        }
    }

    /** Поиск коллекций */
    fun searchCollections(query: String): Flow<List<BookCollection>> =
        collections.map { list ->
            if (query.isBlank()) list
            else list.filter { it.title.contains(query, ignoreCase = true) }
        }


    // ======================================================
    //                      НАСТРОЙКИ
    // ======================================================

    fun updateAppSettings(settings: AppSettings) {
        scope.launch {
            dao.updateAppSettings(settings)
            Log.d(TAG, "⚙️ Настройки обновлены: ${settings.theme}")
        }
    }

    fun setTheme(theme: String) {
        scope.launch {
            val current = dao.getAppSettings().first() ?: AppSettings()
            dao.updateAppSettings(current.copy(theme = theme))
        }
    }


    // ======================================================
    //                 ДЕМО-ДАННЫЕ (BOOKS)
    // ======================================================

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
                    userId = "demo",
                    userName = "Анна",
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
                    createdAt = now,
                    userId = "demo",
                    userName = "Дмитрий"
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
                    userId = "demo",
                    userName = "Сергей",
                    isFavorite = true
                )
            )

            list.forEach { dao.insertBook(it) }
        }
    }
}
