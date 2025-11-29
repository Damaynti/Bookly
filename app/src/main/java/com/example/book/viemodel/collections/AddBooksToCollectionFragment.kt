package com.example.book.viemodel.collections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.book.R
import com.example.book.databinding.FragmentAddBooksToCollectionBinding
import com.example.book.repos.UserBooksRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AddBooksToCollectionFragment : Fragment() {

    private var _binding: FragmentAddBooksToCollectionBinding? = null
    private val binding get() = _binding!!

    private val collectionId: String by lazy { requireArguments().getString("collectionId")!! }

    private val viewModel: AddBooksToCollectionViewModel by viewModels {
        AddBooksToCollectionViewModelFactory(UserBooksRepository(requireContext()), collectionId)
    }

    private lateinit var bookAdapter: BookSelectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBooksToCollectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupToolbar()
        setupSearch()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        bookAdapter = BookSelectionAdapter(emptyList()) { book, isSelected ->
            viewModel.onBookSelected(book, isSelected)
        }
        binding.booksRecyclerView.adapter = bookAdapter
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.addBooksButton) {
                viewModel.addSelectedBooksToCollection()
                findNavController().popBackStack()
                true
            } else {
                false
            }
        }
    }

    private fun setupSearch() {
        binding.searchInput.doOnTextChanged { text, _, _, _ ->
            viewModel.setSearchQuery(text.toString())
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.books.collectLatest { books ->
                bookAdapter.updateBooks(books)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.selectedBookIds.collectLatest { selectedIds ->
                binding.selectedCountText.text = getString(R.string.selected_books_count, selectedIds.size)
                val menuItem = binding.toolbar.menu.findItem(R.id.addBooksButton)
                menuItem?.isEnabled = selectedIds.isNotEmpty()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
