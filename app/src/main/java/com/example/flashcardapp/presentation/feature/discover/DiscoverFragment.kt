package com.example.flashcardapp.presentation.feature.discover

import android.app.AlertDialog
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
import com.example.flashcardapp.databinding.FragmentDiscoverBinding
import com.example.flashcardapp.presentation.common.adapter.CategoryAdapter
import com.example.flashcardapp.presentation.common.adapter.CourseAdapter
import com.example.flashcardapp.presentation.common.dialog.authDialog.LoadingDialogFragment
import com.example.flashcardapp.presentation.common.notification.showAppError
import com.example.flashcardapp.presentation.common.notification.showAppSuccess
import com.example.flashcardapp.presentation.feature.learning.LearningActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DiscoverFragment : Fragment() {

    private var _binding: FragmentDiscoverBinding? = null
    private val binding get() = _binding!!

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var courseAdapter: CourseAdapter

    private var loadingDialog: LoadingDialogFragment? = null

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
                AlertDialog.Builder(requireContext())
                    .setTitle("Lưu bộ thẻ")
                    .setMessage("Bạn có muốn lưu bộ thẻ '${course.name}' này vào thư viện của mình không?")
                    .setPositiveButton("Lưu") { dialog, _ ->
                        viewModel.cloneDeck(course.id)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Huỷ") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()
            }
        )
        binding.rvCourses.apply {
            adapter = courseAdapter
            layoutManager = LinearLayoutManager(context)
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
                        if (isLoading) {
                            if (loadingDialog == null) {
                                loadingDialog = LoadingDialogFragment.newInstance("Đang tải dữ liệu...")
                                loadingDialog?.show(childFragmentManager, "LoadingDialog")
                            }
                        } else {
                            loadingDialog?.dismiss()
                            loadingDialog = null
                        }
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
