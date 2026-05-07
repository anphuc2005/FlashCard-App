package com.example.flashcardapp.presentation.common.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.databinding.ItemRecentDeckBinding
import com.example.flashcardapp.domain.model.Deck

class RecentDeckAdapter(
    private val onItemClick: (Deck) -> Unit
) : ListAdapter<Deck, RecentDeckAdapter.DeckViewHolder>(DeckDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckViewHolder {
        val binding = ItemRecentDeckBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeckViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: DeckViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DeckViewHolder(
        private val binding: ItemRecentDeckBinding,
        private val onItemClick: (Deck) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Deck) {
            binding.apply {
                tvTitle.text = item.name
                tvSubtitle.text = tvSubtitle.context.getString(
                    com.example.flashcardapp.R.string.item_deck_card_count,
                    item.cardCount
                )

                if(item.iconResId != null) {
                    imgIcon.setImageResource(item.iconResId)
                }

                if (item.backgroundResId != null) {
                    val colorValue = ContextCompat.getColor(
                        binding.root.context,
                        item.backgroundResId
                    )
                    cardIcon.setCardBackgroundColor(colorValue)
                }

                cardRoot.setOnClickListener {
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

