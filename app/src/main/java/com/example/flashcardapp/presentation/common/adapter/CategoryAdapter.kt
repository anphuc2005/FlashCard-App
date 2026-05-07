package com.example.flashcardapp.presentation.common.adapter

import android.graphics.PorterDuff
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.ItemShortcutBinding
import com.example.flashcardapp.domain.model.Category
import kotlin.math.abs

class CategoryAdapter(
    private val onItemClick: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    private var selectedCategoryId: String? = null

    fun setSelectedCategoryId(categoryId: String?) {
        val previous = selectedCategoryId
        if (previous == categoryId) return
        selectedCategoryId = categoryId

        val previousIndex = currentList.indexOfFirst { it.id == previous }
        if (previousIndex != -1) notifyItemChanged(previousIndex)

        val currentIndex = currentList.indexOfFirst { it.id == categoryId }
        if (currentIndex != -1) notifyItemChanged(currentIndex)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemShortcutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        val density = parent.context.resources.displayMetrics.density
        val marginHorizontalPx = (5 * density).toInt()
        val params = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            marginStart = marginHorizontalPx
            marginEnd = marginHorizontalPx
        }
        binding.root.layoutParams = params
        return CategoryViewHolder(binding, onItemClick)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, item.id == selectedCategoryId)
    }

    class CategoryViewHolder(
        private val binding: ItemShortcutBinding,
        private val onItemClick: (Category) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Category, isSelected: Boolean) {
            binding.tvTitle.text = item.name
            val (iconRes, bgColorRes, tintColorRes) = getCategoryVisuals(item.name)
            val context = binding.root.context
            binding.imgIcon.setImageResource(iconRes)
            binding.imgIcon.setColorFilter(
                ContextCompat.getColor(context, tintColorRes),
                PorterDuff.Mode.SRC_IN
            )
            binding.cardIcon.setCardBackgroundColor(
                ContextCompat.getColor(context, bgColorRes)
            )

            val selectedStrokeColor = ContextCompat.getColor(context, tintColorRes)
            val selectedStrokeWidthPx = (2 * context.resources.displayMetrics.density).toInt()
            binding.cardIcon.strokeColor = selectedStrokeColor
            binding.cardIcon.strokeWidth = if (isSelected) selectedStrokeWidthPx else 0

            binding.root.alpha = if (isSelected) 1f else 0.76f
            binding.tvTitle.alpha = if (isSelected) 1f else 0.85f
            binding.tvTitle.setTypeface(
                binding.tvTitle.typeface,
                if (isSelected) Typeface.BOLD else Typeface.NORMAL
            )
            binding.root.scaleX = if (isSelected) 1.03f else 1f
            binding.root.scaleY = if (isSelected) 1.03f else 1f
            binding.root.contentDescription = if (isSelected) {
                "${item.name}, đã chọn"
            } else {
                item.name
            }
            binding.root.setOnClickListener { onItemClick(item) }
        }

        private fun getCategoryVisuals(name: String): Triple<Int, Int, Int> {
            val nameLower = name.lowercase().trim()
            if (nameLower.contains("ngôn ngữ") || nameLower.contains("language") || nameLower.contains("tiếng")) {
                return Triple(R.drawable.ic_language, R.color.md_icon_blue_background, R.color.md_icon_blue)
            }
            if (nameLower.contains("khoa học") || nameLower.contains("science") || nameLower.contains("toán") || nameLower.contains("math")) {
                return Triple(R.drawable.ic_science, R.color.md_icon_green_background, R.color.md_icon_green)
            }
            if (nameLower.contains("công nghệ") || nameLower.contains("it") || nameLower.contains("lập trình") || nameLower.contains("tech")) {
                return Triple(R.drawable.ic_json, R.color.md_icon_purple_background, R.color.md_icon_purple)
            }
            if (nameLower.contains("lịch sử") || nameLower.contains("history")) {
                return Triple(R.drawable.ic_history, R.color.md_icon_yellow_background, R.color.md_icon_yellow)
            }
            if (nameLower.contains("kinh tế") || nameLower.contains("art") || nameLower.contains("âm nhạc") || nameLower.contains("music")) {
                return Triple(R.drawable.ic_economic, R.color.md_icon_orange_background, R.color.md_icon_orange)
            }
            if (nameLower.contains("từ vựng") || nameLower.contains("vocabulary")) {
                return Triple(R.drawable.ic_cards, R.color.md_icon_red_background, R.color.md_icon_red)
            }

            val colors = listOf(
                Pair(R.color.md_icon_blue_background, R.color.md_icon_blue),
                Pair(R.color.md_icon_green_background, R.color.md_icon_green),
                Pair(R.color.md_icon_purple_background, R.color.md_icon_purple),
                Pair(R.color.md_icon_yellow_background, R.color.md_icon_yellow),
                Pair(R.color.md_icon_orange_background, R.color.md_icon_orange),
                Pair(R.color.md_icon_red_background, R.color.md_icon_red),
                Pair(R.color.md_icon_gray_background, R.color.md_icon_gray)
            )
            val hash = abs(name.hashCode())
            val colorPair = colors[hash % colors.size]
            return Triple(R.drawable.ic_discover, colorPair.first, colorPair.second)
        }
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Category, newItem: Category): Boolean {
            return oldItem == newItem
        }
    }
}
