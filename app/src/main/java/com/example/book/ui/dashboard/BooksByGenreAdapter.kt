package com.example.book.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.book.R
import com.example.book.data.UserBook
import com.example.book.databinding.ItemBookGridBinding

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
        val binding = ItemBookGridBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        holder.bind(books[position])
    }

    override fun getItemCount() = books.size

    inner class BookViewHolder(private val binding: ItemBookGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: UserBook) {
            binding.bookTitle.text = book.title
            binding.bookAuthor.text = book.author

            Glide.with(binding.bookCover.context)
                .load(book.coverImage)
                .centerCrop()
                .placeholder(R.drawable.trill)
                .into(binding.bookCover)

            binding.favoriteButton.setImageResource(
                if (book.isFavorite) R.drawable.ic_fav else R.drawable.ic_fav_red
            )

            binding.favoriteButton.setOnClickListener { onFavoriteClick(book) }
            binding.root.setOnClickListener { onBookClick(book) }
        }
    }
}
