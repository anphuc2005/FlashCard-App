package com.example.flashcardapp.presentation.feature.statistics.adapter

// Adapter hiển thị danh sách thành tích trong màn hình Thống kê.

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.ItemAchievementBinding
import com.example.flashcardapp.presentation.feature.statistics.model.StatisticAchievementItem
import com.google.android.material.color.MaterialColors

class StatisticAchievementAdapter :
    ListAdapter<StatisticAchievementItem, StatisticAchievementAdapter.AchievementViewHolder>(StatisticAchievementDiffCallback()) {

    // Khởi tạo view từ layout XML của màn hình. Tạo ViewHolder mới cho từng item trong danh sách.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val binding = ItemAchievementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AchievementViewHolder(binding)
    }

    // Gán dữ liệu item vào ViewHolder tương ứng.
    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class AchievementViewHolder(
        private val binding: ItemAchievementBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        // Gán dữ liệu từ model vào các view hiển thị.
        fun bind(item: StatisticAchievementItem) {
            binding.achievementTitle.text = item.title
            binding.achievementDesc.text = item.description
            binding.achievementIcon.setImageResource(item.iconResId)
            val isUnlocked = item.isUnlocked
            val strokeColor = if (isUnlocked) {
                MaterialColors.getColor(binding.root, R.attr.iconGreenBackground)
            } else {
                MaterialColors.getColor(binding.root, R.attr.iconBlueBackground)
            }
            val iconBgColor = if (isUnlocked) {
                MaterialColors.getColor(binding.root, R.attr.iconGreenBackground)
            } else {
                MaterialColors.getColor(binding.root, R.attr.iconBlueBackground)
            }
            binding.root.strokeColor = strokeColor
            binding.achievementIconContainer.setCardBackgroundColor(iconBgColor)
            binding.achievementTitle.setTextColor(
                if (isUnlocked) {
                    MaterialColors.getColor(binding.root, R.attr.iconGreen)
                } else {
                    MaterialColors.getColor(binding.root, R.attr.textColor)
                }
            )
            binding.root.alpha = if (isUnlocked) 1f else 0.78f
            if (isUnlocked) {
                binding.achievementIcon.animate().cancel()
                binding.achievementIcon.scaleX = 0.92f
                binding.achievementIcon.scaleY = 0.92f
                binding.achievementIcon.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(180L)
                    .start()
            } else {
                binding.achievementIcon.animate().cancel()
                binding.achievementIcon.scaleX = 1f
                binding.achievementIcon.scaleY = 1f
            }
            binding.root.setOnClickListener(null)
        }
    }

    private class StatisticAchievementDiffCallback : DiffUtil.ItemCallback<StatisticAchievementItem>() {
        override fun areItemsTheSame(
            oldItem: StatisticAchievementItem,
            newItem: StatisticAchievementItem
        ): Boolean = oldItem.title == newItem.title

        override fun areContentsTheSame(
            oldItem: StatisticAchievementItem,
            newItem: StatisticAchievementItem
        ): Boolean = oldItem == newItem
    }
}
