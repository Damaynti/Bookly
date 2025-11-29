package com.example.book.viemodel.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.book.R
import com.example.book.data.UserBook
import com.example.book.repos.UserBooksRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class GenreItem(val name: String, val count: Int, val iconRes: Int)

class GenresViewModel(private val repository: UserBooksRepository) : ViewModel() {

    private val _selectedGenre = MutableStateFlow<String?>(null)
    val selectedGenre: StateFlow<String?> = _selectedGenre

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val isSearching: StateFlow<Boolean> = _searchQuery
        .map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.Lazily, false)

    val genreIcons = mapOf(
        "Классика" to R.drawable.scroll,
        "Проза" to R.drawable.notes,
        "Фантастика" to R.drawable.ufo,
        "Детектив" to R.drawable.privatedetective,
        "Романтика" to R.drawable.lovebooks,
        "Триллер" to R.drawable.trill,
        "Фэнтези" to R.drawable.dragon,
        "Биография" to R.drawable.man,
        "Бизнес" to R.drawable.briefcase,
        "Приключения" to R.drawable.location,
        "Поэзия" to R.drawable.fountainpen,
        "Ужасы" to R.drawable.trill,
        "Научная фантастика" to R.drawable.science_fiction,
        "История" to R.drawable.history,
        "Психология" to R.drawable.psychology,
        "Саморазвитие" to R.drawable.self_development,
        "Драма" to R.drawable.drama,
        "Юмор" to R.drawable.humor
    )

    val allGenres = genreIcons.keys.toList()

    val genres: StateFlow<List<GenreItem>> = repository.books
        .combine(_searchQuery) { books, query ->
            val genresToShow = if (query.isBlank()) {
                allGenres
            } else {
                allGenres.filter { it.contains(query, ignoreCase = true) }
            }
            genresToShow.map { genre ->
                GenreItem(
                    name = genre,
                    count = books.count { it.genre.equals(genre, ignoreCase = true) },
                    iconRes = genreIcons[genre] ?: R.drawable.book // Fallback icon
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val filteredBooks: StateFlow<List<UserBook>> = repository.books
        .combine(_selectedGenre) { books, genre ->
            if (genre != null) {
                books.filter { it.genre.equals(genre, ignoreCase = true) }
            } else {
                emptyList()
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectGenre(genre: String?) {
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
