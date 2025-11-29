package com.example.book.viemodel.AddBook

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.book.R
import com.example.book.data.UserBook
import com.example.book.data.genres
import com.example.book.databinding.FragmentAddBookBinding
import com.example.book.repos.UserBooksRepository
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class AddBookFragment : Fragment() {

    private lateinit var binding: FragmentAddBookBinding
    private val viewModel: AddBookViewModel by viewModels { AddBookViewModelFactory(UserBooksRepository(requireContext())) }
    private var coverBase64: String? = null

    private val imagePicker = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.data
            if (uri != null) {
                setCoverImage(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddBookBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookId = arguments?.getString("bookId")

        setupGenreChips()

        if (bookId != null) {
            viewModel.loadBook(bookId)
        } else {
            binding.toolbar.title = "Добавить книгу"
            binding.buttonAddBook.text = "Добавить книгу"
        }

        setupClickListeners()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.book.collect { book ->
                book?.let { populateUI(it) }
            }
        }
    }

    private fun setupGenreChips() {
        val chipBackgroundColor = AppCompatResources.getColorStateList(requireContext(), R.color.chip_genre_selector)

        genres.forEach { genre ->
            val chip = Chip(requireContext()).apply {
                text = genre
                isCheckable = true
                setEnsureMinTouchTargetSize(false)
                chipStrokeWidth = 1f
                this.chipBackgroundColor = chipBackgroundColor
                setOnClickListener {
                    binding.chipGroupGenre.clearCheck()
                    isChecked = true
                }
            }
            binding.chipGroupGenre.addView(chip)
        }
    }

    private fun setupClickListeners() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.buttonUpload.setOnClickListener {
            showImagePickerDialog()
        }

        binding.buttonRemoveImage.setOnClickListener {
            removeCoverImage()
        }

        binding.buttonAddBook.setOnClickListener {
            val title = binding.editTitle.text.toString()
            val author = binding.editAuthor.text.toString()
            val summary = binding.editSummary.text.toString()
            val genre = getSelectedGenre()

            if (title.isBlank() || author.isBlank() || summary.isBlank() || genre.isBlank()) {
                Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.saveBook(title, author, genre, summary, coverBase64)
            findNavController().popBackStack()
        }
    }

    private fun getSelectedGenre(): String {
        val selectedChipId = binding.chipGroupGenre.checkedChipId
        if (selectedChipId != View.NO_ID) {
            val chip = binding.chipGroupGenre.findViewById<Chip>(selectedChipId)
            return chip.text.toString()
        }
        return ""
    }

    private fun populateUI(book: UserBook) {
        binding.toolbar.title = getString(R.string.edit_book)
        binding.buttonAddBook.text = getString(R.string.save_changes)
        binding.editTitle.setText(book.title)
        binding.editAuthor.setText(book.author)
        binding.editSummary.setText(book.summary)

        // Set genre
        for (i in 0 until binding.chipGroupGenre.childCount) {
            val chip = binding.chipGroupGenre.getChildAt(i) as Chip
            if (chip.text.toString() == book.genre) {
                chip.isChecked = true
                break
            }
        }

        coverBase64 = book.coverImage
        if (!book.coverImage.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(book.coverImage, Base64.DEFAULT)
                val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                binding.imageCover.setImageBitmap(decodedImage)
                binding.buttonRemoveImage.visibility = View.VISIBLE
            } catch (e: Exception) {
                // handle error
            }
        }
    }

    private fun showImagePickerDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Выберите обложку")
            .setItems(arrayOf("Выбрать из галереи", "Отмена")) { _, which ->
                when (which) {
                    0 -> pickImageFromGallery()
                }
            }
            .show()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        imagePicker.launch(intent)
    }

    private fun setCoverImage(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            binding.imageCover.setImageBitmap(bitmap)
            binding.buttonRemoveImage.visibility = View.VISIBLE
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val bytes = outputStream.toByteArray()
            coverBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Ошибка при загрузке изображения", Toast.LENGTH_SHORT).show()
        }
    }

    private fun removeCoverImage() {
        binding.imageCover.setImageResource(R.drawable.book_placeholder)
        binding.buttonRemoveImage.visibility = View.GONE
        coverBase64 = null
    }
}
