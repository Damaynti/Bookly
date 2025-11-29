package com.example.book.viemodel.collections

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.book.databinding.FragmentCreateCollectionBinding
import com.example.book.repos.UserBooksRepository
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class CreateCollectionFragment : Fragment() {

    private var _binding: FragmentCreateCollectionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreateCollectionViewModel by viewModels {
        CreateCollectionViewModelFactory(UserBooksRepository(requireContext()), requireContext())
    }

    private var selectedImageBase64: String? = null
    private var collectionId: String? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleImageSelection(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateCollectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        collectionId = arguments?.getString("collectionId")
        if (collectionId != null) {
            viewModel.loadCollection(collectionId!!)
        }

        setupClickListeners()
        setupValidation()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnCancel.setOnClickListener { findNavController().popBackStack() }

        binding.btnSave.setOnClickListener { saveCollection() }
        binding.btnCreate.setOnClickListener { saveCollection() }

        binding.btnUpload.setOnClickListener { openImagePicker() }
        binding.imgCover.setOnClickListener { openImagePicker() }

        binding.btnRemoveImage.setOnClickListener {
            selectedImageBase64 = null
            updateCoverImage(null)
        }
    }

    private fun setupValidation() {
        binding.inputTitle.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrBlank()) {
                binding.inputLayoutTitle.error = "Название не может быть пустым"
            } else {
                binding.inputLayoutTitle.error = null
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collectLatest {
                when (it) {
                    is CreateCollectionViewModel.UiState.Loading -> {
                        binding.btnSave.isEnabled = false
                        binding.btnCreate.isEnabled = false
                    }
                    is CreateCollectionViewModel.UiState.Success -> {
                        Snackbar.make(requireView(), "Подборка успешно сохранена", Snackbar.LENGTH_LONG).show()
                        findNavController().popBackStack()
                    }
                    is CreateCollectionViewModel.UiState.Error -> {
                        Snackbar.make(requireView(), it.message, Snackbar.LENGTH_LONG).show()
                        binding.btnSave.isEnabled = true
                        binding.btnCreate.isEnabled = true
                    }
                    else -> {
                        binding.btnSave.isEnabled = true
                        binding.btnCreate.isEnabled = true
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.collection.collectLatest { collection ->
                if (collection != null) {
                    binding.inputTitle.setText(collection.title)
                    binding.inputDescription.setText(collection.description)
                    selectedImageBase64 = collection.coverImage
                    if (!collection.coverImage.isNullOrBlank()) {
                        try {
                            val imageBytes = Base64.decode(collection.coverImage, Base64.DEFAULT)
                            val decodedImage = ImageDecoder.decodeBitmap(ImageDecoder.createSource(ByteBuffer.wrap(imageBytes)))
                            updateCoverImage(decodedImage)
                        } catch (e: Exception) {
                            // Handle exception
                        }
                    }
                }
            }
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        imagePickerLauncher.launch(intent)
    }

    private fun handleImageSelection(uri: Uri) {
        try {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(requireContext().contentResolver, uri))
            } else {
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }
            selectedImageBase64 = bitmapToBase64(bitmap)
            updateCoverImage(bitmap)
        } catch (e: Exception) {
            Snackbar.make(requireView(), "Не удалось загрузить изображение", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun updateCoverImage(bitmap: Bitmap?) {
        if (bitmap != null) {
            binding.imgCover.setImageBitmap(bitmap)
            binding.uploadOverlay.visibility = View.GONE
            binding.btnRemoveImage.visibility = View.VISIBLE
        } else {
            binding.imgCover.setImageResource(0) // Clear image
            binding.uploadOverlay.visibility = View.VISIBLE
            binding.btnRemoveImage.visibility = View.GONE
        }
    }

    private fun saveCollection() {
        val title = binding.inputTitle.text.toString().trim()
        val description = binding.inputDescription.text.toString().trim()

        viewModel.saveCollection(title, description, selectedImageBase64)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
