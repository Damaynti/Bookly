package com.example.book.viemodel.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.book.R
import com.example.book.model.BookCollection

class CollectionAdapter(
    private var collections: List<BookCollection>,
    private val onClick: (BookCollection) -> Unit
) : RecyclerView.Adapter<CollectionAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val coverImage: ImageView = view.findViewById(R.id.coverImage)
        val titleText: TextView = view.findViewById(R.id.titleText)
        val descriptionText: TextView = view.findViewById(R.id.descriptionText)
        val bookCountText: TextView = view.findViewById(R.id.bookCountText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_collection, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = collections[position]
        holder.titleText.text = item.title
        holder.descriptionText.text = item.description
        holder.bookCountText.text = "Книг: ${item.bookIds}"

        if (!item.coverImage.isNullOrEmpty()) {
            Glide.with(holder.itemView)
                .load(item.coverImage)
                .placeholder(R.drawable.ic_folder)
                .into(holder.coverImage)
        } else {
            holder.coverImage.setImageResource(R.drawable.ic_folder)
        }

        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount(): Int = collections.size

    fun updateList(newList: List<BookCollection>) {
        collections = newList
        notifyDataSetChanged()
    }
}
