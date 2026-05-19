package com.example.flashcardapp.presentation.feature.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.ItemOnboardingPagerPageBinding

class OnboardingPagerAdapter(
    private val onBackClick: (Int) -> Unit,
    private val onSkipClick: () -> Unit
) : RecyclerView.Adapter<OnboardingPagerAdapter.OnboardingPageViewHolder>() {

    private val pages = listOf(
        OnboardingPagerItem(
            iconResId = R.drawable.ic_onboard1,
            titleResId = R.string.onboarding_title_1,
            descriptionResId = R.string.onboarding_description_1
        ),
        OnboardingPagerItem(
            iconResId = R.drawable.ic_onboard2,
            titleResId = R.string.onboarding_title_2,
            descriptionResId = R.string.onboarding_description_2
        ),
        OnboardingPagerItem(
            iconResId = R.drawable.ic_onboard3,
            titleResId = R.string.onboarding_title_3,
            descriptionResId = R.string.onboarding_description_3
        )
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingPageViewHolder {
        val binding = ItemOnboardingPagerPageBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OnboardingPageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingPageViewHolder, position: Int) {
        holder.bind(
            item = pages[position],
            position = position,
            onBackClick = onBackClick,
            onSkipClick = onSkipClick
        )
    }

    override fun getItemCount(): Int = pages.size

    class OnboardingPageViewHolder(
        private val binding: ItemOnboardingPagerPageBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: OnboardingPagerItem,
            position: Int,
            onBackClick: (Int) -> Unit,
            onSkipClick: () -> Unit
        ) {
            binding.imageOnboarding.setImageResource(item.iconResId)
            binding.textTitle.setText(item.titleResId)
            binding.textDescription.setText(item.descriptionResId)

            binding.buttonBack.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
            binding.buttonBack.setOnClickListener { onBackClick(position) }
            binding.textSkip.setOnClickListener { onSkipClick() }
        }
    }
}

data class OnboardingPagerItem(
    val iconResId: Int,
    val titleResId: Int,
    val descriptionResId: Int
)

