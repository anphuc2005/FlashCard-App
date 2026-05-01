package com.example.flashcardapp.presentation.common.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.databinding.ItemCourseBinding
import com.example.flashcardapp.domain.model.Deck
import com.example.flashcardapp.domain.model.Category
import kotlin.math.abs
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import com.example.flashcardapp.R

class CourseAdapter(
    private val onItemClick: (Deck) -> Unit,
    private val onSaveClick: (Deck) -> Unit
) : ListAdapter<Deck, CourseAdapter.CourseViewHolder>(CourseDiffCallback()) {

    private var categoryMap: Map<String, String> = emptyMap()

    @SuppressLint("NotifyDataSetChanged")
    fun setCategories(categories: List<Category>) {
        categoryMap = categories.associate { it.id to it.name }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CourseViewHolder(binding, onItemClick, onSaveClick)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(getItem(position), categoryMap)
    }

    class CourseViewHolder(
        private val binding: ItemCourseBinding,
        private val onItemClick: (Deck) -> Unit,
        private val onSaveClick: (Deck) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Deck, categoryMap: Map<String, String>) {
            binding.apply {
                courseTitle.text = item.name
                val totalCards = item.cardCount
                courseMeta.text = root.context.getString(R.string.course_card_count_en, totalCards)
                
                // Lấy tên Category thực tế, nếu không có giữ lại ID hoặc mặc định là DECK
                val categoryName = categoryMap[item.categoryId] ?: "DECK"
                courseTag.text = categoryName.uppercase()

                // Sử dụng categoryName để quyết định màu sắc thay vì item.name
                val (bgColorRes, tintColorRes) = getDeckVisuals(categoryName)
                val context = binding.root.context
                courseTag.setTextColor(ContextCompat.getColor(context, tintColorRes))
                courseTag.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(context, bgColorRes)
                )

                root.setOnClickListener {
                    onItemClick(item)
                }

                btnSaveCourse.setOnClickListener {
                    onSaveClick(item)
                }
            }
        }

        private fun getDeckVisuals(name: String): Pair<Int, Int> {
            val nameLower = name.lowercase().trim()
            if (nameLower.contains("ngôn ngữ") || nameLower.contains("language") || nameLower.contains("tiếng")) {
                return Pair(R.color.md_icon_blue_background, R.color.md_icon_blue)
            }
            if (nameLower.contains("khoa học") || nameLower.contains("science") || nameLower.contains("toán") || nameLower.contains("math")) {
                return Pair(R.color.md_icon_green_background, R.color.md_icon_green)
            }
            if (nameLower.contains("công nghệ") || nameLower.contains("it") || nameLower.contains("lập trình") || nameLower.contains("tech")) {
                return Pair(R.color.md_icon_purple_background, R.color.md_icon_purple)
            }
            if (nameLower.contains("lịch sử") || nameLower.contains("history")) {
                return Pair(R.color.md_icon_yellow_background, R.color.md_icon_yellow)
            }
            if (nameLower.contains("kinh tế") || nameLower.contains("art") || nameLower.contains("âm nhạc") || nameLower.contains("music")) {
                return Pair(R.color.md_icon_orange_background, R.color.md_icon_orange)
            }
            if (nameLower.contains("từ vựng") || nameLower.contains("vocabulary")) {
                return Pair(R.color.md_icon_red_background, R.color.md_icon_red)
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
            return colors[hash % colors.size]
        }
    }

    private class CourseDiffCallback : DiffUtil.ItemCallback<Deck>() {
        override fun areItemsTheSame(oldItem: Deck, newItem: Deck): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Deck, newItem: Deck): Boolean {
            return oldItem == newItem
        }
    }
}
