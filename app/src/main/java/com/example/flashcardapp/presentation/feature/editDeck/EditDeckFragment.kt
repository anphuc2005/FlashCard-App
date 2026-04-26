package com.example.flashcardapp.presentation.feature.editDeck

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.flashcardapp.presentation.common.dialog.accountDialog.AppConfirmDialog
import com.example.flashcardapp.presentation.common.notification.showAppError
import com.example.flashcardapp.presentation.common.notification.showAppSuccess
import com.example.flashcardapp.presentation.common.notification.showAppWarning
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
            onEditClick = { card ->
                val bundle = Bundle().apply {
                    putString("CARD_ID", card.id)
                    putString("QUESTION", card.question)
                    putString("ANSWER", card.answer)
                    putString("DECK_ID", card.deckId)
                }
                findNavController().navigate(R.id.action_editDeckFragment_to_editCardFragment, bundle)
            },
            onDeleteClick = { card ->
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
                        viewModel.deleteCard(card)
                        showAppSuccess(getString(R.string.delete_success_card))
                    }
                }
                dialog.show(childFragmentManager, "delete_card_confirm")
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
                showAppWarning("Tên bộ thẻ không được để trống")
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
                            is EditDeckState.Loading -> Unit
                            is EditDeckState.Success -> {
                                binding.etDeckName.setText(state.deck.name)
                                binding.etDeckDescription.setText(state.deck.description)
                                binding.switchPublic.isChecked = state.deck.isPublic
                                println("Deck is public: ${state.deck.isPublic} ${state.deck.name}")
                                updateSwitchTint(state.deck.isPublic)
                                binding.tvCardCountLabel.text = "Số lượng thẻ (${state.deck.cardCount})"
                            }
                            is EditDeckState.UpdateSuccess -> {
                                showAppSuccess("Đã cập nhật thành công!")
                                requireActivity().finish()
                            }
                            is EditDeckState.Error -> {
                                showAppError(state.message)
                            }
                            else -> Unit
                        }
                    }
                }
                launch {
                    viewModel.cardsState.collect { cards ->
                        editDeckCardAdapter.submitList(cards)
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
