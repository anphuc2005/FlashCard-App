package com.example.flashcardapp.presentation.common.adapter

import android.text.method.LinkMovementMethod
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.core.text.HtmlCompat
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.ItemChatMessageBinding
import com.example.flashcardapp.domain.model.ChatMessage
import com.example.flashcardapp.utils.MarkdownConverter

class ChatMessageAdapter(
    private val onOpenDeck: (String) -> Unit = {}
) : ListAdapter<ChatMessage, ChatMessageAdapter.ChatMessageViewHolder>(DiffCallback()) {

    private val deckIdRegex = Regex("`([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})`")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatMessageViewHolder {
        val binding = ItemChatMessageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatMessageViewHolder(
            binding = binding,
            onOpenDeck = onOpenDeck
        )
    }

    override fun onBindViewHolder(holder: ChatMessageViewHolder, position: Int) {
        val message = getItem(position)
        val matchedDeckId = deckIdRegex.find(message.message)
            ?.groupValues
            ?.getOrNull(1)
        holder.bind(message, matchedDeckId)
    }

    class ChatMessageViewHolder(
        private val binding: ItemChatMessageBinding,
        private val onOpenDeck: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage, deckId: String?) {
            with(binding) {
                val layoutParams = messageContainer.layoutParams as FrameLayout.LayoutParams
                if (message.sender == "user") {
                    tvMessage.text = message.message
                    tvMessage.movementMethod = null
                    layoutParams.gravity = android.view.Gravity.END
                    tvMessage.setBackgroundResource(R.drawable.bg_message_user)
                    tvMessage.setTextColor(root.context.getColor(android.R.color.white))
                    progressBar.visibility = View.GONE
                    tvError.visibility = View.GONE
                } else {
                    val markdownHtml = MarkdownConverter.markdownToHtml(message.message)
                    tvMessage.text = HtmlCompat.fromHtml(markdownHtml, HtmlCompat.FROM_HTML_MODE_LEGACY)
                    tvMessage.movementMethod = LinkMovementMethod.getInstance()
                    layoutParams.gravity = android.view.Gravity.START
                    tvMessage.setBackgroundResource(R.drawable.bg_message_ai)
                    val typedValue = TypedValue()
                    root.context.theme.resolveAttribute(android.R.attr.textColor, typedValue, true)
                    tvMessage.setTextColor(typedValue.data)
                }

                messageContainer.layoutParams = layoutParams
                progressBar.visibility = View.GONE
                tvError.visibility = View.GONE

                if (message.sender == "bot" && !deckId.isNullOrBlank()) {
                    actionContainer.visibility = View.VISIBLE
                    btnConfirmDeck.visibility = View.VISIBLE
                    btnConfirmDeck.text = root.context.getString(R.string.ai_open_deck)
                    btnCancelDeck.visibility = View.GONE
                    btnConfirmDeck.setOnClickListener { onOpenDeck(deckId) }
                    btnCancelDeck.setOnClickListener(null)
                } else {
                    actionContainer.visibility = View.GONE
                    btnConfirmDeck.visibility = View.GONE
                    btnCancelDeck.visibility = View.GONE
                    btnConfirmDeck.setOnClickListener(null)
                    btnCancelDeck.setOnClickListener(null)
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
