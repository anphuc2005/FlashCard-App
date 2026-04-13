package com.example.flashcardapp.presentation.feature.addDeck

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentAddDeskBinding
import kotlinx.coroutines.launch

class AddDeckFragment : Fragment() {
    private lateinit var binding: FragmentAddDeskBinding

    private val viewModel: AddDeckViewModel by viewModels {
        AddDeckViewModelFactory(
            (requireActivity().application as FlashcardApp).container.addDeckUseCase
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddDeskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { requireActivity().finish() }

        binding.switchPublic.setOnCheckedChangeListener { _, _ ->
            updateSwitchTint()
        }
        
        binding.btnNext.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val description = binding.etDesc.text.toString().trim()
            val isPublic = binding.switchPublic.isChecked
            
            viewModel.createDeck(name, description, isPublic)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AddDeckState.Idle -> {
                            // Do nothing
                        }
                        is AddDeckState.Loading -> {
                            // Optionally show loading
                        }
                        is AddDeckState.Success -> {
                            val bundle = bundleOf("DECK_ID" to state.deck.id)
                            findNavController().navigate(
                                R.id.action_addDeckFragment_to_addCardFragment, 
                                bundle
                            )
                            viewModel.resetState()
                        }
                        is AddDeckState.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun updateSwitchTint() {
        fun tintSwitch(checked: Boolean, thumbRes: Int, trackRes: Int): Pair<ColorStateList, ColorStateList> {
            val thumb = if (checked) thumbRes else R.color.switch_thumb_gray
            val track = if (checked) trackRes else R.color.switch_track_gray
            val thumbList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), thumb))
            val trackList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), track))
            return thumbList to trackList
        }

        val (studyThumb, studyTrack) = tintSwitch(binding.switchPublic.isChecked, R.color.switch_thumb_blue, R.color.switch_track_blue)
        binding.switchPublic.thumbTintList = studyThumb
        binding.switchPublic.trackTintList = studyTrack
    }
}

