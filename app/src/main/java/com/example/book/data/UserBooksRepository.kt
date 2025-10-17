package com.example.book.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class UserBooksRepository(context: Context) {

    private val dao = UserBookDatabase.getInstance(context).userBookDao()
    private val scope = CoroutineScope(Dispatchers.IO)

    // StateFlow для Compose
    val books: StateFlow<List<UserBook>> = dao.getAllBooks()
        .stateIn(scope, SharingStarted.Lazily, emptyList())

    fun saveUserBook(book: UserBook) {
        scope.launch { dao.insertBook(book) }
    }

    fun deleteUserBook(book: UserBook) {
        scope.launch { dao.deleteBook(book) }
    }

    fun updateUserBook(book: UserBook) {
        scope.launch { dao.updateBook(book) }
    }

    fun toggleFavorite(book: UserBook) {
        scope.launch {
            val updated = book.copy(isFavorite = !book.isFavorite)
            dao.updateBook(updated)
        }
    }

    fun searchUserBooks(query: String): Flow<List<UserBook>> {
        return if (query.isBlank()) dao.getAllBooks()
        else dao.searchBooks(query)
    }

    fun initializeSampleData() {
        scope.launch {
            val currentBooks = dao.getAllBooks().first()
            if (currentBooks.isEmpty()) {
                val now = Date()
                val sampleBooks = listOf(
                    UserBook(
                        id = "1",
                        title = "Мастер и Маргарита",
                        author = "М. Булгаков",
                        genre = "Классика",
                        summary = "Философский роман о добре и зле.",
                        coverImage = "https://images.unsplash.com/photo-1755188977089-3bb40306d57f",
                        rating = 5,
                        createdAt = now.toString(),
                        userId = "demo",
                        userName = "Анна",
                        isFavorite = true
                    ),
                    UserBook(
                        id = "2",
                        title = "Властелин колец",
                        author = "Дж. Р. Р. Толкин",
                        genre = "Фэнтези",
                        summary = "Эпическая сага о кольце.",
                        coverImage = "https://images.unsplash.com/photo-1667477603004-00852d582d4a",
                        rating = 5,
                        createdAt = now.toString(),
                        userId = "demo",
                        userName = "Дмитрий"
                    )
                )
                sampleBooks.forEach { dao.insertBook(it) }
            }
        }
    }
}
