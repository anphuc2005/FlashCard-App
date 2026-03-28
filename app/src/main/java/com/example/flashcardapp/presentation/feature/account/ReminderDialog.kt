package com.example.flashcardapp.presentation.feature.account

import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.DialogReminderBinding
import com.google.android.material.color.MaterialColors
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.core.graphics.drawable.toDrawable
import androidx.core.content.ContextCompat

/** Dialog that controls reminder time and enable state. */
class ReminderDialog : DialogFragment() {

    interface Listener {
        fun onReminderSaved(hour: Int, minute: Int, enabled: Boolean)
        fun onReminderHistory()
    }

    var listener: Listener? = null

    private var _binding: DialogReminderBinding? = null
    private val binding get() = _binding!!

    private var hour: Int = 8
    private var minute: Int = 0
    private var enabled: Boolean = true

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogReminderBinding.inflate(LayoutInflater.from(requireContext()))

        hour = arguments?.getInt(ARG_HOUR) ?: hour
        minute = arguments?.getInt(ARG_MINUTE) ?: minute
        enabled = arguments?.getBoolean(ARG_ENABLED) ?: enabled

        setupUi()

        val dialog = Dialog(requireContext())
        dialog.setContentView(binding.root)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        return dialog
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupUi() {
        binding.switchEnable.isChecked = enabled
        updateSwitchTint(enabled)
        updateTimeDisplay()
        updateAmPmToggle()

        binding.blockTime.setOnClickListener { openTimePicker() }
        binding.tvTime.setOnClickListener { openTimePicker() }

        binding.btnAm.setOnClickListener {
            if (hour >= 12) {
                hour = if (hour == 12) 0 else hour - 12
                updateTimeDisplay()
                updateAmPmToggle()
            }
        }

        binding.btnPm.setOnClickListener {
            if (hour < 12) {
                hour += 12
                updateTimeDisplay()
                updateAmPmToggle()
            }
        }

        binding.btnHistory.setOnClickListener {
            listener?.onReminderHistory()
        }

        binding.switchEnable.setOnCheckedChangeListener { _, isChecked ->
            enabled = isChecked
            updateSwitchTint(isChecked)
        }

        binding.btnSave.setOnClickListener {
            listener?.onReminderSaved(hour, minute, binding.switchEnable.isChecked)
            dismiss()
        }
    }

    private fun updateSwitchTint(isChecked: Boolean) {
        val thumbColor = if (isChecked) R.color.switch_thumb_blue else R.color.switch_thumb_gray
        val trackColor = if (isChecked) R.color.switch_track_blue else R.color.switch_track_gray
        binding.switchEnable.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), thumbColor))
        binding.switchEnable.trackTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(), trackColor))
    }

    private fun openTimePicker() {
        TimePickerDialog(
            requireContext(),
            { _, pickedHour, pickedMinute ->
                hour = pickedHour
                minute = pickedMinute
                updateTimeDisplay()
                updateAmPmToggle()
            },
            hour,
            minute,
            false
        ).show()
    }

    private fun updateTimeDisplay() {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        val formatted = SimpleDateFormat("hh:mm", Locale.getDefault()).format(calendar.time)
        binding.tvTime.text = formatted
    }

    private fun updateAmPmToggle() {
        val isAm = hour < 12
        val onBg = requireContext().getDrawable(R.drawable.bg_seg_on)
        val offTextColor = MaterialColors.getColor(binding.root, R.attr.textColor)

        binding.btnAm.background = if (isAm) onBg else null
        binding.btnPm.background = if (isAm) null else onBg

        binding.btnAm.setTextColor(if (isAm) Color.WHITE else offTextColor)
        binding.btnPm.setTextColor(if (isAm) offTextColor else Color.WHITE)
    }

    companion object {
        private const val ARG_HOUR = "arg_hour"
        private const val ARG_MINUTE = "arg_minute"
        private const val ARG_ENABLED = "arg_enabled"

        fun newInstance(hour: Int, minute: Int, enabled: Boolean): ReminderDialog {
            return ReminderDialog().apply {
                arguments = Bundle().apply {
                    putInt(ARG_HOUR, hour)
                    putInt(ARG_MINUTE, minute)
                    putBoolean(ARG_ENABLED, enabled)
                }
            }
        }
    }
}
