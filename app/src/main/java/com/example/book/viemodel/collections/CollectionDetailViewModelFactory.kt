package com.example.book.viemodel.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.book.repos.UserBooksRepository

class CollectionDetailViewModelFactory(
    private val repository: UserBooksRepository,
    private val collectionId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CollectionDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CollectionDetailViewModel(repository, collectionId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
