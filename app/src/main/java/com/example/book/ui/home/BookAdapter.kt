package com.example.book.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.book.R
import com.example.book.data.UserBook
import com.example.book.databinding.ItemBookGridBinding
import com.example.book.databinding.ItemBookListBinding


class BookAdapter(
    private val isGridLayout: Boolean,
    private val onBookClick: (UserBook) -> Unit,
    private val onFavoriteClick: (UserBook) -> Unit
) : ListAdapter<UserBook, RecyclerView.ViewHolder>(BookDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (isGridLayout) {
            val binding = ItemBookGridBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            GridBookViewHolder(binding)
        } else {
            val binding = ItemBookListBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
            ListBookViewHolder(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val book = getItem(position)
        when (holder) {
            is GridBookViewHolder -> holder.bind(book)
            is ListBookViewHolder -> holder.bind(book)
        }
    }

    inner class GridBookViewHolder(private val binding: ItemBookGridBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: UserBook) {
            binding.root.setOnClickListener { onBookClick(book) }

            Glide.with(binding.root)
                .load(book.coverImage)
                .centerCrop()
                .into(binding.bookCover)

            binding.bookTitle.text = book.title
            binding.bookAuthor.text = book.author

            binding.favoriteButton.setImageResource(
                if (book.isFavorite) R.drawable.ic_fav
                else R.drawable.ic_fav //red!!!!
            )

            binding.favoriteButton.setOnClickListener {
                onFavoriteClick(book)
            }
        }
    }

    inner class ListBookViewHolder(private val binding: ItemBookListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: UserBook) {
            binding.root.setOnClickListener { onBookClick(book) }

            Glide.with(binding.root)
                .load(book.coverImage)
                .centerCrop()
                .into(binding.bookCover)

            binding.bookTitle.text = book.title
            binding.bookAuthor.text = book.author
            binding.bookGenre.text = book.genre
            binding.userName.text = book.userName

            // Format date
            val formattedDate = (binding.root.context as? HomeFragment)?.formatDate(book.createdAt) ?: book.createdAt
            binding.createdAt.text = formattedDate

            binding.favoriteButton.setImageResource(
                if (book.isFavorite) R.drawable.ic_fav
                else R.drawable.ic_fav
            )

            binding.favoriteButton.setOnClickListener {
                onFavoriteClick(book)
            }
        }
    }
}

class BookDiffCallback : DiffUtil.ItemCallback<UserBook>() {
    override fun areItemsTheSame(oldItem: UserBook, newItem: UserBook): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UserBook, newItem: UserBook): Boolean {
        return oldItem == newItem
    }
}