package com.example.book.viemodel.AddBook

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.book.repos.UserBooksRepository

class AddBookViewModelFactory(private val repository: UserBooksRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddBookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AddBookViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
