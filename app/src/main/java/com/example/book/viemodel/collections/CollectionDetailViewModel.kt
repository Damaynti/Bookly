package com.example.book.viemodel.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.book.data.UserBook
import com.example.book.model.BookCollection
import com.example.book.repos.UserBooksRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CollectionDetailUiState(
    val collection: BookCollection? = null,
    val books: List<UserBook> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class CollectionDetailViewModel(
    private val repository: UserBooksRepository,
    private val collectionId: String
) : ViewModel() {

    val uiState: StateFlow<CollectionDetailUiState> = repository.collections
        .combine(repository.books) { collections, books ->
            val collection = collections.find { it.id == collectionId }
            if (collection != null) {
                val collectionBooks = books.filter { it.id in collection.bookIds }
                CollectionDetailUiState(collection = collection, books = collectionBooks, isLoading = false)
            } else {
                CollectionDetailUiState(isLoading = false, error = "Подборка не найдена")
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = CollectionDetailUiState()
        )

    fun deleteCollection() {
        viewModelScope.launch {
            repository.deleteCollection(collectionId)
        }
    }

    fun removeBookFromCollection(bookId: String) {
        viewModelScope.launch {
            repository.removeBookFromCollection(bookId, collectionId)
        }
    }
}
