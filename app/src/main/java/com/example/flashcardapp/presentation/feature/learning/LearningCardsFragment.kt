package com.example.flashcardapp.presentation.feature.learning

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentLearningCardsBinding
import com.example.flashcardapp.databinding.ItemLearningRatingCardBinding
import com.example.flashcardapp.presentation.common.dialog.accountDialog.AppConfirmDialog
import com.example.flashcardapp.presentation.feature.learning.adapter.LearningSessionPagerAdapter
import kotlinx.coroutines.launch
import java.util.Locale

private const val SECONDS_PER_MINUTE = 60L
private const val FLIP_HALF_DURATION = 110L
private const val FLIP_CAMERA_DISTANCE = 9000f
private const val TTS_UTTERANCE_ID = "learning_cards_answer_tts"
private const val TAG = "LearningCardsFragment"

class LearningCardsFragment : Fragment() {

    private var _binding: FragmentLearningCardsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FlashCardViewModel by activityViewModels()
    private lateinit var pagerAdapter: LearningSessionPagerAdapter
    private var isFlipAnimating = false
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false
    private var pendingSpeechText: String? = null
    private var activeSpeechLocale: Locale? = null

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            stopSpeaking()
            viewModel.setCurrentIndex(position)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLearningCardsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTextToSpeech()
        setupPager()
        setupRatingCards()
        setupClickListeners()
        observeViewModel()
    }

    override fun onStop() {
        stopSpeaking()
        super.onStop()
    }

    override fun onDestroyView() {
        releaseTextToSpeech()
        binding.viewPagerCards.unregisterOnPageChangeCallback(pageChangeCallback)
        _binding = null
        super.onDestroyView()
    }

    private fun setupPager() {
        pagerAdapter = LearningSessionPagerAdapter(
            onCardTapped = ::onCardTapped,
            onSpeakTapped = ::onSpeakAnswerRequested
        )
        binding.viewPagerCards.apply {
            adapter = pagerAdapter
            offscreenPageLimit = 1
            registerOnPageChangeCallback(pageChangeCallback)
        }
    }

    private fun setupClickListeners() {
        binding.btnClose.setOnClickListener { showExitLearningDialog() }
        binding.cardAgain.root.setOnClickListener { rateCurrentCard(LearningRating.AGAIN) }
        binding.cardHard.root.setOnClickListener { rateCurrentCard(LearningRating.HARD) }
        binding.cardGood.root.setOnClickListener { rateCurrentCard(LearningRating.GOOD) }
        binding.cardEasy.root.setOnClickListener { rateCurrentCard(LearningRating.EASY) }
    }

    private fun setupRatingCards() {
        binding.cardAgain.ratingIcon.setImageResource(R.drawable.ic_again)
        binding.cardHard.ratingIcon.setImageResource(R.drawable.ic_difficult)
        binding.cardGood.ratingIcon.setImageResource(R.drawable.ic_good)
        binding.cardEasy.ratingIcon.setImageResource(R.drawable.ic_easy)
        binding.cardAgain.ratingText.setText(R.string.learning_rating_again)
        binding.cardHard.ratingText.setText(R.string.learning_rating_hard)
        binding.cardGood.ratingText.setText(R.string.learning_rating_good)
        binding.cardEasy.ratingText.setText(R.string.learning_rating_easy)
        binding.cardAgain.ratingDelay.setText(R.string.learning_rating_again_delay)
        binding.cardHard.ratingDelay.setText(R.string.learning_rating_hard_delay)
        binding.cardGood.ratingDelay.setText(R.string.learning_rating_good_delay)
        binding.cardEasy.ratingDelay.setText(R.string.learning_rating_easy_delay)
        applyRatingStyle(
            binding.cardAgain,
            R.color.learning_again_bg,
            R.color.learning_again_stroke,
            R.color.learning_again_text
        )
        applyRatingStyle(
            binding.cardHard,
            R.color.learning_hard_bg,
            R.color.learning_hard_stroke,
            R.color.learning_hard_text
        )
        applyRatingStyle(
            binding.cardGood,
            R.color.learning_good_bg,
            R.color.learning_good_stroke,
            R.color.learning_good_text
        )
        applyRatingStyle(
            binding.cardEasy,
            R.color.learning_easy_bg,
            R.color.learning_easy_stroke,
            R.color.learning_easy_text
        )
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

    private var hasHandledEmptySession = false

    private fun renderState(state: LearningUiState) {
        val cards = state.sessionCards
        if (pagerAdapter.currentList != cards) {
            pagerAdapter.submitList(cards)
        }

        val pageCount = cards.size
        val safeIndex = state.currentIndex.coerceIn(0, (pageCount - 1).coerceAtLeast(0))
        val hasCards = pageCount > 0
        binding.tvProgressValue.isVisible = hasCards
        binding.progressBar.isVisible = hasCards
        binding.tvProgressValue.text = if (pageCount == 0) {
            getString(R.string.learning_progress_empty)
        } else {
            state.progressLabel
        }
        binding.progressBar.setProgress(state.progressPercent)
        renderTimer(state.timeRemainingSeconds)
        val isBackFace = pagerAdapter.isPositionFlipped(safeIndex)
        updateBottomAction(isBackFace = isBackFace, enabled = !state.isCompleted)

        if (pageCount == 0) {
            if (!state.isLoading && !hasHandledEmptySession) {
                hasHandledEmptySession = true
                Log.w(TAG, "renderState: sessionCards empty and not loading, popping back")
                val errorMsg = state.errorMessage ?: getString(R.string.learning_no_cards)
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
                if (isAdded) {
                    findNavController().popBackStack()
                }
            }
            return
        }
        if (binding.viewPagerCards.currentItem != safeIndex) {
            binding.viewPagerCards.setCurrentItem(safeIndex, false)
        }
    }

    private fun handleCompletion(state: LearningUiState) {
        if (!state.isCompleted) return
        if (state.sessionCards.isEmpty()) return
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.learningCardsFragment) {
            navController.navigate(R.id.action_learningCardsFragment_to_studyResultFragment)
        }
    }

    private fun onCardTapped(position: Int) {
        if (isFlipAnimating || position != binding.viewPagerCards.currentItem) return
        stopSpeaking()
        if (pagerAdapter.isPositionFlipped(position)) {
            advanceFromBackFace()
            return
        }
        val holder = findCurrentCardView() ?: return
        animateFlip(holder, position)
    }

    private fun advanceFromBackFace() {
        val position = binding.viewPagerCards.currentItem
        if (!pagerAdapter.isPositionFlipped(position) || isFlipAnimating) return
        val completed = viewModel.rateCurrentCard(LearningRating.GOOD)
        pagerAdapter.resetFlipState(position)
        if (!completed) {
            val nextPosition = viewModel.uiState.value.currentIndex
            binding.viewPagerCards.setCurrentItem(nextPosition, true)
        }
    }

    private fun findCurrentCardView(): View? {
        val recyclerView = binding.viewPagerCards.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView
            ?: return null
        val holder = recyclerView.findViewHolderForAdapterPosition(binding.viewPagerCards.currentItem)
            ?: return null
        return holder.itemView.findViewById(R.id.cardSurface)
    }

    private fun animateFlip(cardView: View, position: Int) {
        isFlipAnimating = true
        cardView.cameraDistance = resources.displayMetrics.density * FLIP_CAMERA_DISTANCE
        cardView.animate()
            .rotationY(90f)
            .setDuration(FLIP_HALF_DURATION)
            .withEndAction {
                pagerAdapter.toggleFlipState(position)
                cardView.rotationY = -90f
                cardView.animate()
                    .rotationY(0f)
                    .setDuration(FLIP_HALF_DURATION)
                    .withEndAction {
                        isFlipAnimating = false
                        val isBackFace = pagerAdapter.isPositionFlipped(position)
                        updateBottomAction(isBackFace = isBackFace, enabled = true)
                    }
                    .start()
            }
            .start()
    }

    private fun rateCurrentCard(rating: LearningRating) {
        val position = binding.viewPagerCards.currentItem
        if (!pagerAdapter.isPositionFlipped(position)) return
        if (isFlipAnimating) return
        stopSpeaking()
        val completed = viewModel.rateCurrentCard(rating)
        pagerAdapter.resetFlipState(position)
        if (!completed) {
            val nextPosition = viewModel.uiState.value.currentIndex
            binding.viewPagerCards.setCurrentItem(nextPosition, true)
        }
    }

    private fun initTextToSpeech() {
        if (textToSpeech != null) return
        textToSpeech = TextToSpeech(requireContext().applicationContext) { status ->
            if (status != TextToSpeech.SUCCESS) {
                isTtsReady = false
                Log.e(TAG, "initTextToSpeech failed with status=$status")
                showToast(R.string.learning_tts_unavailable)
                return@TextToSpeech
            }

            isTtsReady = true
            pendingSpeechText?.let { pending ->
                pendingSpeechText = null
                speakTextInternal(pending)
            }
        }
    }

    private fun onSpeakAnswerRequested(text: String) {
        val normalized = text.trim()
        if (normalized.isBlank()) {
            showToast(R.string.learning_no_answer)
            return
        }
        if (!isTtsReady) {
            pendingSpeechText = normalized
            showToast(R.string.learning_tts_initializing)
            initTextToSpeech()
            return
        }
        speakTextInternal(normalized)
    }

    private fun speakTextInternal(text: String) {
        val ttsEngine = textToSpeech ?: run {
            initTextToSpeech()
            pendingSpeechText = text
            return
        }
        if (!applyDetectedLanguage(ttsEngine, text)) {
            showToast(R.string.learning_tts_not_supported)
            return
        }
        val result = ttsEngine.speak(text, TextToSpeech.QUEUE_FLUSH, null, TTS_UTTERANCE_ID)
        if (result == TextToSpeech.ERROR) {
            Log.e(TAG, "speakText failed to queue text")
            showToast(R.string.learning_tts_unavailable)
        }
    }

    private fun applyDetectedLanguage(ttsEngine: TextToSpeech, text: String): Boolean {
        val targetLocale = TtsLanguageResolver.resolveBestSupportedLocale(
            tts = ttsEngine,
            text = text,
            defaultLocale = Locale.getDefault()
        )
        if (activeSpeechLocale == targetLocale) return true

        val setResult = ttsEngine.setLanguage(targetLocale)
        val supported = TtsLanguageResolver.isLanguageResultSupported(setResult)
        if (!supported) {
            Log.w(TAG, "applyDetectedLanguage failed: locale=$targetLocale, result=$setResult")
            return false
        }
        activeSpeechLocale = targetLocale
        Log.d(TAG, "TTS locale selected: $targetLocale")
        return true
    }

    private fun stopSpeaking() {
        textToSpeech?.stop()
    }

    private fun releaseTextToSpeech() {
        stopSpeaking()
        textToSpeech?.shutdown()
        textToSpeech = null
        isTtsReady = false
        pendingSpeechText = null
        activeSpeechLocale = null
    }

    private fun showToast(messageRes: Int) {
        if (!isAdded) return
        Toast.makeText(requireContext(), messageRes, Toast.LENGTH_SHORT).show()
    }

    private fun updateBottomAction(isBackFace: Boolean, enabled: Boolean) {
        binding.ratingSection.visibility = View.GONE
        val canRate = isBackFace && enabled
        val alpha = if (canRate) 1f else 0.5f
        binding.cardAgain.root.isEnabled = canRate
        binding.cardHard.root.isEnabled = canRate
        binding.cardGood.root.isEnabled = canRate
        binding.cardEasy.root.isEnabled = canRate
        binding.cardAgain.root.alpha = alpha
        binding.cardHard.root.alpha = alpha
        binding.cardGood.root.alpha = alpha
        binding.cardEasy.root.alpha = alpha
    }

    private fun applyRatingStyle(
        binding: ItemLearningRatingCardBinding,
        backgroundColor: Int,
        strokeColor: Int,
        textColor: Int
    ) {
        val context = requireContext()
        val resolvedTextColor = ContextCompat.getColor(context, textColor)
        binding.root.setCardBackgroundColor(ContextCompat.getColor(context, backgroundColor))
        binding.root.strokeColor = ContextCompat.getColor(context, strokeColor)
        binding.ratingText.setTextColor(resolvedTextColor)
        binding.ratingDelay.setTextColor(resolvedTextColor)
        binding.ratingIcon.setColorFilter(resolvedTextColor)
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
}
