package com.example.flashcardapp.presentation.common.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.ItemDeckBinding
import com.example.flashcardapp.domain.model.Deck

class DeckAdapter(
    private val onItemClick: (Deck) -> Unit,
    private val onMenuClick: (Deck) -> Unit
) : ListAdapter<Deck, DeckAdapter.DeckViewHolder>(DeckDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckViewHolder {
        val binding = ItemDeckBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeckViewHolder(binding, onItemClick, onMenuClick)
    }

    override fun onBindViewHolder(holder: DeckViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DeckViewHolder(
        private val binding: ItemDeckBinding,
        private val onItemClick: (Deck) -> Unit,
        private val onMenuClick: (Deck) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Deck) {
            binding.apply {
                // Set deck title
                deckTitle.text = item.name

                // Set stats: cards count and studied cards (mock: 50% studied)
                val studiedCards = item.cardCount / 2
                @Suppress("SetTextI18n")
                stats.text = "${item.cardCount} thẻ · Đã học $studiedCards"

                // Set progress percentage (mock data)
                val progressPercent = (studiedCards * 100) / item.cardCount
                @Suppress("SetTextI18n")
                root.findViewById<android.widget.TextView>(R.id.progressPercent)?.text = "$progressPercent%"

                // Set progress bar progress (mock data) - convert to Float
                progressBar.setProgress(progressPercent.toFloat())

                // Set last studied text (mock data)
                @Suppress("SetTextI18n")
                lastStudied.text = "Học lần cuối: 2 giờ trước"

                // Click listeners
                root.setOnClickListener {
                    onItemClick(item)
                }

                menuBtn.setOnClickListener {
                    onMenuClick(item)
                }

                ctaLearn.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }

    private class DeckDiffCallback : DiffUtil.ItemCallback<Deck>() {
        override fun areItemsTheSame(oldItem: Deck, newItem: Deck): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Deck, newItem: Deck): Boolean {
            return oldItem == newItem
        }
    }
}

