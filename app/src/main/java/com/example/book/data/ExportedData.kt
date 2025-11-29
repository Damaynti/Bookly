package com.example.book.data

import com.example.book.model.BookCollection

data class ExportedData(
    val books: List<UserBook>,
    val collections: List<BookCollection>
)
