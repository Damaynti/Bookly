package com.example.book.viemodel.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.book.model.BookCollection
import com.example.book.repos.UserBooksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class NotificationsViewModel(private val repository: UserBooksRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val collections: StateFlow<List<BookCollection>> = repository.collections
        .combine(_searchQuery) { collections, query ->
            if (query.isBlank()) {
                collections
            } else {
                collections.filter { it.title.contains(query, ignoreCase = true) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    fun search(query: String) {
        _searchQuery.value = query
    }
}
