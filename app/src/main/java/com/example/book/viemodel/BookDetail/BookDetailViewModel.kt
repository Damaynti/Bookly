package com.example.book.viemodel.BookDetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.book.data.UserBook
import com.example.book.repos.UserBooksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BookDetailViewModel(private val repository: UserBooksRepository) : ViewModel() {

    private val _book = MutableStateFlow<UserBook?>(null)
    val book: StateFlow<UserBook?> = _book.asStateFlow()

    fun loadBook(bookId: String) {
        viewModelScope.launch {
            _book.value = repository.getBookById(bookId)
        }
    }

    fun toggleFavorite() {
        _book.value?.let { currentBook ->
            viewModelScope.launch {
                val updatedBook = currentBook.copy(isFavorite = !currentBook.isFavorite)
                repository.updateBook(updatedBook)
                _book.value = updatedBook
            }
        }
    }

    fun deleteBook() {
        _book.value?.let {
            viewModelScope.launch {
                repository.deleteBook(it)
            }
        }
    }
}
