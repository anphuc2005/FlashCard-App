package com.example.flashcardapp.presentation.feature.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flashcardapp.databinding.FragmentHomeBinding
import com.example.flashcardapp.presentation.feature.learning.LearningActivity
import com.example.flashcardapp.presentation.common.adapter.RecentDeckAdapter
import com.example.flashcardapp.presentation.common.adapter.ShortcutAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var shortcutAdapter: ShortcutAdapter
    private lateinit var recentDeckAdapter: RecentDeckAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        observeUiState()
        setupListeners()
    }

    private fun setupAdapters() {
        // Setup Shortcuts Adapter
        shortcutAdapter = ShortcutAdapter { shortcut ->
            handleShortcutClick(shortcut.action)
        }
        binding.rvShortcuts.apply {
            adapter = shortcutAdapter
            layoutManager = GridLayoutManager(requireContext(), 4)
        }

        // Setup Recent Decks Adapter
        recentDeckAdapter = RecentDeckAdapter { deck ->
            navigateToDeckDetail(deck.id)
        }
        binding.rvDeckRecently.apply {
            adapter = recentDeckAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Update shortcuts
                    shortcutAdapter.submitList(state.shortcuts)

                    // Update recent decks
                    recentDeckAdapter.submitList(state.recentDecks)

                    // Update active deck info
                    state.activeDeck?.let { deck ->
                        binding.apply {
                            tvCourseTitle.text = deck.name
                            tvProgressPercent.text = getString(
                                com.example.flashcardapp.R.string.home_progress_format,
                                state.userProgress
                            )
                            progressBar.setProgress(state.userProgress.toFloat())
                            btnStart.isEnabled = !state.isLoading
                        }
                    } ?: run {
                        binding.apply {
                            tvCourseTitle.text = getString(com.example.flashcardapp.R.string.home_no_active_deck)
                            tvProgressPercent.text = "0%"
                            progressBar.setProgress(0f)
                        }
                    }

                    // Update user info
                    binding.apply {
                        tvHello.text = state.userGreeting.ifBlank {
                            getString(com.example.flashcardapp.R.string.home_welcome_back)
                        }
                        tvStreak.text = state.userStreak.toString()
                    }

                    // Handle loading state
                    binding.btnStart.isEnabled = !state.isLoading

                    // Handle error state
                    state.error?.let { error ->
                        showError(error)
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnStart.setOnClickListener {
                viewModel.uiState.value.activeDeck?.let { deck ->
                    navigateToDeckDetail(deck.id)
                }
            }

            btnSeeAll.setOnClickListener {
                navigateToAllDecks()
            }
        }
    }

    private fun handleShortcutClick(action: String?) {
        when (action) {
            "CREATE" -> navigateToCreateDeck()
            "SEARCH" -> navigateToSearch()
            "LIST" -> navigateToAllDecks()
            "SETTINGS" -> navigateToSettings()
        }
    }

    private fun showError(@Suppress("UNUSED_PARAMETER") error: String) {
        // TODO: Show error message using Snackbar or Toast
        viewModel.clearError()
    }

    private fun navigateToDeckDetail(deckId: String) {
        val intent = Intent(requireActivity(), LearningActivity::class.java).apply {
            putExtra("DECK_ID", deckId)
        }
        requireActivity().startActivity(intent)
    }

    private fun navigateToAllDecks() {
        // TODO: Navigate to all decks screen
    }

    private fun navigateToCreateDeck() {
        // TODO: Navigate to create deck screen
    }

    private fun navigateToSearch() {
        // TODO: Navigate to search screen
    }

    private fun navigateToSettings() {
        // TODO: Navigate to settings screen
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        @Suppress("UNUSED")
        fun newInstance() = HomeFragment()
    }
}

