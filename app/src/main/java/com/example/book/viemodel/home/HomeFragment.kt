package com.example.book.viemodel.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.book.R
import com.example.book.data.UserBook
import com.example.book.databinding.FragmentHomeBinding
import com.example.book.repos.UserBooksRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(UserBooksRepository(requireContext()))
    }

    private lateinit var bookAdapter: BookAdapter
    private lateinit var searchBookAdapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        setupObservers()
        setupClickListeners()
    }

    private fun setupAdapters() {
        searchBookAdapter = BookAdapter(
            isGridLayout = false,
            onBookClick = { book ->
                val bundle = Bundle()
                bundle.putString("bookId", book.id)
                findNavController().navigate(R.id.action_global_bookDetailFragment, bundle)
            },
            onFavoriteClick = { book -> viewModel.toggleFavorite(book) }
        )

        bookAdapter = BookAdapter(
            isGridLayout = false,
            onBookClick = { book ->
                val bundle = Bundle()
                bundle.putString("bookId", book.id)
                findNavController().navigate(R.id.action_global_bookDetailFragment, bundle)
            },
            onFavoriteClick = { book -> viewModel.toggleFavorite(book) }
        )

        binding.searchResultsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchBookAdapter
            isNestedScrollingEnabled = false
        }

        binding.booksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bookAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.books.collectLatest { books ->
                bookAdapter.submitList(books)
                updateUI(books, viewModel.searchResults.value)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collectLatest { searchResults ->
                searchBookAdapter.submitList(searchResults)
                updateUI(viewModel.books.value, searchResults)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchQuery.collectLatest { query ->
                updateUI(viewModel.books.value, viewModel.searchResults.value)
            }
        }
    }

    private fun updateUI(books: List<UserBook>, searchResults: List<UserBook>) {
        val isSearching = viewModel.searchQuery.value.isNotBlank()


        updateBookCount(books.size)

        if (isSearching) {
            binding.booksRecyclerView.visibility = View.GONE
            binding.emptyState.visibility = View.GONE
            binding.searchResultsHeader.visibility = View.VISIBLE

            if (searchResults.isEmpty()) {
                binding.searchResultsRecyclerView.visibility = View.GONE
                binding.emptySearchState.visibility = View.VISIBLE
            } else {
                binding.searchResultsRecyclerView.visibility = View.VISIBLE
                binding.emptySearchState.visibility = View.GONE
            }
            binding.searchResultsCount.text = "${searchResults.size} найдено"
        } else {

            binding.searchResultsRecyclerView.visibility = View.GONE
            binding.emptySearchState.visibility = View.GONE
            binding.searchResultsHeader.visibility = View.GONE

            if (books.isEmpty()) {
                binding.booksRecyclerView.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
            } else {
                binding.booksRecyclerView.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE
            }
        }
    }

    private fun setupClickListeners() {
        binding.searchEditText.doAfterTextChanged { text ->
            viewModel.setSearchQuery(text.toString())
        }

        binding.addBookButton.setOnClickListener {
            findNavController().navigate(R.id.addBookFragment)
        }

        binding.emptyStateAddButton.setOnClickListener {
            findNavController().navigate(R.id.addBookFragment)
        }
    }

    private fun updateBookCount(count: Int) {
        val bookCountText = when {
            count % 10 == 1 && count % 100 != 11 -> "$count книга в библиотеке"
            count % 10 in 2..4 && count % 100 !in 12..14 -> "$count книги в библиотеке"
            else -> "$count книг в библиотеке"
        }
        binding.bookCountText.text = bookCountText
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class HomeViewModelFactory(private val repository: UserBooksRepository) :
    androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
