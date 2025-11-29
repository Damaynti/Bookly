package com.example.book.viemodel.fav

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.book.R
import com.example.book.data.UserBook
import com.example.book.databinding.ItemFavBookBinding
import com.example.book.utils.formatDate

class FavAdapter(
    private val onFavoriteClick: (UserBook) -> Unit,
    private val onReadClick: (UserBook) -> Unit
) : ListAdapter<UserBook, FavAdapter.FavViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavViewHolder {
        val binding = ItemFavBookBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class FavViewHolder(private val binding: ItemFavBookBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(book: UserBook) {
            binding.bookTitle.text = book.title
            binding.bookAuthor.text = book.author
            binding.bookGenre.text = book.genre
            binding.createdAt.text = formatDate(book.createdAt)

            // Загрузка обложки (если есть)
            Glide.with(binding.root.context)
                .load(book.coverImage)
                .centerCrop()
                .placeholder(R.drawable.book)
                .error(R.drawable.book)
                .into(binding.bookCover)

            // Кнопка "Читать"
            binding.root.setOnClickListener {
                onReadClick(book)
            }

            // Сердце (удалить из избранного)
            binding.favoriteButton.setOnClickListener {
                onFavoriteClick(book)
            }
            binding.favoriteButton.setImageResource(R.drawable.ic_fav_red)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<UserBook>() {
        override fun areItemsTheSame(oldItem: UserBook, newItem: UserBook): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: UserBook, newItem: UserBook): Boolean = oldItem == newItem
    }
}
