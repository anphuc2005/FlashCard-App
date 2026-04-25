package com.example.flashcardapp.presentation.common.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.databinding.ItemEditDeckCardBinding
import com.example.flashcardapp.domain.model.FlashCard

class EditDeckCardAdapter(
    private val onEditClick: (FlashCard) -> Unit,
    private val onDeleteClick: (FlashCard) -> Unit
) : ListAdapter<FlashCard, EditDeckCardAdapter.CardViewHolder>(CardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemEditDeckCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CardViewHolder(private val binding: ItemEditDeckCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(card: FlashCard) {
            binding.tvFront.text = card.question
            binding.tvBack.text = card.answer

            binding.btnEdit.setOnClickListener {
                onEditClick(card)
            }
            binding.btnDelete.setOnClickListener {
                onDeleteClick(card)
            }
        }
    }

    class CardDiffCallback : DiffUtil.ItemCallback<FlashCard>() {
        override fun areItemsTheSame(oldItem: FlashCard, newItem: FlashCard): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FlashCard, newItem: FlashCard): Boolean {
            return oldItem == newItem
        }
    }
}
