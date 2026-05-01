package com.example.flashcardapp.presentation.common.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.ItemDeckBinding
import com.example.flashcardapp.domain.model.Deck

class DeckAdapter(
    private val onItemClick: (Deck) -> Unit,
    private val onItemEdit: (Deck) -> Unit,
    private val onItemLongClick: (Deck) -> Unit
) : ListAdapter<Deck, DeckAdapter.DeckViewHolder>(DeckDiffCallback()) {

    private val selectedDeckIds = mutableSetOf<String>()

    fun setSelectedDeckIds(ids: Set<String>) {
        selectedDeckIds.clear()
        selectedDeckIds.addAll(ids)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckViewHolder {
        val binding = ItemDeckBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeckViewHolder(binding, onItemClick, onItemEdit, onItemLongClick)
    }

    override fun onBindViewHolder(holder: DeckViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, selectedDeckIds.contains(item.id))
    }

    class DeckViewHolder(
        private val binding: ItemDeckBinding,
        private val onItemClick: (Deck) -> Unit,
        private val onItemEdit: (Deck) -> Unit,
        private val onItemLongClick: (Deck) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Deck, isSelected: Boolean) {
            binding.apply {
                // Set deck title
                deckTitle.text = item.name

                // Set stats: cards count only
                stats.text = root.context.getString(R.string.deck_card_count, item.cardCount)

                // Set last studied text using real date
                val lastStudiedText = formatTimeAgo(root.context, item.updatedAt)
                lastStudied.text = root.context.getString(R.string.deck_last_studied, lastStudiedText)

                // Click listeners
                root.setOnClickListener {
                    onItemClick(item)
                }
                root.setOnLongClickListener {
                    onItemLongClick(item)
                    true
                }


                ctaLearn.setOnClickListener {
                    onItemClick(item)
                }

                editBtn.setOnClickListener {
                    onItemEdit(item)
                }

                if (isSelected) {
                    root.strokeColor = 0xFFE53935.toInt()
                    root.strokeWidth = 3
                    root.alpha = 0.92f
                } else {
                    root.strokeColor = 0x00000000
                    root.strokeWidth = 0
                    root.alpha = 1f
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

    companion object {
        private fun formatTimeAgo(context: Context, timestampString: String?): String {
            if (timestampString == null) return context.getString(R.string.deck_never_studied)
            
            var timeMillis: Long? = timestampString.toLongOrNull()

            if (timeMillis == null) {
                timeMillis = runCatching {
                    java.time.Instant.parse(timestampString).toEpochMilli()
                }.getOrNull()
            }

            if (timeMillis == null) {
                timeMillis = runCatching {
                    java.time.OffsetDateTime.parse(timestampString).toInstant().toEpochMilli()
                }.getOrNull()
            }
            
            if (timeMillis == null) {
                try {
                    val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
                    format.timeZone = java.util.TimeZone.getTimeZone("UTC")
                    timeMillis = format.parse(timestampString)?.time
                } catch (e: Exception) {
                    // Ignore
                }
            }
            
            if (timeMillis == null) return context.getString(R.string.deck_never_studied)
            
            val now = System.currentTimeMillis()
            val diff = now - timeMillis
            
            if (diff < 0) return context.getString(R.string.deck_just_now)
            
            val minutes = diff / (60 * 1000)
            if (minutes < 1) return context.getString(R.string.deck_just_now)
            
            val hours = minutes / 60
            if (hours < 1) return context.getString(R.string.deck_minutes_ago, minutes)
            
            val days = hours / 24
            if (days < 1) return context.getString(R.string.deck_hours_ago, hours)
            
            if (days < 30) return context.getString(R.string.deck_days_ago, days)
            
            return context.getString(R.string.deck_months_ago, days / 30)
        }
    }
}
