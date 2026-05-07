package com.example.flashcardapp.presentation.feature.aiChat.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.ItemChatSessionBinding
import com.example.flashcardapp.presentation.feature.aiChat.model.ChatSessionUiModel
import com.google.android.material.color.MaterialColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatSessionAdapter(
    private val onSessionClick: (ChatSessionUiModel) -> Unit,
    private val onDeleteClick: (ChatSessionUiModel) -> Unit
) : ListAdapter<ChatSessionUiModel, ChatSessionAdapter.ChatSessionViewHolder>(DiffCallback()) {

    private var selectedSessionId: String? = null

    fun setSelectedSessionId(sessionId: String?) {
        val previous = selectedSessionId
        if (previous == sessionId) return
        selectedSessionId = sessionId

        val previousIndex = currentList.indexOfFirst { it.id == previous }
        if (previousIndex != -1) notifyItemChanged(previousIndex)

        val currentIndex = currentList.indexOfFirst { it.id == sessionId }
        if (currentIndex != -1) notifyItemChanged(currentIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatSessionViewHolder {
        val binding = ItemChatSessionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatSessionViewHolder(binding, onSessionClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: ChatSessionViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, item.id == selectedSessionId)
    }

    class ChatSessionViewHolder(
        private val binding: ItemChatSessionBinding,
        private val onSessionClick: (ChatSessionUiModel) -> Unit,
        private val onDeleteClick: (ChatSessionUiModel) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault())
        private val selectedStrokeWidthPx = (2 * binding.root.resources.displayMetrics.density).toInt()
        private val normalStrokeWidthPx = (1 * binding.root.resources.displayMetrics.density).toInt()

        fun bind(item: ChatSessionUiModel, isSelected: Boolean) {
            binding.tvSessionTitle.text = item.title
            binding.tvSessionPreview.text = item.preview
            binding.tvSessionUpdatedAt.text = dateFormat.format(Date(item.updatedAt))
            binding.tvActiveBadge.visibility = if (isSelected) View.VISIBLE else View.GONE

            val surfaceColor = MaterialColors.getColor(binding.root, R.attr.componentBackground)
            val highlightColor = MaterialColors.getColor(binding.root, R.attr.buttonColor)
            val defaultStrokeColor = MaterialColors.getColor(binding.root, R.attr.buttonBottomNavInactive)

            binding.cardSession.setCardBackgroundColor(
                if (isSelected) MaterialColors.layer(surfaceColor, highlightColor, 0.10f) else surfaceColor
            )
            binding.cardSession.strokeColor = if (isSelected) highlightColor else defaultStrokeColor
            binding.cardSession.strokeWidth = if (isSelected) selectedStrokeWidthPx else normalStrokeWidthPx

            binding.root.setOnClickListener { onSessionClick(item) }
            binding.btnDeleteSession.setOnClickListener { onDeleteClick(item) }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ChatSessionUiModel>() {
        override fun areItemsTheSame(oldItem: ChatSessionUiModel, newItem: ChatSessionUiModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ChatSessionUiModel, newItem: ChatSessionUiModel): Boolean {
            return oldItem == newItem
        }
    }
}
