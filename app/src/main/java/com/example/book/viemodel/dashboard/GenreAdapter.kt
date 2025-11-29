package com.example.book.viemodel.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.book.databinding.ItemGenreBinding

class GenreAdapter(
    private val onGenreClick: (String) -> Unit
) : RecyclerView.Adapter<GenreAdapter.GenreViewHolder>() {

    private val genres = mutableListOf<GenreItem>()

    fun submitList(list: List<GenreItem>) {
        genres.clear()
        genres.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GenreViewHolder {
        val binding = ItemGenreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GenreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GenreViewHolder, position: Int) {
        holder.bind(genres[position])
    }

    override fun getItemCount() = genres.size

    inner class GenreViewHolder(private val binding: ItemGenreBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(genre: GenreItem) {
            binding.genreName.text = genre.name
            binding.genreCount.text = "${genre.count} книг"

            // Устанавливаем картинку
            binding.genreImage.setImageResource(genre.iconRes)

            binding.root.setOnClickListener { onGenreClick(genre.name) }
        }
    }

}
