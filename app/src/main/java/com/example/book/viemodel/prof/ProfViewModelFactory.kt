package com.example.book.viemodel.prof

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.book.repos.UserBooksRepository

class ProfViewModelFactory(private val repository: UserBooksRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProfViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
