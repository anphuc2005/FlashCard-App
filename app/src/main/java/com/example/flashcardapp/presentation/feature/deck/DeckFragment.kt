package com.example.flashcardapp.presentation.feature.deck

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.databinding.FragmentDeckBinding
import com.example.flashcardapp.presentation.common.adapter.DeckAdapter
import com.example.flashcardapp.presentation.feature.addDeck.AddDeckContainerActivity
import com.example.flashcardapp.presentation.feature.learning.LearningActivity
import androidx.fragment.app.viewModels
import kotlinx.coroutines.launch

class DeckFragment : Fragment() {

    private lateinit var binding: FragmentDeckBinding
    private val deckViewModel: DeckViewModel by viewModels {
        DeckViewModelFactory((requireActivity().application as FlashcardApp).container.deckRepository)
    }
    private lateinit var deckAdapter: DeckAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeckBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize RecyclerView with DeckAdapter
        setupRecyclerView()

        // Observe deckUiState from ViewModel
        lifecycleScope.launch {
            deckViewModel.deckUiState.collect { uiState ->
                handleUiState(uiState)
            }
        }

        // Setup click listeners
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        deckAdapter = DeckAdapter(
            onItemClick = { deck ->
                deckViewModel.updateDeckLastStudied(deck)
                
                val intent = Intent(requireActivity(), LearningActivity::class.java).apply {
                    putExtra("DECK_ID", deck.id)
                }
                startActivity(intent)
                Toast.makeText(requireContext(), "Opening ${deck.name}", Toast.LENGTH_SHORT).show()
            }
        )

        binding.decksRecycler.apply {
            adapter = deckAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupClickListeners() {
        binding.btnAddDesk.setOnClickListener {
            val intent = Intent(requireContext(), AddDeckContainerActivity::class.java)
            startActivity(intent)
        }

        binding.filterButton.setOnClickListener {
            Toast.makeText(requireContext(), "Filter decks", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleUiState(uiState: DeckUiState) {
        when (uiState) {
            is DeckUiState.Loading -> {
                showLoading()
            }
            is DeckUiState.Success -> {
                hideLoading()
                deckAdapter.submitList(uiState.decks)
            }
            is DeckUiState.Error -> {
                hideLoading()
                showError(uiState.message)
            }
            is DeckUiState.Empty -> {
                hideLoading()
                showEmpty()
            }
        }
    }

    private fun showLoading() {
        binding.decksRecycler.visibility = View.GONE
    }

    private fun hideLoading() {
        binding.decksRecycler.visibility = View.VISIBLE
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), "Error: $message", Toast.LENGTH_LONG).show()
    }

    private fun showEmpty() {
        Toast.makeText(requireContext(), "No decks available", Toast.LENGTH_SHORT).show()
    }
}

