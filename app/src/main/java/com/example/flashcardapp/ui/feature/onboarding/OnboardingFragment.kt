package com.example.flashcardapp.ui.feature.onboarding

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.flashcardapp.AppSessionManager
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentOnboardingBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class OnboardingFragment : Fragment(R.layout.fragment_onboarding) {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            updateBottomUi(position)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOnboardingBinding.bind(view)

        binding.viewPagerOnboarding.adapter = OnboardingPagerAdapter(
            onBackClick = { position ->
                if (position > 0) {
                    binding.viewPagerOnboarding.currentItem = position - 1
                }
            },
            onSkipClick = { navigateToLogin() }
        )

        binding.buttonPrimary.setOnClickListener {
            val currentPage = binding.viewPagerOnboarding.currentItem
            if (currentPage < LAST_PAGE_INDEX) {
                binding.viewPagerOnboarding.currentItem = currentPage + 1
            } else {
                navigateToLogin()
            }
        }

        binding.viewPagerOnboarding.registerOnPageChangeCallback(pageChangeCallback)
        setupIndicators()
        updateBottomUi(binding.viewPagerOnboarding.currentItem)
    }

    override fun onResume() {
        super.onResume()
        hideMainChrome()
    }

    override fun onDestroyView() {
        binding.viewPagerOnboarding.unregisterOnPageChangeCallback(pageChangeCallback)
        binding.viewPagerOnboarding.adapter = null
        _binding = null
        super.onDestroyView()
    }

    private fun navigateToLogin() {
        AppSessionManager(requireContext()).markOnboardingCompleted()
        findNavController().navigate(R.id.action_onboardingFragment_to_loginFragment)
    }

    private fun hideMainChrome() {
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)?.visibility = View.GONE
        requireActivity().findViewById<FloatingActionButton>(R.id.fabChat)?.visibility = View.GONE
    }

    private fun setupIndicators() {
        binding.indicatorContainer.removeAllViews()
        repeat(PAGE_COUNT) { index ->
            binding.indicatorContainer.addView(createIndicatorDot(index == 0))
        }
    }

    private fun updateBottomUi(position: Int) {
        binding.buttonPrimary.text = if (position == LAST_PAGE_INDEX) {
            getString(R.string.onboarding_start_now)
        } else {
            getString(R.string.onboarding_continue)
        }

        for (index in 0 until binding.indicatorContainer.childCount) {
            val dot = binding.indicatorContainer.getChildAt(index)
            val isSelected = index == position
            val params = dot.layoutParams as ViewGroup.MarginLayoutParams
            params.width = resources.getDimensionPixelSize(
                if (isSelected) {
                    R.dimen.onboarding_indicator_active_width
                } else {
                    R.dimen.onboarding_indicator_size
                }
            )
            dot.layoutParams = params
            dot.background = ContextCompat.getDrawable(
                requireContext(),
                if (isSelected) {
                    R.drawable.bg_onboarding_indicator_active
                } else {
                    R.drawable.bg_onboarding_indicator_inactive
                }
            )
        }
    }

    private fun createIndicatorDot(isSelected: Boolean): View {
        return View(requireContext()).apply {
            val width = resources.getDimensionPixelSize(
                if (isSelected) R.dimen.onboarding_indicator_active_width else R.dimen.onboarding_indicator_size
            )
            val height = resources.getDimensionPixelSize(R.dimen.onboarding_indicator_size)
            layoutParams = ViewGroup.MarginLayoutParams(width, height).apply {
                val margin = resources.getDimensionPixelSize(R.dimen.onboarding_indicator_spacing)
                marginStart = margin
                marginEnd = margin
            }
            background = ContextCompat.getDrawable(
                context,
                if (isSelected) {
                    R.drawable.bg_onboarding_indicator_active
                } else {
                    R.drawable.bg_onboarding_indicator_inactive
                }
            )
        }
    }

    private companion object {
        const val PAGE_COUNT = 3
        const val LAST_PAGE_INDEX = 2
    }
}
