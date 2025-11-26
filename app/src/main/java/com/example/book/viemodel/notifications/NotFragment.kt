package com.example.book.viemodel.notifications

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.book.databinding.FragmentNotificationsBinding
import com.example.book.viemodel.AddBooksToCollection.CreateCollectionFragment // Импортируем активность

class NotFragment : Fragment() {

    private lateinit var binding: FragmentNotificationsBinding
    private val viewModel: NotificationsViewModel by viewModels()
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

        // Создаём адаптер без начальных данных
        adapter = CollectionAdapter(emptyList()) {
            // TODO: переход к деталям подборки
        }

        // Настройка RecyclerView
        binding.collectionsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.collectionsRecyclerView.adapter = adapter

        // Наблюдаем за списком коллекций
        viewModel.collections.observe(viewLifecycleOwner) { list ->
            adapter.updateList(list)

            // Проверяем, есть ли подборки
            if (list.isEmpty()) {
                binding.collectionsRecyclerView.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
            } else {
                binding.collectionsRecyclerView.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE
            }
        }

        setupSearch()
        setupClickListeners() // Добавляем обработчики кликов
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
        // Обработчик для кнопки создания в хедере
        binding.createButton.setOnClickListener {
            openAddCollectionActivity()
        }

        // Обработчик для кнопки создания в пустом состоянии
        binding.emptyStateAddButton.setOnClickListener {
            openAddCollectionActivity()
        }
    }

    private fun openAddCollectionActivity() {
        val fragment = CreateCollectionFragment()

        // Добавляем/заменяем фрагмент через FragmentManager

    }
}