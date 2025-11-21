package com.example.book.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.book.data.UserBook
import com.example.book.data.UserBooksRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GenreItem(val name: String, val count: Int)

class GenresViewModel(private val repository: UserBooksRepository) : ViewModel() {

    private val _selectedGenre = MutableStateFlow<String?>(null)
    val selectedGenre: StateFlow<String?> = _selectedGenre

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val isSearching: StateFlow<Boolean> = _searchQuery
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val allGenres = listOf(
        "Классика", "Проза", "Фантастика", "Детектив",
        "Романтика", "Триллер", "Фэнтези", "Биография", "Бизнес",
        "Приключения", "Поэзия", "Ужасы"
    )

    val genres: StateFlow<List<GenreItem>> = repository.books
        .combine(_searchQuery) { books, query ->
            if (query.isBlank()) {
                allGenres.map { genre ->
                    GenreItem(
                        name = genre,
                        count = books.count { it.genre.equals(genre, ignoreCase = true) }
                    )
                }
            } else emptyList()
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filteredBooks: StateFlow<List<UserBook>> = repository.books
        .combine(_selectedGenre) { books, genre ->
            val query = _searchQuery.value
            when {
                query.isNotBlank() -> books.filter {
                    it.title.contains(query, true) || it.author.contains(query, true)
                }
                genre != null -> books.filter { it.genre.equals(genre, true) }
                else -> emptyList()
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectGenre(genre: String) {
        _selectedGenre.value = genre
    }

    fun toggleFavorite(book: UserBook) {
        viewModelScope.launch { repository.toggleFavorite(book) }
    }
}

class GenresViewModelFactory(private val repository: UserBooksRepository) :
    androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GenresViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GenresViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
