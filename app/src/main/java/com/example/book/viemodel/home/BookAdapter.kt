package com.example.book.viemodel.home

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.book.R
import com.example.book.data.UserBook
import com.example.book.databinding.ItemBookGridBinding
import com.example.book.databinding.ItemBookListBinding
import com.example.book.utils.formatDate

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

            // Исправленная загрузка изображений - как в первом примере
            loadImageFromBase64(book.coverImage, binding.bookCover)

            binding.bookTitle.text = book.title
            binding.bookAuthor.text = book.author

            binding.favoriteButton.setImageResource(
                if (book.isFavorite) R.drawable.ic_fav
                else R.drawable.ic_fav_red
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

            // Исправленная загрузка изображений - как в первом примере
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
                // В случае ошибки используем fallback изображение
                imageView.setImageResource(R.drawable.book)
            }
        } else {
            // Если изображение пустое, используем fallback
            imageView.setImageResource(R.drawable.book)
        }
    }

    // Оставляем старый метод как альтернативный вариант (можно удалить если не нужен)
    @SuppressLint("DiscouragedApi")
    private fun loadImage(coverImage: String, imageView: ImageView) {
        val context = imageView.context
        val resId = context.resources.getIdentifier(coverImage, "drawable", context.packageName)

        if (resId != 0) {
            Glide.with(context)
                .load(resId)
                .centerCrop()
                .placeholder(R.drawable.man)
                .error(R.drawable.dragon)
                .into(imageView)
        } else {
            imageView.setImageResource(R.drawable.book)
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
}