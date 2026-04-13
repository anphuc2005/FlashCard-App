package com.example.flashcardapp.presentation.feature.statistics.adapter

// Adapter hiển thị tiến độ học theo từng bộ thẻ.

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.R
import com.example.flashcardapp.core.utils.chart.ProgressBar
import com.example.flashcardapp.domain.model.statistics.DeckStatistics
import com.example.flashcardapp.presentation.feature.statistics.StatisticFormatter

class DeckStatisticsAdapter(
    private val formatter: StatisticFormatter
) :
    ListAdapter<DeckStatistics, DeckStatisticsAdapter.DeckStatisticsViewHolder>(DeckStatisticsDiffCallback()) {

    // Khởi tạo view từ layout XML của màn hình. Tạo ViewHolder mới cho từng item trong danh sách.
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeckStatisticsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_progress, parent, false)
        return DeckStatisticsViewHolder(view, formatter)
    }

    // Gán dữ liệu item vào ViewHolder tương ứng.
    override fun onBindViewHolder(holder: DeckStatisticsViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DeckStatisticsViewHolder(
        itemView: View,
        private val formatter: StatisticFormatter
    ) : RecyclerView.ViewHolder(itemView) {
        private val deckNameTextView: TextView = itemView.findViewById(R.id.tvDeckName)
        private val progressPercentTextView: TextView = itemView.findViewById(R.id.prog2Percent)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.prog2)

        // Gán dữ liệu từ model vào các view hiển thị.
        fun bind(item: DeckStatistics) {
            deckNameTextView.text = item.deckName
            progressPercentTextView.text = formatter.formatPercent(item.progressPercent)
            progressBar.setProgress(item.progressPercent.toFloat())
        }
    }

    private class DeckStatisticsDiffCallback : DiffUtil.ItemCallback<DeckStatistics>() {
        override fun areItemsTheSame(oldItem: DeckStatistics, newItem: DeckStatistics): Boolean {
            return oldItem.deckId == newItem.deckId
        }

        override fun areContentsTheSame(oldItem: DeckStatistics, newItem: DeckStatistics): Boolean {
            return oldItem == newItem
        }
    }
}

