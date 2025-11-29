package com.example.book.viemodel.collections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.book.R
import com.example.book.data.UserBook
import com.example.book.databinding.FragmentCollectionDetailBinding
import com.example.book.repos.UserBooksRepository
import com.example.book.viemodel.home.BookAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CollectionDetailFragment : Fragment() {

    private var _binding: FragmentCollectionDetailBinding? = null
    private val binding get() = _binding!!

    private val collectionId: String by lazy { requireArguments().getString("collectionId")!! }

    private val viewModel: CollectionDetailViewModel by viewModels {
        CollectionDetailViewModelFactory(UserBooksRepository(requireContext()), collectionId)
    }

    private lateinit var bookAdapter: BookAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCollectionDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        bookAdapter = BookAdapter(
            isGridLayout = false,
            onBookClick = { /* TODO: Handle book click */ },
            onFavoriteClick = { /* TODO: Handle favorite click */ },
            onBookLongClick = { book ->
                showRemoveBookConfirmationDialog(book)
            }
        )
        binding.recyclerBooks.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bookAdapter
        }
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        binding.btnDeleteCollection.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        binding.btnAddBooks.setOnClickListener {
            val bundle = bundleOf("collectionId" to collectionId)
            findNavController().navigate(R.id.action_collectionDetailFragment_to_addBooksToCollectionFragment, bundle)
        }

        binding.btnAddBooksEmpty.setOnClickListener {
            val bundle = bundleOf("collectionId" to collectionId)
            findNavController().navigate(R.id.action_collectionDetailFragment_to_addBooksToCollectionFragment, bundle)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                if (state.isLoading) {
                    // TODO: Show loading state
                } else if (state.error != null) {
                    // TODO: Show error state
                } else {
                    state.collection?.let {
                        binding.toolbar.title = it.title
                        binding.tvCollectionTitle.text = it.title
                        if (it.description.isNotBlank()) {
                            binding.tvCollectionDescription.text = it.description
                            binding.tvCollectionDescription.visibility = View.VISIBLE
                        }
                        binding.badgeBooksCount.text = "${it.bookIds.size} книг"
                    }
                    bookAdapter.submitList(state.books)

                    if (state.books.isEmpty()) {
                        binding.emptyState.visibility = View.VISIBLE
                        binding.recyclerBooks.visibility = View.GONE
                    } else {
                        binding.emptyState.visibility = View.GONE
                        binding.recyclerBooks.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext(), R.style.App_MaterialAlertDialog)
            .setTitle("Удалить подборку?")
            .setMessage("Это действие нельзя будет отменить.")
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteCollection()
                findNavController().popBackStack()
            }
            .show()
    }

    private fun showRemoveBookConfirmationDialog(book: UserBook) {
        MaterialAlertDialogBuilder(requireContext(), R.style.App_MaterialAlertDialog)
            .setTitle("Удалить книгу из подборки?")
            .setMessage("Вы уверены, что хотите удалить книгу \"${book.title}\" из этой подборки?")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.removeBookFromCollection(book.id)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
