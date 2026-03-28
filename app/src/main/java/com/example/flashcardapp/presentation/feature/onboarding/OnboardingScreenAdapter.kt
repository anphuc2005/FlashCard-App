package com.example.flashcardapp.presentation.feature.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.ItemOnboardingScreenBinding

class OnboardingScreenAdapter(
    private val pages: List<OnboardingPageUiModel>,
    private val onPrimaryClick: (Int) -> Unit
) : RecyclerView.Adapter<OnboardingScreenAdapter.OnboardingScreenViewHolder>() {

    private var currentPage: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingScreenViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemOnboardingScreenBinding.inflate(inflater, parent, false)
        return OnboardingScreenViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingScreenViewHolder, position: Int) {
        holder.bind(
            page = pages[position],
            position = position,
            pageCount = pages.size,
            isActivePage = position == currentPage,
            onPrimaryClick = onPrimaryClick
        )
    }

    override fun getItemCount(): Int = pages.size

    fun updateCurrentPage(position: Int) {
        if (currentPage == position) return
        currentPage = position
        notifyDataSetChanged()
    }

    class OnboardingScreenViewHolder(
        private val binding: ItemOnboardingScreenBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            page: OnboardingPageUiModel,
            position: Int,
            pageCount: Int,
            isActivePage: Boolean,
            onPrimaryClick: (Int) -> Unit
        ) {
            binding.imageOnboarding.setImageResource(page.iconResId)
            binding.textTitle.text = page.title
            binding.textDescription.text = page.description
            binding.buttonPrimary.text = page.buttonText
            binding.buttonPrimary.setOnClickListener { onPrimaryClick(position) }

            binding.indicatorContainer.removeAllViews()
            repeat(pageCount) { index ->
                binding.indicatorContainer.addView(createIndicatorDot(index == position && isActivePage))
            }
        }

        private fun createIndicatorDot(isSelected: Boolean): View {
            return View(binding.root.context).apply {
                val size = binding.root.resources.getDimensionPixelSize(
                    if (isSelected) R.dimen.onboarding_indicator_active_width else R.dimen.onboarding_indicator_size
                )
                val params = ViewGroup.MarginLayoutParams(
                    size,
                    binding.root.resources.getDimensionPixelSize(R.dimen.onboarding_indicator_size)
                )
                val margin = binding.root.resources.getDimensionPixelSize(R.dimen.onboarding_indicator_spacing)
                params.marginStart = margin
                params.marginEnd = margin
                layoutParams = params
                background = ContextCompat.getDrawable(
                    binding.root.context,
                    if (isSelected) {
                        R.drawable.bg_onboarding_indicator_active
                    } else {
                        R.drawable.bg_onboarding_indicator_inactive
                    }
                )
            }
        }
    }
}

