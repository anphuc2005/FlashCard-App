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
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentStudyResultBinding
import kotlinx.coroutines.launch

private const val SECONDS_PER_MINUTE = 60L

class StudyResultFragment : Fragment() {

    private var _binding: FragmentStudyResultBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FlashCardViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStudyResultBinding.inflate(inflater, container, false)
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
        binding.btnBack.setOnClickListener { finishAndPersistSession() }
        binding.btnContinue.setOnClickListener { finishAndPersistSession() }
        binding.cardStudiedResult.resultIcon.setImageResource(R.drawable.ic_cards)
        binding.cardCorrectResult.resultIcon.setImageResource(R.drawable.ic_correct)
        binding.cardIncorrectResult.resultIcon.setImageResource(R.drawable.ic_incorrect)
        binding.cardTimeResult.resultIcon.setImageResource(R.drawable.ic_time)
        binding.cardStudiedResult.resultLabel.setText(R.string.learning_result_studied)
        binding.cardCorrectResult.resultLabel.setText(R.string.learning_result_correct)
        binding.cardIncorrectResult.resultLabel.setText(R.string.learning_result_incorrect)
        binding.cardTimeResult.resultLabel.setText(R.string.learning_result_time)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    renderResult(state.result)
                }
            }
        }
    }

    private fun renderResult(result: LearningResult) {
        binding.cardStudiedResult.resultValue.text = result.studiedCount.toString()
        binding.cardCorrectResult.resultValue.text = getString(
            R.string.learning_result_score_format,
            result.correctCount,
            result.studiedCount
        )
        binding.cardIncorrectResult.resultValue.text = getString(
            R.string.learning_result_score_format,
            result.incorrectCount,
            result.studiedCount
        )
        binding.cardTimeResult.resultValue.text = formatElapsedTime(result.elapsedSeconds)
    }

    private fun formatElapsedTime(totalSeconds: Long): String {
        val minutes = totalSeconds / SECONDS_PER_MINUTE
        val seconds = totalSeconds % SECONDS_PER_MINUTE
        return getString(R.string.learning_result_time_format, minutes, seconds)
    }

    private fun finishAndPersistSession() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.persistCurrentSessionStateNow()
            if (isAdded) {
                requireActivity().finish()
            }
        }
    }
}
