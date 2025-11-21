package com.example.book.ui.fav

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.book.data.UserBook
import com.example.book.databinding.ItemFavBookBinding

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

        fun bind(book: UserBook) = with(binding) {
            tvTitle.text = book.title
            tvAuthor.text = book.author
            tvGenre.text = book.genre

            // Загрузка обложки (если есть)
            Glide.with(root.context)
                .load(book.coverImage)
                .centerCrop()
                .into(ivCover)

            // Кнопка "Читать"
            btnRead.setOnClickListener {
                onReadClick(book)
            }

            // Сердце (удалить из избранного)
            btnFavorite.setOnClickListener {
                onFavoriteClick(book)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<UserBook>() {
        override fun areItemsTheSame(oldItem: UserBook, newItem: UserBook): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: UserBook, newItem: UserBook): Boolean = oldItem == newItem
    }
}
