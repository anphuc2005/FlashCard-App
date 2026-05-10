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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.R
import com.example.flashcardapp.core.utils.textChangesFlow
import com.example.flashcardapp.databinding.FragmentDiscoverBinding
import com.example.flashcardapp.presentation.common.adapter.CategoryAdapter
import com.example.flashcardapp.presentation.common.adapter.CourseAdapter
import com.example.flashcardapp.presentation.common.dialog.accountDialog.AppConfirmDialog
import com.example.flashcardapp.presentation.common.dialog.accountDialog.ReportDeckDialog
import com.example.flashcardapp.presentation.common.notification.showAppError
import com.example.flashcardapp.presentation.common.notification.showAppSuccess
import com.example.flashcardapp.presentation.common.notification.showAppWarning
import com.example.flashcardapp.presentation.feature.learning.LearningActivity
import com.example.flashcardapp.domain.model.Category
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.core.view.isVisible

private const val COLLAPSED_CATEGORY_COUNT = 5

class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var courseAdapter: CourseAdapter
    private lateinit var categoryLayoutManager: LinearLayoutManager
    private var allCategories: List<Category> = emptyList()
    private var isCategoriesExpanded: Boolean = false
    private var isCategoryScrollEnabled: Boolean = false

    private val viewModel: DiscoverViewModel by viewModels {
        val appContainer = (requireActivity().application as FlashcardApp).container
        DiscoverViewModelFactory(
            appContainer.getAllDecksFromApiUseCase,
            appContainer.getAllCategoriesUseCase,
            appContainer.cloneDeckUseCase,
            appContainer.submitDeckReportUseCase
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
            if (allCategories.size <= getCollapsedCategoryCount()) return@setOnClickListener
            isCategoriesExpanded = !isCategoriesExpanded
            renderCategorySection()
        }
        binding.btnPrevCoursePage.setOnClickListener {
            viewModel.goToPreviousCoursePage()
        }
        binding.btnNextCoursePage.setOnClickListener {
            viewModel.goToNextCoursePage()
        }
    }

    private fun setupRecyclerViews() {
        categoryAdapter = CategoryAdapter { category ->
            val activeCategoryId = viewModel.selectedCategoryId.value
            if (activeCategoryId == category.id) {
                viewModel.filterCoursesByCategory(null)
            } else {
                viewModel.filterCoursesByCategory(category.id)
            }
        }
        categoryLayoutManager = object : LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false) {
            override fun canScrollHorizontally(): Boolean {
                return isCategoryScrollEnabled && super.canScrollHorizontally()
            }
        }
        binding.rvCategories.apply {
            adapter = categoryAdapter
            layoutManager = categoryLayoutManager
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
            },
            onReportClick = { course ->
                showReportDialog(course.id, course.name)
            }
        )
        binding.rvCourses.apply {
            adapter = courseAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun observeSearchInput() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                binding.searchInput.textChangesFlow()
                    .map { it.trim() }
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
                        allCategories = categories
                        renderCategorySection()
                        courseAdapter.setCategories(categories)
                    }
                }
                launch {
                    viewModel.selectedCategoryId.collectLatest {
                        renderCategorySection()
                    }
                }
                launch {
                    viewModel.courses.collectLatest { courses ->
                        courseAdapter.submitList(courses)
                    }
                }
                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        binding.btnSeeAllCategories.isEnabled = !isLoading
                        if (!viewModel.isShowingAllCourses.value) {
                            binding.btnPrevCoursePage.isEnabled =
                                viewModel.currentCoursePage.value > 1 && !isLoading
                            binding.btnNextCoursePage.isEnabled =
                                viewModel.currentCoursePage.value < viewModel.totalCoursePages.value && !isLoading
                        }
                    }
                }
                launch {
                    viewModel.currentCoursePage.collectLatest { currentPage ->
                        binding.tvCoursePageIndicator.text = getString(
                            R.string.discover_page_indicator,
                            currentPage,
                            viewModel.totalCoursePages.value
                        )
                        if (!viewModel.isShowingAllCourses.value) {
                            binding.btnPrevCoursePage.isEnabled = currentPage > 1 && !viewModel.isLoading.value
                            binding.btnNextCoursePage.isEnabled =
                                currentPage < viewModel.totalCoursePages.value && !viewModel.isLoading.value
                        }
                    }
                }
                launch {
                    viewModel.totalCoursePages.collectLatest { totalPages ->
                        binding.tvCoursePageIndicator.text = getString(
                            R.string.discover_page_indicator,
                            viewModel.currentCoursePage.value,
                            totalPages
                        )
                        if (!viewModel.isShowingAllCourses.value) {
                            binding.layoutCoursePagination.isVisible = totalPages > 1
                            binding.btnPrevCoursePage.isEnabled =
                                viewModel.currentCoursePage.value > 1 && !viewModel.isLoading.value
                            binding.btnNextCoursePage.isEnabled =
                                viewModel.currentCoursePage.value < totalPages && !viewModel.isLoading.value
                        }
                    }
                }
                launch {
                    viewModel.isShowingAllCourses.collectLatest { isShowingAll ->
                        binding.layoutCoursePagination.isVisible =
                            !isShowingAll && viewModel.totalCoursePages.value > 1
                    }
                }
                launch {
                    viewModel.error.collectLatest { error ->
                        error?.let {
                            if (it == DISCOVER_OFFLINE_MODE_MESSAGE) {
                                showAppWarning(it)
                            } else {
                                showAppError(it)
                            }
                            viewModel.clearError()
                        }
                    }
                }
                launch {
                    viewModel.cloneSuccess.collectLatest { message ->
                        showAppSuccess(message)
                    }
                }
                launch {
                    viewModel.reportSuccess.collectLatest { message ->
                        showAppSuccess(message)
                    }
                }
            }
        }
    }

    private fun showReportDialog(deckId: String, deckName: String) {
        val dialog = ReportDeckDialog.newInstance(deckId, deckName)
        dialog.listener = object : ReportDeckDialog.Listener {
            override fun onSubmit(deckId: String, reason: String) {
                viewModel.submitDeckReport(deckId, reason)
            }
        }
        dialog.show(childFragmentManager, "discover_report_deck_dialog")
    }

    private fun renderCategorySection() {
        val selectedCategoryId = viewModel.selectedCategoryId.value
        val visibleCategories = getVisibleCategories(selectedCategoryId)
        val collapsed = !isCategoriesExpanded && allCategories.size > getCollapsedCategoryCount()

        categoryAdapter.submitList(visibleCategories) {
            categoryAdapter.setSelectedCategoryId(selectedCategoryId)
        }
        updateCategoryRecyclerLayout(isCollapsed = collapsed)
        updateCategoryToggleButton()
    }

    private fun getVisibleCategories(selectedCategoryId: String?): List<Category> {
        val collapsedCount = getCollapsedCategoryCount()
        if (isCategoriesExpanded || allCategories.size <= collapsedCount) {
            return allCategories
        }

        val collapsed = allCategories.take(collapsedCount).toMutableList()
        if (selectedCategoryId.isNullOrBlank()) return collapsed

        if (collapsed.any { it.id == selectedCategoryId }) {
            return collapsed
        }

        val selectedCategory = allCategories.firstOrNull { it.id == selectedCategoryId } ?: return collapsed
        if (collapsed.isEmpty()) return listOf(selectedCategory)

        collapsed[collapsed.lastIndex] = selectedCategory
        return collapsed.distinctBy { it.id }
    }

    private fun getCollapsedCategoryCount(): Int {
        return allCategories.size.coerceAtMost(COLLAPSED_CATEGORY_COUNT)
    }

    private fun updateCategoryRecyclerLayout(isCollapsed: Boolean) {
        isCategoryScrollEnabled = !isCollapsed
        val params = binding.rvCategories.layoutParams as ConstraintLayout.LayoutParams
        val listPaddingHorizontal = resources.getDimensionPixelSize(R.dimen.discover_list_padding_horizontal)
        params.width = if (isCollapsed) {
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        } else {
            0
        }
        binding.rvCategories.layoutParams = params
        binding.rvCategories.setPadding(
            if (isCollapsed) 0 else listPaddingHorizontal,
            binding.rvCategories.paddingTop,
            if (isCollapsed) 0 else listPaddingHorizontal,
            binding.rvCategories.paddingBottom
        )
        binding.rvCategories.requestLayout()
    }

    private fun updateCategoryToggleButton() {
        val canExpand = allCategories.size > getCollapsedCategoryCount()
        binding.btnSeeAllCategories.isEnabled = canExpand
        binding.btnSeeAllCategories.alpha = if (canExpand) 1f else 0.45f
        binding.btnSeeAllCategories.text = if (isCategoriesExpanded && canExpand) {
            getString(R.string.discover_collapse)
        } else {
            getString(R.string.discover_all)
        }
        binding.btnSeeAllCategories.setBackgroundResource(
            if (isCategoriesExpanded && canExpand) {
                R.drawable.bg_theme_option_selected
            } else {
                R.drawable.bg_theme_option_unselected
            }
        )
        binding.btnSeeAllCategories.contentDescription = if (isCategoriesExpanded && canExpand) {
            getString(R.string.discover_collapse)
        } else {
            getString(R.string.discover_all)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
