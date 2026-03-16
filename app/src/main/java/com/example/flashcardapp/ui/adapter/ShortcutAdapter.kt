package com.example.flashcardapp.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.databinding.ItemShortcutBinding
import com.example.flashcardapp.model.Shortcut

class ShortcutAdapter(
    private val onItemClick: (Shortcut) -> Unit
) : ListAdapter<Shortcut, ShortcutAdapter.ShortcutViewHolder>(ShortcutDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShortcutViewHolder {
        val binding = ItemShortcutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShortcutViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: ShortcutViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ShortcutViewHolder(
        private val binding: ItemShortcutBinding,
        private val onItemClick: (Shortcut) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Shortcut) {
            binding.apply {
                tvTitle.text = item.title
                imgIcon.setImageResource(item.iconResId)

                if (item.backgroundResId != null) {
                    val colorValue = ContextCompat.getColor(
                        binding.root.context,
                        item.backgroundResId
                    )
                    cardIcon.setCardBackgroundColor(colorValue)
                }

                root.setOnClickListener {
                    onItemClick(item)
                }
            }
        }
    }

    private class ShortcutDiffCallback : DiffUtil.ItemCallback<Shortcut>() {
        override fun areItemsTheSame(oldItem: Shortcut, newItem: Shortcut): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Shortcut, newItem: Shortcut): Boolean {
            return oldItem == newItem
        }
    }
}

