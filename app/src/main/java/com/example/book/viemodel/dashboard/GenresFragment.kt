package com.example.book.viemodel.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.book.data.UserBook
import com.example.book.databinding.FragmentGenresBinding
import com.example.book.repos.UserBooksRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GenresFragment : Fragment() {

    private var _binding: FragmentGenresBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GenresViewModel by viewModels {
        GenresViewModelFactory(UserBooksRepository(requireContext()))
    }

    private lateinit var genreAdapter: GenreAdapter
    private lateinit var booksAdapter: BooksByGenreAdapter

    interface GenresFragmentListener {
        fun onReadBook(book: UserBook)
    }

    private var listener: GenresFragmentListener? = null

    fun setListener(listener: GenresFragmentListener) {
        this.listener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGenresBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupObservers()
        setupSearch()
        setupBackButton()
    }

    private fun setupAdapters() {
        genreAdapter = GenreAdapter { genre ->
            viewModel.selectGenre(genre)
        }

        booksAdapter = BooksByGenreAdapter(
            onBookClick = { book -> listener?.onReadBook(book) },
            onFavoriteClick = { book -> viewModel.toggleFavorite(book) }
        )

        binding.genresRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.genresRecyclerView.adapter = genreAdapter

        binding.booksRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.booksRecyclerView.adapter = booksAdapter
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.genres.collectLatest { genreAdapter.submitList(it) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.filteredBooks.collectLatest { books ->
                booksAdapter.submitList(books)
                updateUI(books)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isSearching.collectLatest {
                updateUI(viewModel.filteredBooks.value)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedGenre.collectLatest { genre ->
                if (genre != null) {
                    binding.selectedGenreTitle.text = genre
                }
                updateUI(viewModel.filteredBooks.value)
            }
        }
    }

    private fun setupSearch() {
        binding.searchEditText.doAfterTextChanged { text ->
            viewModel.setSearchQuery(text.toString())
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            viewModel.selectGenre(null)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (viewModel.selectedGenre.value != null) {
                viewModel.selectGenre(null)
            } else {
                isEnabled = false
                requireActivity().onBackPressed()
            }
        }
    }

    private fun updateUI(books: List<UserBook>) {
        val searching = viewModel.isSearching.value
        val genreSelected = viewModel.selectedGenre.value != null

        if (searching || genreSelected) {
            binding.genresRecyclerView.visibility = View.GONE
            binding.booksRecyclerView.visibility = View.VISIBLE
            binding.subtitleText.visibility = View.GONE
            binding.selectedGenreHeader.visibility = if (genreSelected) View.VISIBLE else View.GONE
        } else {
            binding.genresRecyclerView.visibility = View.VISIBLE
            binding.booksRecyclerView.visibility = View.GONE
            binding.subtitleText.visibility = View.VISIBLE
            binding.selectedGenreHeader.visibility = View.GONE
        }

        binding.emptyState.visibility = if (searching && books.isEmpty()) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
