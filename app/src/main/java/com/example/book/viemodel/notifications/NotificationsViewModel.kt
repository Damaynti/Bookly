package com.example.book.viemodel.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.book.model.BookCollection

class NotificationsViewModel : ViewModel() {

    private val _collections = MutableLiveData<List<BookCollection>>(emptyList())
    val collections: LiveData<List<BookCollection>> = _collections

    /**
     * Добавление новой коллекции пользователем
     */
    fun addCollection(collection: BookCollection) {
        val currentList = _collections.value ?: emptyList()
        _collections.value = currentList + collection
    }

    /**
     * Удаление коллекции
     */
    fun removeCollection(collection: BookCollection) {
        val currentList = _collections.value ?: emptyList()
        _collections.value = currentList - collection
    }

    /**
     * Поиск по существующим коллекциям
     */
    fun search(query: String) {
        val currentList = _collections.value ?: emptyList()
        _collections.value = if (query.isEmpty()) {
            currentList
        } else {
            currentList.filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.description.contains(query, ignoreCase = true)
            }
        }
    }
}
