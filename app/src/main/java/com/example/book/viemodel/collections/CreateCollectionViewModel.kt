package com.example.book.viemodel.collections

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.book.R
import com.example.book.model.BookCollection
import com.example.book.repos.UserBooksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Date
import java.util.UUID

class CreateCollectionViewModel(private val repository: UserBooksRepository, private val context: Context) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState

    private val _collection = MutableStateFlow<BookCollection?>(null)
    val collection: StateFlow<BookCollection?> = _collection

    fun loadCollection(collectionId: String) {
        viewModelScope.launch {
            _collection.value = repository.getCollectionById(collectionId)
        }
    }

    fun saveCollection(title: String, description: String, coverImageBase64: String?) {
        if (title.isBlank()) {
            _uiState.value = UiState.Error("Название не может быть пустым")
            return
        }

        _uiState.value = UiState.Loading

        viewModelScope.launch {
            try {
                val existingCollection = _collection.value
                if (existingCollection == null) {
                    val finalCoverImage = coverImageBase64 ?: getDefaultCoverImage()
                    repository.createCollection(
                        title = title,
                        description = description,
                        coverBase64 = finalCoverImage
                    )
                } else {
                    val finalCoverImage = coverImageBase64 ?: existingCollection.coverImage
                    val updatedCollection = existingCollection.copy(
                        title = title,
                        description = description,
                        coverImage = finalCoverImage
                    )
                    repository.updateCollection(updatedCollection)
                }
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Не удалось сохранить подборку: ${e.message}")
            }
        }
    }

    private fun getDefaultCoverImage(): String {
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.books)
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    sealed class UiState {
        object Idle : UiState()
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }
}
