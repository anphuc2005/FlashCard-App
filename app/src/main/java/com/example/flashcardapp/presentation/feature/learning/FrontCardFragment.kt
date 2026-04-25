package com.example.flashcardapp.presentation.feature.learning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentFrontCardBinding
import kotlinx.coroutines.launch

private const val SECONDS_PER_MINUTE = 60L

class FrontCardFragment : Fragment() {

    private var _binding: FragmentFrontCardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FlashCardViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFrontCardBinding.inflate(inflater, container, false)
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
        binding.btnClose.setOnClickListener { requireActivity().finish() }
        binding.btnFlip.setOnClickListener { navigateToAnswer() }
        binding.cardFront.setOnClickListener { navigateToAnswer() }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderState(state)
                    handleCompletion(state)
                }
            }
        }
    }

    private fun renderState(state: LearningUiState) {
        val card = state.currentCard
        binding.tvProgressValue.text = if (state.totalSessionCards == 0) {
            getString(R.string.learning_progress_empty)
        } else {
            state.progressLabel
        }
        binding.progressBar.setProgress(state.progressPercent)
        binding.questionText.text = card?.question ?: getString(R.string.learning_no_cards)
        renderTimer(state.timeRemainingSeconds)
    }

    private fun navigateToAnswer() {
        findNavController().navigate(R.id.action_frontCardFragment_to_backCardFragment)
    }

    private fun handleCompletion(state: LearningUiState) {
        if (!state.isCompleted || !state.isTimeExpired) return
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.frontCardFragment) {
            navController.navigate(R.id.action_frontCardFragment_to_studyResultFragment)
        }
    }

    private fun renderTimer(timeRemainingSeconds: Long?) {
        if (timeRemainingSeconds == null) {
            binding.tvTimer.visibility = View.GONE
            return
        }
        binding.tvTimer.visibility = View.VISIBLE
        binding.tvTimer.text = formatTime(timeRemainingSeconds)
    }

    private fun formatTime(totalSeconds: Long): String {
        val minutes = totalSeconds / SECONDS_PER_MINUTE
        val seconds = totalSeconds % SECONDS_PER_MINUTE
        return getString(R.string.learning_result_time_format, minutes, seconds)
    }
}
