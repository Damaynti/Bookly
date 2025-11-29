package com.example.book.viemodel.fav

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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
            loadImageFromBase64(book.coverImage, binding.bookCover)

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

    private fun loadImageFromBase64(coverImage: String, imageView: ImageView) {
        if (coverImage.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(coverImage, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                // В случае ошибки используем fallback изображение
                imageView.setImageResource(R.drawable.book)
            }
        } else {
            // Если изображение пустое, используем fallback
            imageView.setImageResource(R.drawable.book)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<UserBook>() {
        override fun areItemsTheSame(oldItem: UserBook, newItem: UserBook): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: UserBook, newItem: UserBook): Boolean = oldItem == newItem
    }
}
