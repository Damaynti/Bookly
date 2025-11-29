package com.example.book.viemodel.collections

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.book.R
import com.example.book.data.UserBook
import com.example.book.databinding.ItemBookSelectionBinding

class BookSelectionAdapter(
    private var books: List<UserBook>,
    private val onBookSelected: (UserBook, Boolean) -> Unit
) : RecyclerView.Adapter<BookSelectionAdapter.BookViewHolder>() {

    private val selectedBooks = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount() = books.size

    fun updateBooks(newBooks: List<UserBook>) {
        books = newBooks
        notifyDataSetChanged()
    }

    fun getSelectedBookIds(): List<String> = selectedBooks.toList()

    inner class BookViewHolder(private val binding: ItemBookSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: UserBook) {
            binding.bookTitle.text = book.title
            binding.bookAuthor.text = book.author

            if (book.coverImage.isNotEmpty()) {
                try {
                    val imageBytes = Base64.decode(book.coverImage, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    binding.bookCover.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    binding.bookCover.setImageResource(R.drawable.book)
                }
            } else {
                binding.bookCover.setImageResource(R.drawable.book)
            }

            binding.checkbox.isChecked = selectedBooks.contains(book.id)

            binding.root.setOnClickListener {
                binding.checkbox.isChecked = !binding.checkbox.isChecked
            }

            binding.checkbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedBooks.add(book.id)
                } else {
                    selectedBooks.remove(book.id)
                }
                onBookSelected(book, isChecked)
            }
        }
    }
}
