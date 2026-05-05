package com.example.flashcardapp.presentation.feature.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.R
import com.example.flashcardapp.core.utils.chart.WeeklyBarChartView
import com.example.flashcardapp.databinding.FragmentStatisticBinding
import com.example.flashcardapp.presentation.common.notification.showAppError
import com.example.flashcardapp.presentation.common.notification.showAppWarning
import com.example.flashcardapp.presentation.feature.statistics.adapter.StatisticAchievementAdapter
import com.example.flashcardapp.presentation.feature.statistics.model.StatisticAchievementItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate
import androidx.core.view.isVisible

class StatisticFragment : Fragment() {

    private var _binding: FragmentStatisticBinding? = null
    private val binding get() = _binding!!

    private val statisticFormatter = StatisticFormatter()
    private val viewModel: StatisticViewModel by viewModels {
        val container = (requireActivity().application as FlashcardApp).container
        StatisticViewModelFactory(container.statisticsRepository)
    }
    private lateinit var achievementAdapter: StatisticAchievementAdapter
    private var allAchievements: List<StatisticAchievementItem> = emptyList()

    private var avatarLoadJob: Job? = null

    private var skipNextResumeAvatarRefresh = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        observeUiState()
        setupListeners()
        renderCachedAvatar()
        loadUserAvatar(forceRefresh = false)
    }

    private fun setupAdapters() {
        achievementAdapter = StatisticAchievementAdapter()
        binding.rvBadges.apply {
            adapter = achievementAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupListeners() {
        binding.btnWeeklyDetail.setOnClickListener {
            viewModel.changeRange(STAT_RANGE_WEEK)
        }
        binding.btnAchievementDetail.setOnClickListener {
            if (allAchievements.isEmpty()) {
                showAppWarning(getString(R.string.stat_no_achievement_data))
            } else {
                createAchievementDetailDialog(allAchievements)
                    .show(childFragmentManager, ACHIEVEMENT_DETAIL_DIALOG_TAG)
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when {
                        state.isLoading -> renderLoading()
                        state.errorMessage != null -> renderError(state.errorMessage)
                        state.isInitialized && state.overview != null && state.timeStatistics != null -> {
                            renderSuccess(state)
                        }
                    }
                }
            }
        }
    }

    private fun renderLoading() {
        binding.weeklyBlurPlaceholder.isVisible = true
        binding.weeklyContent.alpha = 0.35f
        binding.btnWeeklyDetail.isEnabled = false
        binding.btnAchievementDetail.isEnabled = false
        binding.tvStreak.text = "--"
        binding.streakValue.text = getString(R.string.stat_placeholder_streak)
        binding.learnedValue.text = "--"
        binding.weeklyTime.text = getString(R.string.stat_placeholder_time)
        binding.weeklyNewCards.text = getString(R.string.stat_placeholder_cards)
        binding.weeklyChart.setData(emptyList(), highlight = -1)
        if (allAchievements.isEmpty()) {
            allAchievements = viewModel.uiState.value.allAchievements
        }
        achievementAdapter.submitList(viewModel.uiState.value.achievements)
    }

    private fun renderSuccess(state: StatisticUiState) {
        binding.weeklyBlurPlaceholder.isVisible = false
        binding.weeklyContent.alpha = 1f
        val overview = state.overview ?: return
        val timeStatistics = state.timeStatistics ?: return

        binding.btnWeeklyDetail.isEnabled = true
        binding.btnAchievementDetail.isEnabled = true
        allAchievements = state.allAchievements

        binding.helloName.text = getString(R.string.stat_hello_name, overview.userName)
        binding.tvStreak.text = overview.xpToday.toString()
        binding.streakValue.text = getString(R.string.stat_streak_days, overview.streakDays)
        binding.learnedValue.text = statisticFormatter.formatNumber(overview.learnedCards)
        binding.weeklyTime.text = statisticFormatter.formatMinutes(timeStatistics.totalStudyMinutes)
        binding.weeklyNewCards.text = getString(
            R.string.stat_cards_count,
            statisticFormatter.formatNumber(timeStatistics.totalReviewedCards)
        )

        val chartEntries = buildChartEntries(timeStatistics)

        val highlightIndex = when {
            chartEntries.isEmpty() -> -1
            timeStatistics.range == STAT_RANGE_WEEK -> {
                (LocalDate.now().dayOfWeek.value - 1).coerceIn(0, chartEntries.lastIndex)
            }
            else -> {
                timeStatistics.values.indexOf(timeStatistics.values.maxOrNull() ?: 0)
                    .coerceAtLeast(-1)
            }
        }
        binding.weeklyChart.setData(chartEntries, highlight = highlightIndex)

        achievementAdapter.submitList(state.achievements)
    }

    private fun buildChartEntries(timeStatistics: com.example.flashcardapp.domain.model.statistics.TimeStatistics): List<WeeklyBarChartView.DayEntry> {
        val defaultLabels = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")
        val labels = timeStatistics.labels
        val values = timeStatistics.values
        if (labels.isEmpty() && values.isEmpty()) {
            return defaultLabels.map { WeeklyBarChartView.DayEntry(it, 0f) }
        }
        val size = maxOf(labels.size, values.size)
        return (0 until size).map { index ->
            val label = labels.getOrNull(index) ?: defaultLabels.getOrElse(index) { "${index + 1}" }
            val value = values.getOrNull(index)?.toFloat() ?: 0f
            WeeklyBarChartView.DayEntry(label = label, value = value)
        }
    }

    private fun renderError(message: String) {
        binding.weeklyBlurPlaceholder.isVisible = false
        binding.weeklyContent.alpha = 1f
        binding.btnWeeklyDetail.isEnabled = true
        binding.btnAchievementDetail.isEnabled = false
        binding.tvStreak.text = "0"
        binding.streakValue.text = getString(R.string.stat_streak_days, 0)
        binding.learnedValue.text = statisticFormatter.formatNumber(0)
        binding.weeklyTime.text = statisticFormatter.formatMinutes(0)
        binding.weeklyNewCards.text = getString(R.string.stat_cards_count, statisticFormatter.formatNumber(0))
        binding.weeklyChart.setData(emptyList(), highlight = -1)
        allAchievements = emptyList()
        achievementAdapter.submitList(emptyList())
        val safeMessage = if (message.isBlank()) getString(R.string.stat_error_load) else message
        showAppError(safeMessage)
    }

    private fun renderCachedAvatar() {
        val cachedProfile = (requireActivity().application as FlashcardApp)
            .container
            .getMyProfileUseCase
            .getCachedProfile()
        renderAvatar(cachedProfile?.avatarUrl)
    }

    private fun loadUserAvatar(forceRefresh: Boolean) {
        avatarLoadJob?.cancel()
        avatarLoadJob = viewLifecycleOwner.lifecycleScope.launch {
            val profileResult = (requireActivity().application as FlashcardApp)
                .container
                .getMyProfileUseCase(forceRefresh = forceRefresh)

            profileResult.onSuccess { profile ->
                renderAvatar(profile.avatarUrl)
            }

            profileResult.onFailure {
                renderAvatar(null)
            }
        }
    }

    private fun renderAvatar(avatarUrl: String?) {
        if (avatarUrl.isNullOrBlank()) {
            binding.imgAvatar.setImageResource(R.drawable.user)
        } else {
            Glide.with(this@StatisticFragment)
                .load(avatarUrl)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(binding.imgAvatar)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
