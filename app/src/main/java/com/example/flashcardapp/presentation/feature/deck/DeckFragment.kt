package com.example.flashcardapp.presentation.feature.deck

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.R
import com.example.flashcardapp.core.utils.textChangesFlow
import com.example.flashcardapp.databinding.FragmentDeckBinding
import com.example.flashcardapp.presentation.common.adapter.DeckAdapter
import com.example.flashcardapp.presentation.common.dialog.accountDialog.AppConfirmDialog
import com.example.flashcardapp.presentation.common.notification.showAppError
import com.example.flashcardapp.presentation.common.notification.showAppSuccess
import com.example.flashcardapp.presentation.common.notification.showAppWarning
import com.example.flashcardapp.presentation.feature.addDeck.AddDeckContainerActivity
import com.example.flashcardapp.presentation.feature.learning.LearningActivity
import androidx.core.view.isVisible
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class DeckFragment : Fragment() {

    private lateinit var binding: FragmentDeckBinding
    private val deckViewModel: DeckViewModel by viewModels {
        val container = (requireActivity().application as FlashcardApp).container
        DeckViewModelFactory(
            deckRepository = container.deckRepository
        )
    }
    private lateinit var deckAdapter: DeckAdapter
    private var shouldRefreshOnResume = false
    private val selectedDeckIds = mutableSetOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeckBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeSearchInput()
        lifecycleScope.launch {
            deckViewModel.deckUiState.collect { uiState ->
                handleUiState(uiState)
            }
        }
        setupClickListeners()
        binding.swipeRefresh.setOnRefreshListener {
            deckViewModel.syncDecksFromApi()
        }
    }

    override fun onResume() {
        super.onResume()
        if (shouldRefreshOnResume) {
            deckViewModel.syncDecksFromApi()
            shouldRefreshOnResume = false
        }
    }

    private fun setupRecyclerView() {
        deckAdapter = DeckAdapter(
            onItemClick = { deck ->
                if (selectedDeckIds.isNotEmpty()) {
                    toggleDeckSelection(deck.id)
                    return@DeckAdapter
                }
                deckViewModel.updateDeckLastStudied(deck)
                val intent = Intent(requireActivity(), LearningActivity::class.java).apply {
                    putExtra("DECK_ID", deck.id)
                }
                startActivity(intent)
            },
            onItemEdit = { deck ->
                if (selectedDeckIds.isNotEmpty()) {
                    toggleDeckSelection(deck.id)
                    return@DeckAdapter
                }
                val intent = Intent(requireContext(), AddDeckContainerActivity::class.java).apply {
                    putExtra("DECK_ID", deck.id)
                    putExtra("IS_EDIT_MODE", true)
                }
                startActivity(intent)
            },
            onItemLongClick = { deck ->
                toggleDeckSelection(deck.id)
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
                if (position == RecyclerView.NO_POSITION) return

                val deckId = deckAdapter.currentList[position].id
                val dialog = AppConfirmDialog.newInstance(
                    title = getString(R.string.delete_confirm_title),
                    message = getString(R.string.delete_confirm_message_deck),
                    confirmText = getString(R.string.delete_confirm_action),
                    cancelText = getString(R.string.delete_confirm_cancel),
                    iconRes = R.drawable.ic_delete,
                    destructive = true
                )
                dialog.listener = object : AppConfirmDialog.Listener {
                    override fun onConfirm() {
                        deckViewModel.deleteDeck(deckId)
                        showAppSuccess(getString(R.string.delete_success_deck))
                    }

                    override fun onCancel() {
                        deckAdapter.notifyItemChanged(position)
                    }
                }
                dialog.show(childFragmentManager, "delete_deck_confirm")
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
                val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)

                val density = requireContext().resources.displayMetrics.density
                val buttonWidth = 50 * density
                val margin = 8 * density
                val maxSwipe = -(buttonWidth + margin * 2)
                val clampedDx = if (dX < maxSwipe) maxSwipe else dX

                if (clampedDx < 0) {
                    val radius = 15 * density

                    paint.color = ContextCompat.getColor(requireContext(), R.color.deck_swipe_delete_bg)
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
            shouldRefreshOnResume = true
            startActivity(intent)
        }

        binding.filterButton.setOnClickListener {
            if (selectedDeckIds.isEmpty()) {
                showAppWarning(getString(R.string.deck_bulk_delete_hint))
            } else {
                showBulkDeleteConfirmDialog()
            }
        }
    }

    private fun toggleDeckSelection(deckId: String) {
        if (selectedDeckIds.contains(deckId)) selectedDeckIds.remove(deckId) else selectedDeckIds.add(deckId)
        deckAdapter.setSelectedDeckIds(selectedDeckIds)
        updateSelectionUi()
    }

    private fun clearSelection() {
        selectedDeckIds.clear()
        deckAdapter.setSelectedDeckIds(emptySet())
        updateSelectionUi()
    }

    private fun updateSelectionUi() {
        if (selectedDeckIds.isEmpty()) {
            binding.filterButton.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_filter)
            binding.filterButton.setBackgroundColor(Color.TRANSPARENT)
            binding.filterButton.iconTint = null
            return
        }
        binding.filterButton.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete)
        binding.filterButton.setBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.deck_bulk_delete_active_bg)
        )
        binding.filterButton.iconTint = ContextCompat.getColorStateList(requireContext(), android.R.color.white)
    }

    private fun showBulkDeleteConfirmDialog() {
        val count = selectedDeckIds.size
        val dialog = AppConfirmDialog.newInstance(
            title = getString(R.string.deck_bulk_delete_title, count),
            message = getString(R.string.deck_bulk_delete_message, count),
            confirmText = getString(R.string.delete_confirm_action),
            cancelText = getString(R.string.delete_confirm_cancel),
            iconRes = R.drawable.ic_delete,
            destructive = true
        )
        dialog.listener = object : AppConfirmDialog.Listener {
            override fun onConfirm() {
                val idsToDelete = selectedDeckIds.toList()
                idsToDelete.forEach { deckViewModel.deleteDeck(it) }
                clearSelection()
                showAppSuccess(getString(R.string.deck_bulk_delete_success, count))
            }
        }
        dialog.show(childFragmentManager, "delete_bulk_deck_confirm")
    }

    @OptIn(FlowPreview::class)
    private fun observeSearchInput() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                binding.searchInput.textChangesFlow()
                    .map { it.trim() }
                    .debounce(300)
                    .distinctUntilChanged()
                    .collect { query ->
                        deckViewModel.updateSearchQuery(query)
                    }
            }
        }
    }

    private fun handleUiState(uiState: DeckUiState) {
        when (uiState) {
            is DeckUiState.Loading -> showLoading()
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
        binding.searchContainer.alpha = 0.6f
        binding.decksRecycler.alpha = 0.6f
        binding.btnAddDesk.isEnabled = false
        binding.swipeRefresh.isRefreshing = true
    }

    private fun hideLoading() {
        binding.searchContainer.alpha = 1f
        binding.decksRecycler.alpha = 1f
        binding.btnAddDesk.isEnabled = true
        binding.swipeRefresh.isRefreshing = false
    }

    private fun showError(message: String) {
        showAppError(message)
    }

    private fun showEmpty() {
        deckAdapter.submitList(emptyList())
    }
}
