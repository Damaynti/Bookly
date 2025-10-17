package com.example.book.ui.prof

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ProfViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is prof Fragment"
    }
    val text: LiveData<String> = _text
}