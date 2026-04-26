package com.example.flashcardapp.presentation.feature.learning.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.ItemLearningSessionPageBinding
import com.example.flashcardapp.domain.model.FlashCard

class LearningSessionPagerAdapter(
    private val onCardTapped: (position: Int) -> Unit
) : ListAdapter<FlashCard, LearningSessionPagerAdapter.CardViewHolder>(DiffCallback) {

    private val flippedPositions = mutableSetOf<Int>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemLearningSessionPageBinding.inflate(inflater, parent, false)
        return CardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        holder.bind(getItem(position), flippedPositions.contains(position))
    }

    override fun onBindViewHolder(
        holder: CardViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.contains(PAYLOAD_FLIP)) {
            holder.renderFlipState(flippedPositions.contains(position))
            return
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    override fun onViewRecycled(holder: CardViewHolder) {
        Glide.with(holder.itemView).clear(holder.binding.cardImage)
        super.onViewRecycled(holder)
    }

    fun isPositionFlipped(position: Int): Boolean {
        return flippedPositions.contains(position)
    }

    fun toggleFlipState(position: Int) {
        if (position !in 0 until itemCount) return
        if (flippedPositions.contains(position)) {
            flippedPositions.remove(position)
        } else {
            flippedPositions.add(position)
        }
        notifyItemChanged(position, PAYLOAD_FLIP)
    }

    fun resetFlipState(position: Int) {
        if (flippedPositions.remove(position)) {
            notifyItemChanged(position, PAYLOAD_FLIP)
        }
    }

    inner class CardViewHolder(
        val binding: ItemLearningSessionPageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.cardSurface.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCardTapped(position)
                }
            }
        }

        fun bind(card: FlashCard, isFlipped: Boolean) {
            binding.questionText.text = card.question
            binding.backQuestionText.text = card.question
            binding.answerText.text = card.answer
            if (card.imageUrl.isNullOrBlank()) {
                binding.cardImage.setImageResource(R.drawable.test)
            } else {
                Glide.with(binding.root)
                    .load(card.imageUrl)
                    .placeholder(R.drawable.test)
                    .error(R.drawable.test)
                    .centerCrop()
                    .into(binding.cardImage)
            }
            renderFlipState(isFlipped)
        }

        fun renderFlipState(isFlipped: Boolean) {
            binding.frontFace.isVisible = !isFlipped
            binding.backFace.isVisible = isFlipped
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<FlashCard>() {
        override fun areItemsTheSame(oldItem: FlashCard, newItem: FlashCard): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FlashCard, newItem: FlashCard): Boolean {
            return oldItem == newItem
        }
    }

    private companion object {
        const val PAYLOAD_FLIP = "payload_flip"
    }
}
