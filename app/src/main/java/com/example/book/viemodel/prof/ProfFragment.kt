package com.example.book.viemodel.prof

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.book.databinding.FragmentProfBinding
import com.example.book.repos.UserBooksRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfFragment : Fragment() {

    private var _binding: FragmentProfBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfViewModel by viewModels {
        ProfViewModelFactory(UserBooksRepository(requireContext()))
    }

    private val exportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                viewLifecycleOwner.lifecycleScope.launch {
                    val json = viewModel.getExportData()
                    writeJsonToUri(uri, json)
                }
            }
        }
    }

    private val importLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                readJsonFromUri(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.theme.collectLatest { theme ->
                binding.themeSwitch.isChecked = theme == "dark"
            }
        }

        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setTheme(if (isChecked) "dark" else "light")
        }

        binding.btnDelete.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        binding.btnExport.setOnClickListener {
            createFile()
        }

        binding.btnImport.setOnClickListener {
            openFile()
        }
    }

    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удалить все данные?")
            .setMessage("Это действие нельзя будет отменить. Все ваши книги и подборки будут удалены.")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteAllData()
            }
            .show()
    }

    private fun createFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "bookly_backup.json")
        }
        exportLauncher.launch(intent)
    }

    private fun openFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        importLauncher.launch(intent)
    }

    private fun writeJsonToUri(uri: Uri, json: String) {
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use {
                it.write(json.toByteArray())
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    private fun readJsonFromUri(uri: Uri) {
        try {
            requireContext().contentResolver.openInputStream(uri)?.use {
                val json = it.reader().readText()
                viewModel.importData(json)
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
