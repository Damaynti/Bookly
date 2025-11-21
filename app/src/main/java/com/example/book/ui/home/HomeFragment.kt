package com.example.book.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.book.data.UserBook
import com.example.book.data.UserBooksRepository
import com.example.book.databinding.FragmentHomeBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.widget.doAfterTextChanged
import androidx.navigation.fragment.findNavController
import com.example.book.R
import com.example.book.ui.AddBook.AddBookActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(UserBooksRepository(requireContext()))
    }

    private lateinit var bookAdapter: BookAdapter
    private lateinit var searchBookAdapter: BookAdapter

    interface HomeFragmentListener {
        fun onReadBook(book: UserBook)
        fun onAddBook()
    }

    private var listener: HomeFragmentListener? = null

    fun setListener(listener: HomeFragmentListener) {
        this.listener = listener
    }

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

        // Initialize sample data
        UserBooksRepository(requireContext()).initializeSampleData()

        setupAdapters()
        setupObservers()
        setupClickListeners()
    }

    private fun setupAdapters() {
        searchBookAdapter = BookAdapter(
            isGridLayout = true,
            onBookClick = { book -> listener?.onReadBook(book) },
            onFavoriteClick = { book -> viewModel.toggleFavorite(book) }
        )

        bookAdapter = BookAdapter(
            isGridLayout = false,
            onBookClick = { book -> listener?.onReadBook(book) },
            onFavoriteClick = { book -> viewModel.toggleFavorite(book) }
        )

        binding.searchResultsRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = searchBookAdapter
        }

        binding.booksRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bookAdapter
        }
    }

    private fun setupObservers() {
        // Подписка на все книги
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.books.collectLatest { books ->
                updateUI(books)
            }
        }

        // Подписка на результаты поиска
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collectLatest { searchResults ->
                val isSearching = viewModel.searchQuery.value.isNotBlank()
                if (isSearching) {
                    searchBookAdapter.submitList(searchResults)
                    updateSearchUI(searchResults)
                }
            }
        }
    }

    private fun updateUI(books: List<UserBook>) {
        val isSearching = viewModel.searchQuery.value.isNotBlank()

        if (!isSearching) {
            bookAdapter.submitList(books)

            if (books.isEmpty()) {
                binding.emptyState.visibility = View.VISIBLE
                binding.booksRecyclerView.visibility = View.GONE
                binding.searchResultsRecyclerView.visibility = View.GONE
            } else {
                binding.emptyState.visibility = View.GONE
                binding.booksRecyclerView.visibility = View.VISIBLE
                binding.searchResultsRecyclerView.visibility = View.GONE
            }
        }

        updateBookCount(books.size)
    }

    private fun updateSearchUI(searchResults: List<UserBook>) {
        if (searchResults.isEmpty()) {
            binding.searchResultsRecyclerView.visibility = View.GONE
            binding.emptySearchState.visibility = View.VISIBLE
        } else {
            binding.searchResultsRecyclerView.visibility = View.VISIBLE
            binding.emptySearchState.visibility = View.GONE
            binding.booksRecyclerView.visibility = View.GONE
            binding.emptyState.visibility = View.GONE
        }

        binding.searchResultsCount.text = "${searchResults.size} найдено"
    }

    private fun setupClickListeners() {
        binding.searchEditText.doAfterTextChanged { text ->
            viewModel.setSearchQuery(text.toString())
        }

        binding.addBookButton.setOnClickListener {
            val intent = Intent(requireContext(), AddBookActivity::class.java)
            startActivity(intent)
        }

        binding.emptyStateAddButton.setOnClickListener {
            val intent = Intent(requireContext(), AddBookActivity::class.java)
            startActivity(intent)
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

    fun formatDate(dateString: String): String {
        return try {
            val date = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH).parse(dateString)
            date?.let {
                val now = Date()
                val diff = now.time - it.time
                val diffDays = (diff / (1000 * 60 * 60 * 24)).toInt()

                when {
                    diffDays == 0 -> "Сегодня"
                    diffDays == 1 -> "Вчера"
                    diffDays < 7 -> "$diffDays дн. назад"
                    diffDays < 30 -> "${diffDays / 7} нед. назад"
                    else -> SimpleDateFormat("d MMM", Locale("ru")).format(it)
                }
            } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Factory для ViewModel
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
