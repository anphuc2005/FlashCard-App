package com.example.flashcardapp.presentation.feature.learning

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.view.isVisible
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentDeckDetailBinding
import com.example.flashcardapp.presentation.common.notification.showAppError
import com.example.flashcardapp.presentation.common.notification.showAppWarning
import com.example.flashcardapp.presentation.feature.learning.adapter.LearningCardPreviewAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.launch

private const val TAB_NEW = 1
private const val TAB_REVIEW = 2
private const val TAG = "DeckDetailFragment"

class DeckDetailFragment : Fragment() {

    private var _binding: FragmentDeckDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FlashCardViewModel by activityViewModels()
    private lateinit var cardPreviewAdapter: LearningCardPreviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDeckDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        loadDeck()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupRecyclerView() {
        cardPreviewAdapter = LearningCardPreviewAdapter()
        binding.rvCards.apply {
            adapter = cardPreviewAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { requireActivity().finish() }
        binding.ctaButton.setOnClickListener {
            Log.d(TAG, "Start button clicked for deckId=${viewModel.uiState.value.deckId}")
            findNavController().navigate(R.id.action_deckDetailFragment_to_sessionSettingFragment)
        }
        binding.tabLayout.addOnTabSelectedListener(
            LearningTabSelectedListener { filter -> viewModel.setFilter(filter) }
        )
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state)
                    state.errorMessage?.let { message ->
                        Log.e(TAG, "UI error received: deckId=${state.deckId}, message=$message")
                        showAppError(message)
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    private fun loadDeck() {
        val deckId = arguments?.getString(EXTRA_DECK_ID)
            ?: requireActivity().intent.getStringExtra(EXTRA_DECK_ID)
        if (deckId.isNullOrBlank()) {
            Log.w(TAG, "loadDeck aborted: missing deckId in args and intent")
            showAppWarning(getString(R.string.learning_missing_deck))
            requireActivity().finish()
            return
        }
        Log.d(TAG, "loadDeck requested from UI: deckId=$deckId")
        viewModel.loadDeck(deckId)
    }

    private fun renderState(state: LearningUiState) {
        val deck = state.deck
        renderLoadingState(state.isLoading && deck == null)
        Log.d(
            TAG,
            "renderState: deckId=${state.deckId}, loading=${state.isLoading}, cards=${state.cards.size}, preview=${state.previewCards.size}, learned=${state.deckSummary.learnedCards}"
        )
        binding.deckTitle.text = deck?.name ?: getString(R.string.learning_deck_loading)
        binding.deckDesc.text = deck?.description ?: getString(R.string.learning_deck_no_description)
        binding.statTotalCard.statLabel.text = getString(R.string.learning_stat_total)
        binding.statTotalCard.statValue.text = state.deckSummary.totalCards.toString()
        binding.statLearnedCard.statLabel.text = getString(R.string.learning_stat_learned)
        binding.statLearnedCard.statValue.text = state.deckSummary.learnedCards.toString()
        binding.statNewCard.statLabel.text = getString(R.string.learning_stat_new)
        binding.statNewCard.statValue.text = state.deckSummary.newCards.toString()
        binding.ctaButton.isEnabled = !state.isLoading && state.cards.isNotEmpty()
        binding.deckTitle.isVisible = !state.isLoading
        binding.deckDesc.isVisible = !state.isLoading
        binding.titlePlaceholder.isVisible = state.isLoading
        binding.descPlaceholder.isVisible = state.isLoading
        binding.rvCards.isVisible = !state.isLoading
        binding.cardsPlaceholderContainer.isVisible = state.isLoading
        cardPreviewAdapter.submitList(state.previewCards)
    }

    private fun renderLoadingState(isLoading: Boolean) {
        binding.scroll.visibility = View.VISIBLE
    }

    private class LearningTabSelectedListener(
        private val onFilterSelected: (LearningCardFilter) -> Unit
    ) : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(tab: TabLayout.Tab?) {
            val filter = when (tab?.position) {
                TAB_NEW -> LearningCardFilter.NEW
                TAB_REVIEW -> LearningCardFilter.REVIEW
                else -> LearningCardFilter.ALL
            }
            Log.d(TAG, "Tab selected: position=${tab?.position}, filter=$filter")
            onFilterSelected(filter)
        }

        override fun onTabUnselected(tab: TabLayout.Tab?) = Unit
        override fun onTabReselected(tab: TabLayout.Tab?) = Unit
    }
}
