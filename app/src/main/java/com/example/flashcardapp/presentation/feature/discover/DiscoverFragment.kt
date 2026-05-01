package com.example.flashcardapp.presentation.feature.discover

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.R
import com.example.flashcardapp.core.utils.textChangesFlow
import com.example.flashcardapp.databinding.FragmentDiscoverBinding
import com.example.flashcardapp.presentation.common.adapter.CategoryAdapter
import com.example.flashcardapp.presentation.common.adapter.CourseAdapter
import com.example.flashcardapp.presentation.common.dialog.accountDialog.AppConfirmDialog
import com.example.flashcardapp.presentation.common.notification.showAppError
import com.example.flashcardapp.presentation.common.notification.showAppSuccess
import com.example.flashcardapp.presentation.feature.learning.LearningActivity
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.core.view.isVisible

class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var courseAdapter: CourseAdapter

    private val viewModel: DiscoverViewModel by viewModels {
        val appContainer = (requireActivity().application as FlashcardApp).container
        DiscoverViewModelFactory(
            appContainer.getAllDecksFromApiUseCase,
            appContainer.getAllCategoriesUseCase,
            appContainer.cloneDeckUseCase
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDiscoverBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerViews()
        setupListeners()
        observeSearchInput()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnSeeAllCategories.setOnClickListener {
            viewModel.filterCoursesByCategory(null)
        }
    }

    private fun setupRecyclerViews() {
        categoryAdapter = CategoryAdapter { category ->
            viewModel.filterCoursesByCategory(category.id)
        }
        binding.rvCategories.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        courseAdapter = CourseAdapter(
            onItemClick = { course ->
                val intent = Intent(requireActivity(), LearningActivity::class.java).apply {
                    putExtra("DECK_ID", course.id)
                }
                startActivity(intent)
            },
            onSaveClick = { course ->
                val dialog = AppConfirmDialog.newInstance(
                    title = getString(R.string.discover_save_deck_title),
                    message = getString(R.string.discover_save_deck_message, course.name),
                    confirmText = getString(R.string.discover_save_deck_action),
                    cancelText = getString(R.string.discover_save_deck_cancel),
                    iconRes = R.drawable.ic_download,
                    destructive = false
                )
                dialog.listener = object : AppConfirmDialog.Listener {
                    override fun onConfirm() {
                        viewModel.cloneDeck(course.id)
                    }
                }
                dialog.show(childFragmentManager, "discover_save_deck_confirm")
            }
        )
        binding.rvCourses.apply {
            adapter = courseAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchInput() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                binding.searchInput.textChangesFlow()
                    .map { it.trim() }
                    .debounce(300)
                    .distinctUntilChanged()
                    .collect { query ->
                        viewModel.updateSearchQuery(query)
                    }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.categories.collectLatest { categories ->
                        categoryAdapter.submitList(categories)
                        courseAdapter.setCategories(categories)
                    }
                }
                launch {
                    viewModel.courses.collectLatest { courses ->
                        courseAdapter.submitList(courses)
                    }
                }
                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        val alpha = if (isLoading) 0.55f else 1f
                        binding.rvCategories.alpha = alpha
                        binding.rvCourses.alpha = alpha
                        binding.searchContainer.alpha = alpha
                        binding.btnSeeAllCategories.isEnabled = !isLoading
                        binding.btnSeeAllCourses.isEnabled = !isLoading
                    }
                }
                launch {
                    viewModel.error.collectLatest { error ->
                        error?.let { showAppError(it) }
                    }
                }
                launch {
                    viewModel.cloneSuccess.collectLatest { message ->
                        showAppSuccess(message)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
