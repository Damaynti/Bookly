package com.example.book.viemodel.add

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.book.data.UserBook
import com.example.book.repos.UserBooksRepository
import com.example.book.databinding.FragmentAddBookBinding
import java.util.*

class AddBookFragment : Fragment() {

    private var _binding: FragmentAddBookBinding? = null
    private val binding get() = _binding!!
    private lateinit var repository: UserBooksRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = UserBooksRepository(requireContext())

        binding.saveButton.setOnClickListener {
            val title = binding.titleInput.text.toString()
            val author = binding.authorInput.text.toString()
            val genre = binding.genreInput.text.toString()
            val summary = binding.summaryInput.text.toString()

            if (title.isNotBlank() && author.isNotBlank()) {
                val book = UserBook(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    author = author,
                    genre = genre,
                    summary = summary,
                    coverImage = "book",
                    rating = 0,
                    createdAt = Date().toString(),
                    isFavorite = false
                )
                repository.saveUserBook(book)
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
