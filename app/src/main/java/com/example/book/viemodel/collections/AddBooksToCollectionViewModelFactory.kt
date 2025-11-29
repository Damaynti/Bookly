package com.example.book.viemodel.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.book.repos.UserBooksRepository

class AddBooksToCollectionViewModelFactory(
    private val repository: UserBooksRepository,
    private val collectionId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddBooksToCollectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddBooksToCollectionViewModel(repository, collectionId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
