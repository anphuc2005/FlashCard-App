package com.example.flashcardapp.presentation.common.adapter

import android.content.res.ColorStateList
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.ItemAdminNotificationBinding
import com.example.flashcardapp.domain.model.UserNotification
import com.google.android.material.color.MaterialColors
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NotificationInboxAdapter(
    private val onItemClick: (UserNotification) -> Unit
) : ListAdapter<UserNotification, NotificationInboxAdapter.NotificationViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemAdminNotificationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NotificationViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NotificationViewHolder(
        private val binding: ItemAdminNotificationBinding,
        private val onItemClick: (UserNotification) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserNotification) {
            val context = binding.root.context
            binding.tvTitle.text = item.title.ifBlank {
                when (item.type.uppercase()) {
                    "DECK_WARNING" -> context.getString(R.string.notifications_inbox_type_deck_warning)
                    "REPORT_RESULT" -> context.getString(R.string.notifications_inbox_type_report_result)
                    else -> context.getString(R.string.notifications_inbox_type_system)
                }
            }
            binding.tvMessage.text = item.message
            binding.tvTime.text = formatRelativeTime(item.createdAt, context)

            val (iconRes, tintRes) = when (item.type.uppercase()) {
                "DECK_WARNING" -> R.drawable.ic_app_notification_warning to R.color.md_icon_red
                "REPORT_RESULT" -> R.drawable.ic_info to R.color.md_icon_blue
                else -> R.drawable.ic_notif_shortcut to R.color.md_icon_yellow
            }

            binding.imgType.setImageResource(iconRes)
            binding.imgType.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(context, tintRes)
            )

            val backgroundColor = if (item.isRead) {
                MaterialColors.getColor(binding.root, R.attr.componentBackground)
            } else {
                ContextCompat.getColor(context, R.color.md_icon_blue_background)
            }
            binding.cardRoot.setCardBackgroundColor(backgroundColor)
            binding.unreadDot.isVisible = !item.isRead

            binding.root.setOnClickListener { onItemClick(item) }
        }

        private fun formatRelativeTime(rawTimestamp: String?, context: android.content.Context): String {
            val timestamp = parseTimestampMillis(rawTimestamp) ?: return context.getString(R.string.deck_just_now)
            val now = System.currentTimeMillis()
            if (timestamp >= now) {
                return DateUtils.getRelativeTimeSpanString(
                    timestamp,
                    now,
                    DateUtils.MINUTE_IN_MILLIS,
                    DateUtils.FORMAT_ABBREV_RELATIVE
                ).toString()
            }
            val diff = now - timestamp
            if (diff < DateUtils.MINUTE_IN_MILLIS) {
                return context.getString(R.string.deck_just_now)
            }
            return DateUtils.getRelativeTimeSpanString(
                timestamp,
                now,
                DateUtils.MINUTE_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_RELATIVE
            ).toString()
        }

        private fun parseTimestampMillis(raw: String?): Long? {
            if (raw.isNullOrBlank()) return null
            raw.toLongOrNull()?.let { return it }
            runCatching { Instant.parse(raw).toEpochMilli() }.getOrNull()?.let { return it }
            runCatching { OffsetDateTime.parse(raw).toInstant().toEpochMilli() }.getOrNull()?.let { return it }
            runCatching {
                LocalDateTime.parse(raw, DateTimeFormatter.ISO_DATE_TIME)
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }.getOrNull()?.let { return it }
            runCatching {
                LocalDate.parse(raw, DateTimeFormatter.ISO_DATE)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            }.getOrNull()?.let { return it }
            return null
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<UserNotification>() {
        override fun areItemsTheSame(oldItem: UserNotification, newItem: UserNotification): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserNotification, newItem: UserNotification): Boolean {
            return oldItem == newItem
        }
    }
}
