package com.example.flashcardapp.presentation.feature.statistics.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.ItemAchievementDetailBinding
import com.example.flashcardapp.presentation.feature.statistics.model.StatisticAchievementItem
import com.google.android.material.color.MaterialColors

class SystemAchievementAdapter :
    ListAdapter<StatisticAchievementItem, SystemAchievementAdapter.SystemAchievementViewHolder>(SystemAchievementDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SystemAchievementViewHolder {
        val binding = ItemAchievementDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SystemAchievementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SystemAchievementViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SystemAchievementViewHolder(
        private val binding: ItemAchievementDetailBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: StatisticAchievementItem) {
            binding.achievementIcon.setImageResource(item.iconResId)
            binding.achievementTitle.text = item.title
            binding.achievementDesc.text = item.description
            binding.achievementStatus.text = binding.root.context.getString(
                if (item.isUnlocked) R.string.stat_achievement_unlocked else R.string.stat_achievement_locked
            )

            val statusColor = if (item.isUnlocked) {
                MaterialColors.getColor(binding.root, R.attr.iconGreen)
            } else {
                MaterialColors.getColor(binding.root, R.attr.subTitleColor)
            }
            binding.achievementStatus.setTextColor(statusColor)
            binding.root.alpha = if (item.isUnlocked) 1f else 0.75f
        }
    }
}

private class SystemAchievementDiffCallback : DiffUtil.ItemCallback<StatisticAchievementItem>() {
    override fun areItemsTheSame(
        oldItem: StatisticAchievementItem,
        newItem: StatisticAchievementItem
    ): Boolean = oldItem.title == newItem.title

    override fun areContentsTheSame(
        oldItem: StatisticAchievementItem,
        newItem: StatisticAchievementItem
    ): Boolean = oldItem == newItem
}
