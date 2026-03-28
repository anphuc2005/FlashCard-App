package com.example.flashcardapp.presentation.feature.account

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.DialogThemeBinding
import androidx.core.graphics.drawable.toDrawable

class ThemeDialog : DialogFragment() {

    enum class ThemeOption { LIGHT, DARK }

    interface Listener {
        fun onThemeSaved(option: ThemeOption)
    }

    var listener: Listener? = null

    private var _binding: DialogThemeBinding? = null
    private val binding get() = _binding!!

    private var selected = ThemeOption.LIGHT

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogThemeBinding.inflate(LayoutInflater.from(requireContext()))

        selected = arguments?.getString(ARG_THEME)?.let { ThemeOption.valueOf(it) } ?: selected

        setupUi()

        return Dialog(requireContext()).apply {
            setContentView(binding.root)
            window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        }
    }

    override fun onStart() {
        super.onStart()
        // Ensure dialog uses full available width for its content.
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupUi() {
        binding.cardLight.setOnClickListener { updateSelection(ThemeOption.LIGHT) }
        binding.cardDark.setOnClickListener { updateSelection(ThemeOption.DARK) }

        binding.btnSave.setOnClickListener {
            listener?.onThemeSaved(selected)
            dismiss()
        }

        updateSelection(selected)
    }

    private fun updateSelection(option: ThemeOption) {
        selected = option
        val selectedBg = R.drawable.bg_theme_option_selected
        val unselectedBg = R.drawable.bg_theme_option_unselected

        val lightSelected = option == ThemeOption.LIGHT

        binding.cardLight.setBackgroundResource(if (lightSelected) selectedBg else unselectedBg)
        binding.cardDark.setBackgroundResource(if (lightSelected) unselectedBg else selectedBg)

        binding.tvLight.setTextColor(if (lightSelected) COLOR_PRIMARY else COLOR_TEXT)
        binding.tvDark.setTextColor(if (lightSelected) COLOR_TEXT else COLOR_PRIMARY)
    }

    companion object {
        private const val ARG_THEME = "arg_theme"
        private const val COLOR_PRIMARY = 0xFF0A63E8.toInt()
        private const val COLOR_TEXT = 0xFF1F2B3C.toInt()

        fun newInstance(option: ThemeOption): ThemeDialog = ThemeDialog().apply {
            arguments = Bundle().apply { putString(ARG_THEME, option.name) }
        }
    }
}
