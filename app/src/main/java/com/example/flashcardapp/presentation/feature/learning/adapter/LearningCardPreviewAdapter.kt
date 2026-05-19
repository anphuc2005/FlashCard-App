package com.example.flashcardapp.presentation.feature.learning.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.ItemLearningCardPreviewBinding
import com.example.flashcardapp.domain.model.FlashCard

class LearningCardPreviewAdapter :
    ListAdapter<FlashCard, LearningCardPreviewAdapter.CardViewHolder>(FlashCardDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val binding = ItemLearningCardPreviewBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position), position + 1)
    }

    class CardViewHolder(
        private val binding: ItemLearningCardPreviewBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(card: FlashCard, position: Int) {
            binding.textCardTag.text = binding.root.context.getString(
                R.string.learning_card_preview_index,
                position
            )
            binding.textQuestion.text = card.question
            binding.textAnswer.text = card.answer
        }
    }

    private class FlashCardDiffCallback : DiffUtil.ItemCallback<FlashCard>() {
        override fun areItemsTheSame(oldItem: FlashCard, newItem: FlashCard): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FlashCard, newItem: FlashCard): Boolean {
            return oldItem == newItem
        }
    }
}
