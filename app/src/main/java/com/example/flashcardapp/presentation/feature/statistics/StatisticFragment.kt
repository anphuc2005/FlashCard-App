package com.example.flashcardapp.presentation.feature.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
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
import com.example.flashcardapp.presentation.feature.statistics.adapter.DeckStatisticsAdapter
import com.example.flashcardapp.presentation.feature.statistics.adapter.StatisticAchievementAdapter
import com.example.flashcardapp.presentation.feature.statistics.model.StatisticAchievementItem
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class StatisticFragment : Fragment() {

    private var _binding: FragmentStatisticBinding? = null
    private val binding get() = _binding!!

    private val statisticFormatter = StatisticFormatter()
    private val viewModel: StatisticViewModel by viewModels {
        val container = (requireActivity().application as FlashcardApp).container
        StatisticViewModelFactory(container.statisticsRepository)
    }
    private lateinit var achievementAdapter: StatisticAchievementAdapter
    private lateinit var deckStatisticsAdapter: DeckStatisticsAdapter
    private var allAchievements: List<StatisticAchievementItem> = emptyList()

    private var avatarLoadJob: Job? = null

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

        deckStatisticsAdapter = DeckStatisticsAdapter(statisticFormatter)
        binding.rvDeckProgress.apply {
            adapter = deckStatisticsAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupListeners() {
        binding.btnWeeklyDetail.setOnClickListener {
            viewModel.refreshData()
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
                        state.isInitialized && state.summary != null -> renderSuccess(state)
                    }
                }
            }
        }
    }

    private fun renderLoading() {
        renderLoadingState(true)
        binding.btnWeeklyDetail.isEnabled = false
        binding.btnAchievementDetail.isEnabled = false
        binding.tvStreak.text = "--"
        binding.streakValue.text = getString(R.string.stat_placeholder_streak)
        binding.learnedValue.text = "--"
        binding.weeklyTime.text = "--.-%"
        binding.weeklyNewCards.text = getString(R.string.stat_placeholder_cards)
        binding.weeklyChart.setData(emptyList(), highlight = -1)
        allAchievements = emptyList()
        achievementAdapter.submitList(emptyList())
        deckStatisticsAdapter.submitList(emptyList())
    }

    private fun renderSuccess(state: StatisticUiState) {
        val summary = state.summary ?: return
        renderLoadingState(false)

        binding.btnWeeklyDetail.isEnabled = true
        binding.btnAchievementDetail.isEnabled = true
        allAchievements = state.allAchievements

        binding.helloName.text = getString(R.string.stat_hello_placeholder)
        binding.tvStreak.text = summary.currentStreak.toString()
        binding.streakValue.text = getString(R.string.stat_streak_days, summary.currentStreak)
        binding.learnedValue.text = formatCount(summary.totalStudied)
        binding.weeklyTime.text = String.format(Locale.US, "%.1f%%", summary.retentionRate)

        val reviewedCards = state.chartData.sumOf { it.count }
            .coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong())
            .toInt()
        binding.weeklyNewCards.text = getString(
            R.string.stat_cards_count,
            statisticFormatter.formatNumber(reviewedCards)
        )

        val chartEntries = state.chartData.map { item ->
            WeeklyBarChartView.DayEntry(
                label = formatDateLabel(item.date),
                value = item.count.toFloat()
            )
        }

        val highlightIndex = chartEntries.lastIndex
        binding.weeklyChart.setData(chartEntries, highlight = highlightIndex)

        achievementAdapter.submitList(state.achievements)
        deckStatisticsAdapter.submitList(state.deckStatistics)
    }

    private fun renderError(message: String) {
        renderLoadingState(false)
        binding.btnWeeklyDetail.isEnabled = true
        binding.btnAchievementDetail.isEnabled = false
        binding.tvStreak.text = "0"
        binding.streakValue.text = getString(R.string.stat_streak_days, 0)
        binding.learnedValue.text = statisticFormatter.formatNumber(0)
        binding.weeklyTime.text = "0.0%"
        binding.weeklyNewCards.text = getString(R.string.stat_cards_count, statisticFormatter.formatNumber(0))
        binding.weeklyChart.setData(emptyList(), highlight = -1)
        allAchievements = emptyList()
        achievementAdapter.submitList(emptyList())
        deckStatisticsAdapter.submitList(emptyList())
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

    private fun formatCount(value: Long): String {
        val intValue = value.coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()
        return statisticFormatter.formatNumber(intValue)
    }

    private fun formatDateLabel(isoDate: String): String {
        return runCatching {
            LocalDate.parse(isoDate).format(DateTimeFormatter.ofPattern("dd/MM"))
        }.getOrDefault(isoDate)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun renderLoadingState(isLoading: Boolean) {
        binding.contentScroll.visibility = if (isLoading) View.GONE else View.VISIBLE
        binding.skeletonStatistic.visibility = if (isLoading) View.VISIBLE else View.GONE
        if (isLoading && binding.skeletonStatistic.animation == null) {
            binding.skeletonStatistic.startAnimation(
                AnimationUtils.loadAnimation(requireContext(), R.anim.skeleton_pulse)
            )
        } else if (!isLoading) {
            binding.skeletonStatistic.clearAnimation()
        }
    }
}
