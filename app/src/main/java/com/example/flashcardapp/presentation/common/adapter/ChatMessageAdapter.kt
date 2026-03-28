package com.example.flashcardapp.presentation.common.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.R
import com.example.flashcardapp.domain.model.ChatMessage
import com.example.flashcardapp.data.datasource.local.entity.ChatMessageEntity
import com.example.flashcardapp.databinding.ItemChatMessageBinding

class ChatMessageAdapter : ListAdapter<ChatMessage, ChatMessageAdapter.ChatMessageViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatMessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatMessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ChatMessageViewHolder(
        private val binding: ItemChatMessageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage) {
            with(binding) {
                tvMessage.text = message.message

                val layoutParams = messageContainer.layoutParams as FrameLayout.LayoutParams
                if (message.sender == "user") {
                    layoutParams.gravity = android.view.Gravity.END
                    tvMessage.setBackgroundResource(R.drawable.bg_message_user)
                    tvMessage.setTextColor(root.context.getColor(android.R.color.white))
                    progressBar.visibility = View.GONE
                    tvError.visibility = View.GONE
                } else {
                    layoutParams.gravity = android.view.Gravity.START
                    tvMessage.setBackgroundResource(R.drawable.bg_message_ai)
                    tvMessage.setTextColor(root.context.getColor(android.R.color.black))
                }
                messageContainer.layoutParams = layoutParams
                progressBar.visibility = View.GONE
                tvError.visibility = View.GONE
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage) =
            oldItem == newItem
    }
}

