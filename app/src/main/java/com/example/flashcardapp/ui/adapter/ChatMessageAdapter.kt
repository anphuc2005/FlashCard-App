package com.example.flashcardapp.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.R
import com.example.flashcardapp.data.model.ChatMessage
import com.example.flashcardapp.data.model.MessageStatus
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
                tvMessage.text = message.text

                val layoutParams = messageContainer.layoutParams as FrameLayout.LayoutParams
                if (message.isUser) {
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

                when (message.status) {
                    MessageStatus.SENDING -> {
                        progressBar.visibility = View.VISIBLE
                        tvError.visibility = View.GONE
                    }
                    MessageStatus.SUCCESS -> {
                        progressBar.visibility = View.GONE
                        tvError.visibility = View.GONE
                    }
                    MessageStatus.ERROR -> {
                        progressBar.visibility = View.GONE
                        tvError.visibility = View.VISIBLE
                        tvError.text = root.context.getString(R.string.error_message, message.text)
                    }
                }
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
