package com.example.book.viemodel.collections

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.book.repos.UserBooksRepository

class CreateCollectionViewModelFactory(
    private val repository: UserBooksRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateCollectionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CreateCollectionViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
