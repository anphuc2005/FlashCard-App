package com.example.flashcardapp.presentation.feature.statistics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.DialogAchievementDetailBinding
import com.example.flashcardapp.presentation.feature.statistics.adapter.SystemAchievementAdapter
import com.example.flashcardapp.presentation.feature.statistics.model.StatisticAchievementItem

const val ACHIEVEMENT_DETAIL_DIALOG_TAG = "AchievementDetailDialog"
private const val ARG_ACHIEVEMENTS = "arg_achievements"

fun createAchievementDetailDialog(
    achievements: List<StatisticAchievementItem>
): AchievementDetailDialogFragment {
    return AchievementDetailDialogFragment().apply {
        arguments = Bundle().apply {
            putSerializable(ARG_ACHIEVEMENTS, ArrayList(achievements))
        }
    }
}

class AchievementDetailDialogFragment : DialogFragment() {

    private var _binding: DialogAchievementDetailBinding? = null
    private val binding get() = _binding!!
    private val achievementAdapter = SystemAchievementAdapter()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAchievementDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val achievements = getAchievementsFromArgs()
        val unlockedCount = achievements.count { it.isUnlocked }

        binding.tvAchievementSummary.text = getString(
            R.string.stat_achievement_summary,
            unlockedCount,
            achievements.size
        )
        binding.rvAllAchievements.apply {
            adapter = achievementAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        achievementAdapter.submitList(achievements)

        binding.btnClose.setOnClickListener { dismiss() }
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.92f).toInt()
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun getAchievementsFromArgs(): List<StatisticAchievementItem> {
        @Suppress("DEPRECATION")
        val serialized = arguments?.getSerializable(ARG_ACHIEVEMENTS)
        return (serialized as? ArrayList<*>)?.filterIsInstance<StatisticAchievementItem>().orEmpty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
