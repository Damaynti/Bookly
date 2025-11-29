package com.example.book.viemodel.AddBook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.book.data.UserBook
import com.example.book.repos.UserBooksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddBookViewModel(private val repository: UserBooksRepository) : ViewModel() {

    private val _book = MutableStateFlow<UserBook?>(null)
    val book: StateFlow<UserBook?> = _book.asStateFlow()

    fun loadBook(bookId: String) {
        viewModelScope.launch {
            _book.value = repository.getBookById(bookId)
        }
    }

    fun saveBook(title: String, author: String, genre: String, summary: String, coverImage: String?) {
        viewModelScope.launch {
            val existingBook = _book.value
            if (existingBook == null) {
                val newBook = UserBook(
                    id = System.currentTimeMillis().toString(),
                    title = title,
                    author = author,
                    genre = genre,
                    summary = summary,
                    coverImage = coverImage ?: "",
                    rating = 0,
                    createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                    isFavorite = false
                )
                repository.saveUserBook(newBook)
            } else {
                val updatedBook = existingBook.copy(
                    title = title,
                    author = author,
                    genre = genre,
                    summary = summary,
                    coverImage = coverImage ?: existingBook.coverImage
                )
                repository.updateBook(updatedBook)
            }
        }
    }
}
