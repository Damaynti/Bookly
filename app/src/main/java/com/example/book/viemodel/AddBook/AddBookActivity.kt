package com.example.book.viemodel.AddBook

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.book.data.UserBook
import com.example.book.repos.UserBooksRepository
import com.example.book.databinding.ActivityAddBookBinding
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddBookActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddBookBinding
    private lateinit var repository: UserBooksRepository
    private var coverBase64: String = ""
    private val selectedCollections = mutableListOf<String>()

    private val genres = listOf(
        "Классическая литература",
        "Современная проза",
        "Фантастика",
        "Детектив",
        "Романтика",
        "Триллер",
        "Научная фантастика",
        "Фэнтези",
        "Биография",
        "История",
        "Психология",
        "Бизнес",
        "Саморазвитие",
        "Поэзия",
        "Драма",
        "Приключения",
        "Ужасы",
        "Юмор"
    )

    private val imagePicker =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) setCoverImage(uri)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBookBinding.inflate(layoutInflater)
        setContentView(binding.root)

        repository = UserBooksRepository(this)
        setupUI()
    }

    private fun setupUI() {
        setupToolbar()
        setupGenreChips()
        setupTextCounters()
        setupClickListeners()
        setupCollections()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupGenreChips() {
        genres.forEach { genre ->
            val chip = Chip(this).apply {
                text = genre
                isCheckable = true
                setEnsureMinTouchTargetSize(false)
                chipStrokeWidth = 1f
                setOnClickListener {
                    // Автоматически снимаем выделение с других чипов
                    binding.chipGroupGenre.clearCheck()
                    isChecked = true
                }
            }
            binding.chipGroupGenre.addView(chip)
        }
    }

    private fun setupTextCounters() {
        // Счетчик для названия
        binding.editTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.textTitleCounter.text = "${s?.length ?: 0}/100 символов"
            }
        })

        // Счетчик для автора
        binding.editAuthor.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.textAuthorCounter.text = "${s?.length ?: 0}/100 символов"
            }
        })

        // Счетчик для описания
        binding.editSummary.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                binding.textSummaryCounter.text = "${s?.length ?: 0}/2000 символов"
            }
        })
    }

    private fun setupClickListeners() {
        // Загрузка обложки
        binding.buttonUpload.setOnClickListener {
            showImagePickerDialog()
        }

        // Удаление обложки
        binding.buttonRemoveImage.setOnClickListener {
            removeCoverImage()
        }

        // Добавление книги
        binding.buttonAddBook.setOnClickListener {
            saveBook()
        }
    }

    private fun setupCollections() {
        // TODO: Загрузить реальные подборки из базы данных
        val collections = listOf<String>() // Пустой список

        binding.cardCollections.visibility = if (collections.isNotEmpty()) {
            View.VISIBLE
            collections.forEach { collectionName ->
                val checkBox = CheckBox(this).apply {
                    text = collectionName
                    textSize = 14f
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            selectedCollections.add(collectionName)
                        } else {
                            selectedCollections.remove(collectionName)
                        }
                        updateCollectionsCounter()
                    }
                }
                binding.layoutCollections.addView(checkBox)
            }
            updateCollectionsCounter()
            View.VISIBLE
        } else {
            View.GONE
        }
    }
    private fun updateCollectionsCounter() {
        val totalCollections = binding.layoutCollections.childCount
        binding.textCollectionsCounter.text = "Выбрано: ${selectedCollections.size} из $totalCollections"
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Выбрать из галереи", "Отмена")

        MaterialAlertDialogBuilder(this)
            .setTitle("Выберите обложку")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> pickImageFromGallery()
                    // 1 - Отмена
                }
            }
            .show()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK).apply {
            type = "image/*"
        }
        imagePicker.launch(intent)
    }

    private fun setCoverImage(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Проверка размера (5MB)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            if (stream.toByteArray().size > 5 * 1024 * 1024) {
                Toast.makeText(this, "Размер файла не должен превышать 5MB", Toast.LENGTH_SHORT).show()
                return
            }

            binding.imageCover.setImageBitmap(bitmap)
            binding.buttonRemoveImage.visibility = View.VISIBLE

            // Преобразуем в base64
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val bytes = outputStream.toByteArray()
            coverBase64 = Base64.encodeToString(bytes, Base64.DEFAULT)

            Toast.makeText(this, "Обложка загружена", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при загрузке изображения", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("ResourceType")
    private fun removeCoverImage() {
        binding.imageCover.setImageDrawable(null)
        binding.imageCover.setBackgroundResource(com.google.android.material.R.attr.colorSurfaceVariant)
        binding.buttonRemoveImage.visibility = View.GONE
        coverBase64 = ""
    }

    private fun getSelectedGenre(): String {
        val selectedChipId = binding.chipGroupGenre.checkedChipId
        if (selectedChipId != View.NO_ID) {
            val chip = findViewById<Chip>(selectedChipId)
            return chip.text.toString()
        }
        return ""
    }

    private fun saveBook() {
        val title = binding.editTitle.text.toString().trim()
        val author = binding.editAuthor.text.toString().trim()
        val genre = getSelectedGenre()
        val summary = binding.editSummary.text.toString().trim()

        when {
            title.isEmpty() -> {
                Toast.makeText(this, "Введите название книги", Toast.LENGTH_SHORT).show()
                return
            }
            author.isEmpty() -> {
                Toast.makeText(this, "Введите автора книги", Toast.LENGTH_SHORT).show()
                return
            }
            genre.isEmpty() -> {
                Toast.makeText(this, "Выберите жанр", Toast.LENGTH_SHORT).show()
                return
            }
            summary.isEmpty() -> {
                Toast.makeText(this, "Напишите краткое содержание", Toast.LENGTH_SHORT).show()
                return
            }
            coverBase64.isEmpty() -> {
                Toast.makeText(this, "Загрузите обложку книги", Toast.LENGTH_SHORT).show()
                return
            }
        }

        val id = System.currentTimeMillis().toString()
        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val book = UserBook(
            id = id,
            title = title,
            author = author,
            genre = genre,
            summary = summary,
            coverImage = coverBase64,
            rating = 0,
            createdAt = date,
            isFavorite = false
        )

        CoroutineScope(Dispatchers.IO).launch {
            repository.saveUserBook(book)

            // TODO: Добавить книгу в выбранные подборки

            runOnUiThread {
                val message = if (selectedCollections.isNotEmpty()) {
                    "Книга добавлена в ${selectedCollections.size} подборку(и)!"
                } else {
                    "Книга успешно добавлена!"
                }
                Toast.makeText(this@AddBookActivity, message, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}