package com.example.flashcardapp.presentation.feature.learning

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentSessionSettingBinding
import kotlinx.coroutines.launch

private const val MIN_CARD_LIMIT = 1
private const val TAG = "SessionSettingFragment"

class SessionSettingFragment : Fragment() {

    private var _binding: FragmentSessionSettingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FlashCardViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSessionSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.rbRandom.setOnClickListener { selectOrder(LearningCardOrder.RANDOM) }
        binding.cardRandom.setOnClickListener { selectOrder(LearningCardOrder.RANDOM) }
        binding.rbOrdered.setOnClickListener { selectOrder(LearningCardOrder.ORDERED) }
        binding.cardOrdered.setOnClickListener { selectOrder(LearningCardOrder.ORDERED) }
        binding.rbTimeAttack.setOnClickListener { selectTimeAttackMode() }
        binding.cardTimeAttack.setOnClickListener { selectTimeAttackMode() }
        binding.rbSpacedRepetition.setOnClickListener { selectSpacedRepetitionMode() }
        binding.cardSpacedRepetition.setOnClickListener { selectSpacedRepetitionMode() }
        binding.chipAll.setOnClickListener { viewModel.setFilter(LearningCardFilter.ALL) }
        binding.chipNew.setOnClickListener { viewModel.setFilter(LearningCardFilter.NEW) }
        binding.chipReview.setOnClickListener { viewModel.setFilter(LearningCardFilter.REVIEW) }
        binding.chipTimeOneMinute.setOnClickListener { viewModel.setTimeLimitMinutes(1) }
        binding.chipTimeThreeMinutes.setOnClickListener { viewModel.setTimeLimitMinutes(3) }
        binding.chipTimeFiveMinutes.setOnClickListener { viewModel.setTimeLimitMinutes(5) }
        binding.customSlider.onValueChanged = { value ->
            viewModel.setCardLimit(value.toInt())
        }
        binding.btnStart.setOnClickListener {
            val state = viewModel.uiState.value
            preloadDeckCardImages()
            Log.d(
                TAG,
                "Start session clicked: deckId=${state.deckId}, mode=${state.settings.mode}, order=${state.settings.order}, filter=${state.settings.filter}, cardLimit=${state.settings.cardLimit}, timeLimit=${state.settings.timeLimitMinutes}"
            )
            viewModel.startSession { isReady ->
                Log.d(TAG, "startSession callback: isReady=$isReady, currentDestination=${findNavController().currentDestination?.id}")
                if (isReady && isAdded) {
                    findNavController().navigate(R.id.action_sessionSettingFragment_to_learningCardsFragment)
                }
            }
        }
    }

    private fun preloadDeckCardImages() {
        val imageUrls = viewModel.uiState.value.cards
            .mapNotNull { it.imageUrl?.trim() }
            .filter { it.isNotEmpty() }
            .distinct()

        if (imageUrls.isEmpty()) return

        Log.d(TAG, "preloadDeckCardImages: count=${imageUrls.size}")
        imageUrls.forEach { url ->
            Glide.with(this)
                .load(url)
                .preload()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state)
                    state.errorMessage?.let { message ->
                        Log.e(TAG, "UI error received: deckId=${state.deckId}, message=$message")
                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        viewModel.clearError()
                    }
                }
            }
        }
    }

    private fun renderState(state: LearningUiState) {
        Log.d(
            TAG,
            "renderState: deckId=${state.deckId}, totalCards=${state.cards.size}, cardLimit=${state.settings.cardLimit}, mode=${state.settings.mode}, filter=${state.settings.filter}, loading=${state.isLoading}"
        )
        val maxCards = state.cards.size.coerceAtLeast(MIN_CARD_LIMIT)
        binding.customSlider.minValue = MIN_CARD_LIMIT.toFloat()
        binding.customSlider.maxValue = maxCards.toFloat()
        binding.customSlider.value = state.settings.cardLimit.coerceIn(MIN_CARD_LIMIT, maxCards).toFloat()
        binding.textCardLimit.text = getString(
            R.string.learning_session_limit_value,
            state.settings.cardLimit.coerceAtMost(maxCards),
            maxCards
        )
        binding.rbRandom.isChecked = state.settings.order == LearningCardOrder.RANDOM &&
            state.settings.mode == LearningStudyMode.RANDOM
        binding.rbOrdered.isChecked = state.settings.order == LearningCardOrder.ORDERED &&
            state.settings.mode == LearningStudyMode.SEQUENTIAL
        binding.rbTimeAttack.isChecked = state.settings.mode == LearningStudyMode.TIME_ATTACK
        binding.rbSpacedRepetition.isChecked = state.settings.mode == LearningStudyMode.SPACED_REPETITION
        binding.chipAll.isChecked = state.settings.filter == LearningCardFilter.ALL
        binding.chipNew.isChecked = state.settings.filter == LearningCardFilter.NEW
        binding.chipReview.isChecked = state.settings.filter == LearningCardFilter.REVIEW
        binding.chipTimeOneMinute.isChecked = state.settings.timeLimitMinutes == 1
        binding.chipTimeThreeMinutes.isChecked = state.settings.timeLimitMinutes == 3
        binding.chipTimeFiveMinutes.isChecked = state.settings.timeLimitMinutes == 5
        renderTimeAttackOptions(state.settings.mode == LearningStudyMode.TIME_ATTACK)
        binding.btnStart.isEnabled = state.cards.isNotEmpty()
    }

    private fun selectOrder(order: LearningCardOrder) {
        Log.d(TAG, "selectOrder: order=$order")
        viewModel.setOrder(order)
    }

    private fun selectTimeAttackMode() {
        Log.d(TAG, "selectTimeAttackMode")
        viewModel.setStudyMode(LearningStudyMode.TIME_ATTACK)
    }

    private fun selectSpacedRepetitionMode() {
        Log.d(TAG, "selectSpacedRepetitionMode")
        viewModel.setStudyMode(LearningStudyMode.SPACED_REPETITION)
    }

    private fun renderTimeAttackOptions(isEnabled: Boolean) {
        binding.layoutTimeLimit.alpha = if (isEnabled) 1f else 0.45f
        binding.chipTimeOneMinute.isEnabled = isEnabled
        binding.chipTimeThreeMinutes.isEnabled = isEnabled
        binding.chipTimeFiveMinutes.isEnabled = isEnabled
    }
}
