package com.example.flashcardapp.presentation.common.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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
                @Suppress("SetTextI18n")
                stats.text = "${item.cardCount} thẻ"

                // Set last studied text using real date
                val lastStudiedText = formatTimeAgo(item.updatedAt)
                @Suppress("SetTextI18n")
                lastStudied.text = "Học lần cuối: $lastStudiedText"

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
        private fun formatTimeAgo(timestampString: String?): String {
            if (timestampString == null) return "Chưa từng học"
            
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
            
            if (timeMillis == null) return "Chưa từng học"
            
            val now = System.currentTimeMillis()
            val diff = now - timeMillis
            
            if (diff < 0) return "Vừa xong"
            
            val minutes = diff / (60 * 1000)
            if (minutes < 1) return "Vừa xong"
            
            val hours = minutes / 60
            if (hours < 1) return "$minutes phút trước"
            
            val days = hours / 24
            if (days < 1) return "$hours giờ trước"
            
            if (days < 30) return "$days ngày trước"
            
            return "${days / 30} tháng trước"
        }
    }
}
