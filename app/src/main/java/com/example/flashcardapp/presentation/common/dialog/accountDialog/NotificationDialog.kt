package com.example.flashcardapp.presentation.common.dialog.accountDialog

import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.os.bundleOf
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.DialogFragment
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.DialogNotificationBinding

class NotificationDialog : DialogFragment() {

    private var _binding: DialogNotificationBinding? = null
    private val binding get() = _binding!!

    private var study = true
    private var newDeck = false
    private var achievement = true



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogNotificationBinding.inflate(LayoutInflater.from(requireContext()))

        study = arguments?.getBoolean(ARG_STUDY) ?: study
        newDeck = arguments?.getBoolean(ARG_NEW_DECK) ?: newDeck
        achievement = arguments?.getBoolean(ARG_ACHIEVEMENT) ?: achievement

        setupUi()

        return Dialog(requireContext()).apply {
            setContentView(binding.root)
            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun updateSwitchTint() {
        fun tintSwitch(checked: Boolean, thumbRes: Int, trackRes: Int): Pair<ColorStateList, ColorStateList> {
            val thumb = if (checked) thumbRes else R.color.switch_thumb_gray
            val track = if (checked) trackRes else R.color.switch_track_gray
            val thumbList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), thumb))
            val trackList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), track))
            return thumbList to trackList
        }

        val (studyThumb, studyTrack) = tintSwitch(binding.switchStudy.isChecked, R.color.switch_thumb_blue, R.color.switch_track_blue)
        binding.switchStudy.thumbTintList = studyThumb
        binding.switchStudy.trackTintList = studyTrack

        val (newDeckThumb, newDeckTrack) = tintSwitch(binding.switchNewDeck.isChecked, R.color.switch_thumb_blue, R.color.switch_track_blue)
        binding.switchNewDeck.thumbTintList = newDeckThumb
        binding.switchNewDeck.trackTintList = newDeckTrack

        val (achievementThumb, achievementTrack) = tintSwitch(binding.switchAchievement.isChecked, R.color.switch_thumb_blue, R.color.switch_track_blue)
        binding.switchAchievement.thumbTintList = achievementThumb
        binding.switchAchievement.trackTintList = achievementTrack
    }


    private fun setupUi() {
        binding.switchStudy.isChecked = study
        binding.switchNewDeck.isChecked = newDeck
        binding.switchAchievement.isChecked = achievement

        updateSwitchTint()

        binding.switchStudy.setOnCheckedChangeListener { _, _ -> updateSwitchTint() }
        binding.switchNewDeck.setOnCheckedChangeListener { _, _ -> updateSwitchTint() }
        binding.switchAchievement.setOnCheckedChangeListener { _, _ -> updateSwitchTint() }

        binding.btnApply.setOnClickListener {
            parentFragmentManager.setFragmentResult(
                RESULT_KEY,
                bundleOf(
                    RESULT_STUDY to binding.switchStudy.isChecked,
                    RESULT_NEW_DECK to binding.switchNewDeck.isChecked,
                    RESULT_ACHIEVEMENT to binding.switchAchievement.isChecked
                )
            )
            dismiss()
        }
    }

    companion object {
        private const val ARG_STUDY = "arg_study"
        private const val ARG_NEW_DECK = "arg_new_deck"
        private const val ARG_ACHIEVEMENT = "arg_achievement"
        const val RESULT_KEY = "notification_settings_result"
        const val RESULT_STUDY = "result_study"
        const val RESULT_NEW_DECK = "result_new_deck"
        const val RESULT_ACHIEVEMENT = "result_achievement"

        fun newInstance(study: Boolean, newDeck: Boolean, achievement: Boolean): NotificationDialog =
            NotificationDialog().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_STUDY, study)
                    putBoolean(ARG_NEW_DECK, newDeck)
                    putBoolean(ARG_ACHIEVEMENT, achievement)
                }
            }
    }
}
