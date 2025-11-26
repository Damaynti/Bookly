package com.example.book.viemodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.book.data.UserBook
import com.example.book.repos.UserBooksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: UserBooksRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Подписываемся на StateFlow из репозитория
    val books: StateFlow<List<UserBook>> = repository.books

    private val _searchResults = MutableStateFlow<List<UserBook>>(emptyList())
    val searchResults: StateFlow<List<UserBook>> = _searchResults.asStateFlow()

    fun setSearchQuery(query: String) {
        _searchQuery.value = query

        viewModelScope.launch {
            _searchResults.value = if (query.isBlank()) {
                repository.books.value // текущий список всех книг
            } else {
                repository.searchUserBooks(query).first() // берём первый (текущий) список из Flow
            }
        }
    }


    fun toggleFavorite(book: UserBook) {
        viewModelScope.launch {
            repository.toggleFavorite(book)
            // Здесь не нужно явно обновлять _books, т.к. репозиторий уже обновляет StateFlow
        }
    }

    fun getBookCount(): Int = books.value.size
}
