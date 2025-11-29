package com.example.book.viemodel.notifications

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.book.R
import com.example.book.databinding.ItemCollectionBinding
import com.example.book.model.BookCollection

class CollectionAdapter(
    private var collections: List<BookCollection>,
    private val onCollectionClick: (BookCollection) -> Unit
) : RecyclerView.Adapter<CollectionAdapter.CollectionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollectionViewHolder {
        val binding = ItemCollectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CollectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CollectionViewHolder, position: Int) {
        holder.bind(collections[position])
    }

    override fun getItemCount() = collections.size

    fun updateList(newList: List<BookCollection>) {
        collections = newList
        notifyDataSetChanged()
    }

    inner class CollectionViewHolder(private val binding: ItemCollectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(collection: BookCollection) {
            binding.collectionTitle.text = collection.title
            binding.collectionBookCount.text = "${collection.bookIds.size} книг"

            if (collection.coverImage.isNotEmpty()) {
                try {
                    val imageBytes = Base64.decode(collection.coverImage, Base64.DEFAULT)
                    val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    binding.collectionCover.setImageBitmap(bitmap)
                } catch (e: Exception) {
                    binding.collectionCover.setImageResource(R.drawable.cover_placeholder)
                }
            } else {
                binding.collectionCover.setImageResource(R.drawable.cover_placeholder)
            }

            binding.root.setOnClickListener { onCollectionClick(collection) }
        }
    }
}
