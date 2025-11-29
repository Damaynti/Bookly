package com.example.book.viemodel.prof

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.book.repos.UserBooksRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfViewModel(private val repository: UserBooksRepository) : ViewModel() {

    val theme: StateFlow<String> = repository.appSettings
        .map { it?.theme ?: "system" }
        .stateIn(viewModelScope, SharingStarted.Lazily, "system")

    fun deleteAllData() {
        viewModelScope.launch {
            repository.deleteAllData()
        }
    }

    suspend fun getExportData(): String {
        return repository.exportData()
    }

    fun importData(json: String) {
        viewModelScope.launch {
            repository.importData(json)
        }
    }

    fun setTheme(theme: String) {
        viewModelScope.launch {
            repository.setTheme(theme)
        }
    }
}
