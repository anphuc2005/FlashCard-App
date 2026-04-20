package com.example.flashcardapp.presentation.feature.editDeck

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.databinding.FragmentEditCardBinding
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
        
        // Cập nhật header text để hiển thị "Sửa thẻ" (nếu layout có headerTitle)
        binding.headerTitle.text = "Sửa thẻ"
        
        // Lấy argument do Navigation truyền vào
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

        // Đổi btnSave -> để update
        binding.btnSave.setOnClickListener {
            val question = binding.etFront.text.toString()
            val answer = binding.etBack.text.toString()
            viewModel.updateCard(cardId, question, answer, deckId, null)
        }

        // Có thể setup nút btnAddAnother để đổi thành nút Xóa thẻ (nếu muốn)
        // hoặc tự thêm logic xoá
        binding.btnAddAnother.setOnClickListener {
            viewModel.deleteCard(cardId, initialQuestion, initialAnswer, deckId)
        }
        binding.btnAddAnother.text = "Xoá thẻ"
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is EditCardState.Idle -> {}
                        is EditCardState.Loading -> {}
                        is EditCardState.Success -> {
                            Toast.makeText(requireContext(), "Cập nhật thẻ thành công", Toast.LENGTH_SHORT).show()
                            viewModel.resetState()
                            findNavController().popBackStack()
                        }
                        is EditCardState.Deleted -> {
                            Toast.makeText(requireContext(), "Xoá thẻ thành công", Toast.LENGTH_SHORT).show()
                            viewModel.resetState()
                            findNavController().popBackStack()
                        }
                        is EditCardState.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
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
