package com.example.flashcardapp.presentation.feature.editDeck

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentEditCardBinding
import com.example.flashcardapp.presentation.common.dialog.accountDialog.AppConfirmDialog
import com.example.flashcardapp.presentation.common.notification.showAppError
import com.example.flashcardapp.presentation.common.notification.showAppSuccess
import kotlinx.coroutines.launch

class EditCardFragment : Fragment() {

    private var _binding: FragmentEditCardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditCardViewModel by viewModels {
        EditCardViewModelFactory(
            (requireActivity().application as FlashcardApp).container.updateFlashCardUseCase,
            (requireActivity().application as FlashcardApp).container.deleteFlashCardUseCase
        )
    }

    private var cardId: String = ""
    private var deckId: String = ""
    private var initialQuestion: String = ""
    private var initialAnswer: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.headerTitle.text = getString(R.string.edit_card_title)

        arguments?.let {
            cardId = it.getString("CARD_ID") ?: ""
            deckId = it.getString("DECK_ID") ?: ""
            initialQuestion = it.getString("QUESTION") ?: ""
            initialAnswer = it.getString("ANSWER") ?: ""

            binding.etFront.setText(initialQuestion)
            binding.etBack.setText(initialAnswer)
        }

        setupListeners()
        observeData()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnSave.setOnClickListener {
            val question = binding.etFront.text.toString()
            val answer = binding.etBack.text.toString()
            viewModel.updateCard(cardId, question, answer, deckId, null)
        }

        binding.btnAddAnother.setOnClickListener {
            val dialog = AppConfirmDialog.newInstance(
                title = getString(R.string.delete_confirm_title),
                message = getString(R.string.delete_confirm_message_card),
                confirmText = getString(R.string.delete_confirm_action),
                cancelText = getString(R.string.delete_confirm_cancel),
                iconRes = R.drawable.ic_delete,
                destructive = true
            )
            dialog.listener = object : AppConfirmDialog.Listener {
                override fun onConfirm() {
                    viewModel.deleteCard(cardId, initialQuestion, initialAnswer, deckId)
                }
            }
            dialog.show(childFragmentManager, "delete_card_confirm")
        }
        binding.btnAddAnother.text = getString(R.string.edit_card_delete)
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is EditCardState.Idle -> Unit
                        is EditCardState.Loading -> Unit
                        is EditCardState.Success -> {
                            showAppSuccess(getString(R.string.edit_card_update_success))
                            viewModel.resetState()
                            findNavController().popBackStack()
                        }
                        is EditCardState.Deleted -> {
                            showAppSuccess(getString(R.string.delete_success_card))
                            viewModel.resetState()
                            findNavController().popBackStack()
                        }
                        is EditCardState.Error -> {
                            showAppError(state.message)
                        }
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
