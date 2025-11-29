package com.example.book.viemodel.BookDetail

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.book.R
import com.example.book.data.UserBook
import com.example.book.databinding.FragmentBookDetailBinding
import com.example.book.repos.UserBooksRepository
import kotlinx.coroutines.launch

class BookDetailFragment : Fragment() {

    private lateinit var binding: FragmentBookDetailBinding
    private val viewModel: BookDetailViewModel by viewModels { BookDetailViewModelFactory(UserBooksRepository(requireContext())) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBookDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookId = arguments?.getString("bookId")

        if (bookId == null) {
            findNavController().popBackStack()
            return
        }

        viewModel.loadBook(bookId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.book.collect { book ->
                book?.let { displayBookDetails(it) }
            }
        }

        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnEdit.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("bookId", bookId)
            findNavController().navigate(R.id.action_bookDetailFragment_to_addBookFragment, bundle)
        }

        binding.btnFavorite.setOnClickListener {
            viewModel.toggleFavorite()
        }

        binding.btnDelete.setOnClickListener {
            viewModel.deleteBook()
            findNavController().popBackStack()
        }
    }

    private fun displayBookDetails(book: UserBook) {
        binding.tvTitle.text = book.title
        binding.tvAuthor.text = book.author
        binding.tvGenreBadge.text = book.genre
        binding.tvSummary.text = book.summary
        binding.tvCreatedAt.text = book.createdAt

        if (!book.coverImage.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(book.coverImage, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                Glide.with(requireContext())
                    .load(decodedImage)
                    .placeholder(R.drawable.book_placeholder)
                    .into(binding.imgCover)
            } catch (e: Exception) {
                binding.imgCover.setImageResource(R.drawable.book_placeholder)
            }
        } else {
            binding.imgCover.setImageResource(R.drawable.book_placeholder)
        }

        val favoriteIcon = if (book.isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outline
        binding.btnFavorite.setImageResource(favoriteIcon)
    }
}
