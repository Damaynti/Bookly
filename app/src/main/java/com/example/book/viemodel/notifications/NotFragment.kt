package com.example.book.viemodel.notifications

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.book.R
import com.example.book.databinding.FragmentNotificationsBinding
import com.example.book.repos.UserBooksRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding
    private val viewModel: NotificationsViewModel by viewModels { NotificationsViewModelFactory(UserBooksRepository(requireContext())) }
    private lateinit var adapter: CollectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CollectionAdapter(emptyList()) { collection ->
            val bundle = bundleOf("collectionId" to collection.id)
            findNavController().navigate(R.id.action_navigation_notifications_to_collectionDetailFragment, bundle)
        }

        binding.collectionsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.collectionsRecyclerView.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.collections.collectLatest { list ->
                adapter.updateList(list)
                binding.collectionsRecyclerView.visibility = if (list.isEmpty()) View.GONE else View.VISIBLE
                binding.emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        setupSearch()
        setupClickListeners()
    }

    private fun setupSearch() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.search(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupClickListeners() {
        binding.createButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_notifications_to_createCollectionFragment)
        }

        binding.emptyStateAddButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_notifications_to_createCollectionFragment)
        }
    }
}

class NotificationsViewModelFactory(private val repository: UserBooksRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NotificationsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NotificationsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
