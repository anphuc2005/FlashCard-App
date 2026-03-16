package com.example.flashcardapp.ui.feature.onboarding

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
            title = "Học nhanh, nhớ lâu",
            description = "Ghi nhớ kiến thức hiệu quả hơn với phương pháp lặp lại ngắt quãng."
        ),
        OnboardingPagerItem(
            iconResId = R.drawable.ic_onboard2,
            title = "Duy trì thói quen",
            description = "Hệ thống nhắc nhở thông minh giúp bạn không bỏ lỡ ngày học nào."
        ),
        OnboardingPagerItem(
            iconResId = R.drawable.ic_onboard3,
            title = "Sáng tạo bộ thẻ",
            description = "Dễ dàng tạo bộ thẻ riêng hoặc khám phá thêm nhiều nội dung từ cộng đồng."
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
            binding.textTitle.text = item.title
            binding.textDescription.text = item.description

            binding.buttonBack.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
            binding.buttonBack.setOnClickListener { onBackClick(position) }
            binding.textSkip.setOnClickListener { onSkipClick() }
        }
    }
}

data class OnboardingPagerItem(
    val iconResId: Int,
    val title: String,
    val description: String
)
