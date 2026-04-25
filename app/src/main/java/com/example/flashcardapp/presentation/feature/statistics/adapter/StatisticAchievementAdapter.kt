package com.example.flashcardapp.presentation.feature.statistics.adapter

// Adapter hiển thị danh sách thành tích trong màn hình Thống kê.

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.databinding.ItemAchievementBinding
import com.example.flashcardapp.presentation.feature.statistics.model.StatisticAchievementItem

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
            binding.root.alpha = 1f
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

