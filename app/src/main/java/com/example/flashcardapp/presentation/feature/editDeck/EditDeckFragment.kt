package com.example.flashcardapp.presentation.feature.editDeck

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentEditDeckBinding
import com.example.flashcardapp.presentation.common.adapter.EditDeckCardAdapter
import kotlinx.coroutines.launch

class EditDeckFragment : Fragment() {

    private var _binding: FragmentEditDeckBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditDeckViewModel by viewModels {
        val container = (requireActivity().application as FlashcardApp).container
        EditDeckViewModelFactory(
            container.getDeckByIdUseCase,
            container.updateDeckUseCase,
            container.getCardsByDeckIdUseCase,
            container.deleteFlashCardUseCase
        )
    }

    private var deckId: String? = null
    private lateinit var editDeckCardAdapter: EditDeckCardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Nhận ID truyền từ Navigation Graph args hoặc Intent
        deckId = arguments?.getString("DECK_ID") ?: arguments?.getString("DECK_ID_STR")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditDeckBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViews()
        setupListeners()
        observeData()

        deckId?.let { id ->
            viewModel.getDeckDetail(id)
        }
    }

    private fun setupViews() {
        editDeckCardAdapter = EditDeckCardAdapter(
            onEditClick = { card ->// Navigate to edit card, passing card details if needed
                val bundle = Bundle().apply {
                    putString("CARD_ID", card.id)
                    putString("QUESTION", card.question)
                    putString("ANSWER", card.answer)
                    putString("DECK_ID", card.deckId)
                }
                findNavController().navigate(R.id.action_editDeckFragment_to_editCardFragment, bundle) 
            },
            onDeleteClick = { card ->
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Xoá thẻ")
                    .setMessage("Bạn có chắc chắn muốn xoá thẻ này không?")
                    .setPositiveButton("Xoá") { _, _ ->
                        viewModel.deleteCard(card)
                        Toast.makeText(requireContext(), "Đã xoá thẻ", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Huỷ", null)
                    .show()
            }
        )

        binding.rvCards.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCards.adapter = editDeckCardAdapter
        binding.tvTitle.text = "Sửa bộ thẻ"
    }

    private fun setupListeners() {
        binding.btnClose.setOnClickListener {
            requireActivity().finish()
        }

        binding.btnAddCard.setOnClickListener {
            findNavController().navigate(R.id.action_editDeckFragment_to_editCardFragment)
        }

        binding.switchPublic.setOnCheckedChangeListener { _, isChecked ->
            updateSwitchTint(isChecked)
        }

        binding.btnSave.setOnClickListener {
            val deckName = binding.etDeckName.text.toString().trim()
            val description = binding.etDeckDescription.text.toString().trim()
            val isPublic = binding.switchPublic.isChecked
            
            if (deckName.isEmpty()) {
                Toast.makeText(requireContext(), "Tên bộ thẻ không được để trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            deckId?.let { id ->
                viewModel.updateDeck(id, deckName, description, isPublic)
            }
        }
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.deckState.collect { state ->
                        when (state) {
                            is EditDeckState.Loading -> {
                                // Hiện loading (tuỳ chọn)
                            }
                            is EditDeckState.Success -> {
                                binding.etDeckName.setText(state.deck.name)
                                binding.etDeckDescription.setText(state.deck.description)
                                binding.switchPublic.isChecked = state.deck.isPublic
                                println("Deck is public: ${state.deck.isPublic} ${state.deck.name}")
                                updateSwitchTint(state.deck.isPublic)
                                binding.tvCardCountLabel.text = "Số lượng thẻ (${state.deck.cardCount})"
                            }
                            is EditDeckState.UpdateSuccess -> {
                                Toast.makeText(context, "Cập nhật thành công!", Toast.LENGTH_SHORT).show()
                                requireActivity().finish()
                            }
                            is EditDeckState.Error -> {
                                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                            }
                            else -> {}
                        }
                    }
                }
                launch {
                    viewModel.cardsState.collect { cards ->
                        editDeckCardAdapter.submitList(cards)
                        // Update UI label if desired (so it depends on cards API response)
                        if (cards.isNotEmpty()) {
                            binding.tvCardCountLabel.text = "Số lượng thẻ (${cards.size})"
                        }
                    }
                }
            }
        }
    }

    private fun updateSwitchTint(isChecked: Boolean) {
        val thumbColor = ContextCompat.getColor(
            requireContext(),
            if (isChecked) R.color.switch_thumb_blue else R.color.switch_thumb_gray
        )
        val trackColor = ContextCompat.getColor(
            requireContext(),
            if (isChecked) R.color.switch_track_blue else R.color.switch_track_gray
        )
        binding.switchPublic.thumbTintList = ColorStateList.valueOf(thumbColor)
        binding.switchPublic.trackTintList = ColorStateList.valueOf(trackColor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
