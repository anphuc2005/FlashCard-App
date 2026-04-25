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
import com.example.flashcardapp.R
import com.example.flashcardapp.core.utils.chart.WeeklyBarChartView
import com.example.flashcardapp.databinding.FragmentStatisticBinding
import com.example.flashcardapp.presentation.common.notification.showAppError
import com.example.flashcardapp.presentation.common.notification.showAppWarning
import com.example.flashcardapp.presentation.feature.statistics.adapter.DeckStatisticsAdapter
import com.example.flashcardapp.presentation.feature.statistics.adapter.StatisticAchievementAdapter
import com.example.flashcardapp.presentation.feature.statistics.model.StatisticAchievementItem
import kotlinx.coroutines.launch
import java.time.LocalDate

class StatisticFragment : Fragment() {

    private var _binding: FragmentStatisticBinding? = null
    private val binding get() = _binding!!

    private val statisticFormatter = StatisticFormatter()
    private val viewModel: StatisticViewModel by viewModels()
    private lateinit var achievementAdapter: StatisticAchievementAdapter
    private lateinit var deckStatisticsAdapter: DeckStatisticsAdapter
    private var allAchievements: List<StatisticAchievementItem> = emptyList()

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
        binding.btnWeeklyDetail.isEnabled = false
        binding.btnAchievementDetail.isEnabled = false
        binding.tvStreak.text = "--"
        binding.streakValue.text = getString(R.string.stat_placeholder_streak)
        binding.learnedValue.text = "--"
        binding.weeklyTime.text = getString(R.string.stat_placeholder_time)
        binding.weeklyNewCards.text = getString(R.string.stat_placeholder_cards)
        binding.weeklyChart.setData(emptyList(), highlight = -1)
        allAchievements = emptyList()
        achievementAdapter.submitList(emptyList())
        deckStatisticsAdapter.submitList(emptyList())
    }

    private fun renderSuccess(state: StatisticUiState) {
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

        val chartEntries = timeStatistics.labels.zip(timeStatistics.values).map { (label, value) ->
            WeeklyBarChartView.DayEntry(label = label, value = value.toFloat())
        }

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
        deckStatisticsAdapter.submitList(state.deckStatistics)
    }

    private fun renderError(message: String) {
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
        deckStatisticsAdapter.submitList(emptyList())
        val safeMessage = if (message.isBlank()) getString(R.string.stat_error_load) else message
        showAppError(safeMessage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
