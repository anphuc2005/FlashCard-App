package com.example.flashcardapp.presentation.feature.learning

import android.os.Bundle
import android.speech.tts.TextToSpeech
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
import com.example.flashcardapp.databinding.FragmentBackCardBinding
import com.example.flashcardapp.presentation.common.dialog.accountDialog.AppConfirmDialog
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

private const val SECONDS_PER_MINUTE = 60L
private const val FLIP_ANIMATION_DURATION = 110L
private const val FLIP_CAMERA_DISTANCE = 9000f
private const val TTS_UTTERANCE_ID = "back_card_answer_tts"
private const val TAG = "BackCardFragment"

class BackCardFragment : Fragment() {

    private var _binding: FragmentBackCardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FlashCardViewModel by activityViewModels()
    private var isFlipAnimating = false
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false
    private var pendingSpeechText: String? = null
    private var activeSpeechLocale: Locale? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBackCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        initTextToSpeech()
        animateEntryFromFront()
        observeViewModel()
    }

    override fun onStop() {
        stopSpeaking()
        super.onStop()
    }

    override fun onDestroyView() {
        releaseTextToSpeech()
        Glide.with(this).clear(binding.cardImage)
        _binding = null
        super.onDestroyView()
    }

    private fun setupClickListeners() {
        binding.btnClose.setOnClickListener { showExitLearningDialog() }
        binding.cardAnswer.setOnClickListener { flipBackToQuestion() }
        binding.btnSpeakAnswer.setOnClickListener { speakCurrentAnswer() }
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
        binding.tvProgress.text = if (state.totalSessionCards == 0) {
            getString(R.string.learning_progress_empty)
        } else {
            state.progressLabel
        }
        binding.progressBar.setProgress(state.progressPercent)
        binding.cardTitle.text = card?.question ?: getString(R.string.learning_no_cards)
        binding.cardDesc.text = card?.answer ?: getString(R.string.learning_no_answer)
        binding.tagAnswer.text = when {
            card == null -> getString(R.string.learning_answer_label)
            card.repetition > 0 -> getString(R.string.learning_card_badge_review)
            else -> getString(R.string.learning_card_badge_new)
        }
        binding.btnSpeakAnswer.isEnabled = !card?.answer.isNullOrBlank()
        renderTimer(state.timeRemainingSeconds)
        loadCardImage(card?.localImagePath?.takeIf { File(it).exists() } ?: card?.imageUrl)
    }

    private fun rateCard(rating: LearningRating) {
        if (isFlipAnimating) return
        val currentState = viewModel.uiState.value
        if (currentState.isCompleted) {
            Log.w(TAG, "rateCard ignored: session already completed")
            return
        }
        Log.d(
            TAG,
            "rateCard clicked: deckId=${currentState.deckId}, cardId=${currentState.currentCard?.id}, rating=$rating, index=${currentState.currentIndex}"
        )
        stopSpeaking()
        val completed = viewModel.rateCurrentCard(rating)
        val actionId = if (completed) {
            R.id.action_backCardFragment_to_studyResultFragment
        } else {
            R.id.action_backCardFragment_to_frontCardFragment
        }
        Log.d(TAG, "rateCard navigate: completed=$completed, actionId=$actionId")
        if (completed) {
            findNavController().navigate(actionId)
            return
        }
        animateCardFlipOut(binding.cardAnswer, targetRotation = -90f) {
            val navController = findNavController()
            if (navController.currentDestination?.id == R.id.backCardFragment) {
                navController.navigate(actionId)
            }
        }
    }

    private fun flipBackToQuestion() {
        if (isFlipAnimating) return
        val currentState = viewModel.uiState.value
        if (currentState.isCompleted) return
        stopSpeaking()
        animateCardFlipOut(binding.cardAnswer, targetRotation = -90f) {
            val navController = findNavController()
            if (navController.currentDestination?.id == R.id.backCardFragment) {
                navController.navigate(R.id.action_backCardFragment_to_frontCardFragment)
            }
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
                speakText(pending)
            }
        }
    }

    private fun speakCurrentAnswer() {
        val answer = viewModel.uiState.value.currentCard?.answer?.trim().orEmpty()
        if (answer.isBlank()) {
            showToast(R.string.learning_no_answer)
            return
        }
        if (!isTtsReady) {
            pendingSpeechText = answer
            showToast(R.string.learning_tts_initializing)
            initTextToSpeech()
            return
        }
        speakText(answer)
    }

    private fun speakText(text: String) {
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

    private fun loadCardImage(imageUrl: String?) {
        binding.cardImage.visibility = View.VISIBLE
        if (imageUrl.isNullOrBlank()) {
            Log.d(TAG, "loadCardImage fallback placeholder used")
            binding.cardImage.setImageResource(R.drawable.test)
            return
        }

        Log.d(TAG, "loadCardImage remote url=$imageUrl")
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.test)
            .error(R.drawable.test)
            .centerCrop()
            .into(binding.cardImage)
    }

    private fun handleCompletion(state: LearningUiState) {
        if (!state.isCompleted || !state.isTimeExpired) return
        Log.w(TAG, "handleCompletion: session completed by timeout, deckId=${state.deckId}")
        val navController = findNavController()
        if (navController.currentDestination?.id == R.id.backCardFragment) {
            navController.navigate(R.id.action_backCardFragment_to_studyResultFragment)
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

    private fun animateEntryFromFront() {
        val navController = findNavController()
        if (navController.previousBackStackEntry?.destination?.id != R.id.frontCardFragment) return
        val card = binding.cardAnswer
        card.post {
            card.cameraDistance = resources.displayMetrics.density * FLIP_CAMERA_DISTANCE
            card.rotationY = 90f
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
