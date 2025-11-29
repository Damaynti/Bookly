package com.example.book.viemodel.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.book.data.UserBook
import com.example.book.repos.UserBooksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AddBooksToCollectionViewModel(
    private val repository: UserBooksRepository,
    private val collectionId: String
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedBookIds = MutableStateFlow(emptySet<String>())
    val selectedBookIds: StateFlow<Set<String>> = _selectedBookIds

    val books: StateFlow<List<UserBook>> = repository.books
        .combine(_searchQuery) { books, query ->
            if (query.isBlank()) {
                books
            } else {
                books.filter { it.title.contains(query, ignoreCase = true) || it.author.contains(query, ignoreCase = true) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = emptyList()
        )

    fun onBookSelected(book: UserBook, isSelected: Boolean) {
        val currentSelected = _selectedBookIds.value.toMutableSet()
        if (isSelected) {
            currentSelected.add(book.id)
        } else {
            currentSelected.remove(book.id)
        }
        _selectedBookIds.value = currentSelected
    }

    fun addSelectedBooksToCollection() {
        viewModelScope.launch {
            repository.addBooksToCollection(collectionId, _selectedBookIds.value.toList())
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
