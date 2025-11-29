package com.example.book.viemodel.dashboard

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.book.R
import com.example.book.data.UserBook
import com.example.book.databinding.ItemBookListBinding
import com.example.book.utils.formatDate

class BooksByGenreAdapter(
    private val onBookClick: (UserBook) -> Unit,
    private val onFavoriteClick: (UserBook) -> Unit
) : RecyclerView.Adapter<BooksByGenreAdapter.BookViewHolder>() {

    private val books = mutableListOf<UserBook>()

    fun submitList(list: List<UserBook>) {
        books.clear()
        books.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val binding = ItemBookListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount() = books.size

    inner class BookViewHolder(private val binding: ItemBookListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: UserBook) {
            binding.root.setOnClickListener { onBookClick(book) }

            loadImageFromBase64(book.coverImage, binding.bookCover)

            binding.bookTitle.text = book.title
            binding.bookAuthor.text = book.author
            binding.bookGenre.text = book.genre

            // Format date
            val formattedDate = formatDate(book.createdAt)
            binding.createdAt.text = formattedDate

            binding.favoriteButton.setImageResource(
                if (book.isFavorite) R.drawable.ic_fav_red
                else R.drawable.ic_fav
            )

            binding.favoriteButton.setOnClickListener {
                onFavoriteClick(book)
            }
        }
    }

    private fun loadImageFromBase64(coverImage: String, imageView: ImageView) {
        if (coverImage.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(coverImage, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                imageView.setImageResource(R.drawable.book)
            }
        } else {
            imageView.setImageResource(R.drawable.book)
        }
    }
}
