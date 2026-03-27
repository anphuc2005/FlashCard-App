package com.example.flashcardapp.presentation.feature.onboarding

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentOnboardingPagerBinding

class OnboardingPagerFragment : Fragment(R.layout.fragment_onboarding_pager) {

    private var _binding: FragmentOnboardingPagerBinding? = null
    private val binding get() = _binding!!

    private lateinit var onboardingPagerAdapter: OnboardingScreenAdapter
    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            onboardingPagerAdapter.updateCurrentPage(position)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOnboardingPagerBinding.bind(view)

        onboardingPagerAdapter = OnboardingScreenAdapter(
            pages = onboardingPages,
            onPrimaryClick = ::handlePrimaryAction
        )

        binding.viewPagerOnboarding.adapter = onboardingPagerAdapter
        binding.viewPagerOnboarding.offscreenPageLimit = onboardingPages.size
        binding.viewPagerOnboarding.registerOnPageChangeCallback(pageChangeCallback)

        onboardingPagerAdapter.updateCurrentPage(binding.viewPagerOnboarding.currentItem)
    }

    override fun onDestroyView() {
        binding.viewPagerOnboarding.unregisterOnPageChangeCallback(pageChangeCallback)
        binding.viewPagerOnboarding.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private fun handlePrimaryAction(position: Int) {
        if (position < onboardingPages.lastIndex) {
            binding.viewPagerOnboarding.currentItem = position + 1
        } else {
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        // TODO: navigate to next screen (login or main screen)
    }

    private val onboardingPages = listOf(
        OnboardingPageUiModel(
            iconResId = R.drawable.ic_onboard1,
            title = "Học nhanh, nhớ lâu",
            description = "Ghi nhớ kiến thức hiệu quả hơn với phương pháp lặp lại ngắt quãng.",
            buttonText = "Tiếp tục"
        ),
        OnboardingPageUiModel(
            iconResId = R.drawable.ic_onboard2,
            title = "Duy trì thói quen",
            description = "Hệ thống nhắc nhở thông minh giúp bạn không bỏ lỡ ngày học nào.",
            buttonText = "Bắt đầu ngay"
        )
    )
}

