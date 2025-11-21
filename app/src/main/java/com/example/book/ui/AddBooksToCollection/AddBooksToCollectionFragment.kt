package com.example.book.ui.AddBooksToCollection

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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.book.data.BookCollection
import com.example.book.data.UserBooksRepository
import com.example.book.databinding.ActivityAddBooksToCollectionBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class CreateCollectionFragment : Fragment() {

    private var _binding: ActivityAddBooksToCollectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: UserBooksRepository

    private var coverBase64: String = ""

    // Выбор изображения
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {

                val imageUri: Uri? = result.data!!.data
                if (imageUri != null) {
                    val input = requireContext().contentResolver.openInputStream(imageUri)
                    if (input != null) {
                        val bitmap = BitmapFactory.decodeStream(input)
                        binding.imageCover.setImageBitmap(bitmap)
                        coverBase64 = bitmapToBase64(bitmap)
                    }
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding =ActivityAddBooksToCollectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        repository = UserBooksRepository(requireContext())
        setupListeners()
    }

    private fun setupListeners() {

        // кнопка выбора изображения
        binding.root.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK).apply {
                type = "image/*"
            }
            pickImageLauncher.launch(intent)
        }

        // кнопка создать
        binding.buttonCreate.setOnClickListener {
            saveCollection()
        }

        // кнопка назад на toolbar
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun saveCollection() {
        val title = binding.editTitle.text.toString().trim()
        val description = binding.editDescription.text.toString().trim()

        // проверка названия
        if (title.isEmpty()) {
            binding.layoutTitle.error = "Введите название"
            return
        } else {
            binding.layoutTitle.error = null
        }

        // проверка описания
        if (description.isEmpty()) {
            binding.layoutDescription.error = "Введите описание"
            return
        } else {
            binding.layoutDescription.error = null
        }

        // проверка загрузки обложки
        if (coverBase64.isEmpty()) {
            Snackbar.make(binding.root, "Пожалуйста, выберите обложку", Snackbar.LENGTH_SHORT).show()
            return
        }

        // создание объекта
        val id = UUID.randomUUID().toString()
        val createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(Date())

        val collection = BookCollection(
            id = id,
            title = title,
            description = description,
            coverImage = coverBase64,
            createdAt = createdAt,
            bookIds = emptyList()
        )

        // сохранение
        repository.insertCollection(collection)

        // возврат назад
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    // Bitmap → Base64
    private fun bitmapToBase64(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
