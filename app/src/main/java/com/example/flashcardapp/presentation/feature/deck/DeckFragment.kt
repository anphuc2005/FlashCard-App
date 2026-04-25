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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import androidx.core.content.ContextCompat
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.databinding.FragmentDeckBinding
import com.example.flashcardapp.presentation.common.adapter.DeckAdapter
import com.example.flashcardapp.presentation.feature.addDeck.AddDeckContainerActivity
import com.example.flashcardapp.presentation.feature.learning.LearningActivity
import androidx.fragment.app.viewModels
import com.example.flashcardapp.R
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
            },
            onItemEdit = { deck ->
                val intent = Intent(requireContext(), AddDeckContainerActivity::class.java).apply {
                    putExtra("DECK_ID", deck.id)
                    putExtra("IS_EDIT_MODE", true)
                }
                startActivity(intent)
            }
        )

        binding.decksRecycler.apply {
            adapter = deckAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val deckId = deckAdapter.currentList[position].id
                    
                    android.app.AlertDialog.Builder(requireContext())
                        .setTitle("Xoá bộ thẻ")
                        .setMessage("Bạn có chắc chắn muốn xoá bộ thẻ này không?")
                        .setPositiveButton("Xoá") { _, _ ->
                            deckViewModel.deleteDeck(deckId)
                            Toast.makeText(requireContext(), "Đã xoá bộ thẻ", Toast.LENGTH_SHORT).show()
                        }
                        .setNegativeButton("Huỷ", { dialog, _ ->
                            deckAdapter.notifyItemChanged(position)
                            dialog.dismiss()
                        })
                        .setOnCancelListener {
                            deckAdapter.notifyItemChanged(position)
                        }
                        .show()
                }
            }
            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val paint = Paint()
                val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete) // Make sure you have an ic_delete drawable

                val density = requireContext().resources.displayMetrics.density
                val buttonWidth = 50 * density
                val margin = 8 * density
                val maxSwipe = -(buttonWidth + margin * 2)
                
                // Giới hạn dX không cho kéo xa hơn maxSwipe
                val clampedDx = if (dX < maxSwipe) maxSwipe else dX

                if (clampedDx < 0) { // Swiping to the left
                    val radius = 15 * density

                    paint.color = Color.parseColor("#F37E33")
                    val background = RectF(
                        itemView.right.toFloat() - buttonWidth - margin,
                        itemView.top.toFloat() + margin,
                        itemView.right.toFloat() - margin,
                        itemView.bottom.toFloat() - margin
                    )
                    c.drawRoundRect(background, radius, radius, paint)

                    icon?.let {
                        val iconMargin = (buttonWidth - it.intrinsicWidth) / 2
                        val iconTop = itemView.top + (itemView.height - it.intrinsicHeight) / 2
                        val iconLeft = (itemView.right - margin - buttonWidth + iconMargin).toInt()

                        // Draw only if there's enough space
                        if (-clampedDx > buttonWidth / 3) {
                            it.setBounds(
                                iconLeft,
                                iconTop,
                                iconLeft + it.intrinsicWidth,
                                iconTop + it.intrinsicHeight
                            )
                            it.setTint(Color.WHITE)
                            it.draw(c)
                        }
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, clampedDx, dY, actionState, isCurrentlyActive)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.decksRecycler)
    }

    private fun setupClickListeners() {
        binding.btnAddDesk.setOnClickListener {
            val intent = Intent(requireContext(), AddDeckContainerActivity::class.java).apply {
                putExtra("IS_EDIT_MODE", false)
            }
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
