package com.example.book.ui.fav

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.book.data.UserBooksRepository
import com.example.book.databinding.FragmentFavBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavFragment : Fragment() {

    private var _binding: FragmentFavBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: UserBooksRepository
    private lateinit var adapter: FavAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = UserBooksRepository(requireContext())
        adapter = FavAdapter(
            onFavoriteClick = { book -> repository.toggleFavorite(book) },
            onReadClick = { book ->  }
        )


        binding.recyclerViewFav.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFav.adapter = adapter

        // Наблюдаем за изменениями избранных книг
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getFavoriteBooks().collectLatest { books ->
                adapter.submitList(books)
                binding.emptyView.visibility = if (books.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
