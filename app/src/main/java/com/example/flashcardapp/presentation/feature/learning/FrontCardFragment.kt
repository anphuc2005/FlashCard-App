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
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentFrontCardBinding
import com.example.flashcardapp.presentation.common.dialog.accountDialog.AppConfirmDialog
import kotlinx.coroutines.launch

private const val SECONDS_PER_MINUTE = 60L
private const val FLIP_ANIMATION_DURATION = 110L
private const val FLIP_CAMERA_DISTANCE = 9000f
private const val TAG = "FrontCardFragment"

class FrontCardFragment : Fragment() {

    private var _binding: FragmentFrontCardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FlashCardViewModel by activityViewModels()
    private var isFlipAnimating = false

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
        animateEntryFromBack()
        observeViewModel()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupClickListeners() {
        binding.btnClose.setOnClickListener { showExitLearningDialog() }
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
        Log.d(
            TAG,
            "renderState: deckId=${state.deckId}, currentIndex=${state.currentIndex}, total=${state.totalSessionCards}, cardId=${card?.id}, completed=${state.isCompleted}, timeRemaining=${state.timeRemainingSeconds}"
        )
        binding.tvProgressValue.text = if (state.totalSessionCards == 0) {
            getString(R.string.learning_progress_empty)
        } else {
            state.progressLabel
        }
        binding.progressBar.setProgress(state.progressPercent)
        binding.questionText.text = card?.question ?: getString(R.string.learning_no_cards)
        binding.questionLabel.text = when {
            card == null -> getString(R.string.learning_question_label)
            card.repetition > 0 -> getString(R.string.learning_card_badge_review)
            else -> getString(R.string.learning_card_badge_new)
        }
        renderTimer(state.timeRemainingSeconds)
    }

    private fun navigateToAnswer() {
        if (isFlipAnimating) return
        val currentCardId = viewModel.uiState.value.currentCard?.id
        Log.d(TAG, "navigateToAnswer: cardId=$currentCardId")
        animateCardFlipOut(binding.cardFront, targetRotation = 90f) {
            val navController = findNavController()
            if (navController.currentDestination?.id == R.id.frontCardFragment) {
                navController.navigate(R.id.action_frontCardFragment_to_backCardFragment)
            }
        }
    }

    private fun handleCompletion(state: LearningUiState) {
        if (!state.isCompleted || !state.isTimeExpired) return
        Log.w(TAG, "handleCompletion: session completed by timeout, deckId=${state.deckId}")
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

    private fun showExitLearningDialog() {
        val dialog = AppConfirmDialog.newInstance(
            title = getString(R.string.learning_exit_dialog_title),
            message = getString(R.string.learning_exit_dialog_message),
            confirmText = getString(R.string.learning_exit_dialog_exit),
            cancelText = getString(R.string.learning_exit_dialog_continue),
            iconRes = R.drawable.ic_close,
            destructive = true
        )
        dialog.listener = object : AppConfirmDialog.Listener {
            override fun onConfirm() {
                viewLifecycleOwner.lifecycleScope.launch {
                    viewModel.persistCurrentSessionStateNow()
                    if (isAdded) {
                        requireActivity().finish()
                    }
                }
            }
        }
        dialog.show(childFragmentManager, "learning_exit_confirm")
    }

    private fun animateEntryFromBack() {
        val navController = findNavController()
        if (navController.previousBackStackEntry?.destination?.id != R.id.backCardFragment) return
        val card = binding.cardFront
        card.post {
            card.cameraDistance = resources.displayMetrics.density * FLIP_CAMERA_DISTANCE
            card.rotationY = -90f
            card.animate()
                .rotationY(0f)
                .setDuration(FLIP_ANIMATION_DURATION)
                .start()
        }
    }

    private fun animateCardFlipOut(card: View, targetRotation: Float, onEnd: () -> Unit) {
        isFlipAnimating = true
        card.post {
            card.cameraDistance = resources.displayMetrics.density * FLIP_CAMERA_DISTANCE
            card.animate()
                .rotationY(targetRotation)
                .setDuration(FLIP_ANIMATION_DURATION)
                .withEndAction {
                    card.rotationY = 0f
                    isFlipAnimating = false
                    onEnd()
                }
                .start()
        }
    }
}
