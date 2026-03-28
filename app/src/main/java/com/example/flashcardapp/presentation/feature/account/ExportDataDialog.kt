package com.example.flashcardapp.presentation.feature.account

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.DialogExportDataBinding

class ExportDataDialog : DialogFragment() {

    enum class ExportFormat { CSV, JSON }

    interface Listener {
        fun onExport(format: ExportFormat)
    }

    var listener: Listener? = null

    private var _binding: DialogExportDataBinding? = null
    private val binding get() = _binding!!

    private var selected = ExportFormat.CSV

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogExportDataBinding.inflate(LayoutInflater.from(requireContext()))

        selected = arguments?.getString(ARG_FORMAT)?.let { ExportFormat.valueOf(it) } ?: selected

        setupUi()

        return Dialog(requireContext()).apply {
            setContentView(binding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupUi() {
        binding.rowCsv.setOnClickListener { updateSelection(ExportFormat.CSV) }
        binding.rowJson.setOnClickListener { updateSelection(ExportFormat.JSON) }

        binding.btnExport.setOnClickListener {
            listener?.onExport(selected)
            dismiss()
        }

        updateSelection(selected)
    }

    private fun updateSelection(format: ExportFormat) {
        selected = format
        val onBg = R.drawable.bg_option_selected
        val offBg = R.drawable.bg_option_unselected

        val isCsv = format == ExportFormat.CSV

        binding.rowCsv.setBackgroundResource(if (isCsv) onBg else offBg)
        binding.rowJson.setBackgroundResource(if (isCsv) offBg else onBg)

        binding.radioCsv.setImageResource(if (isCsv) R.drawable.ic_radio_on else R.drawable.ic_radio_off)
        binding.radioJson.setImageResource(if (isCsv) R.drawable.ic_radio_off else R.drawable.ic_radio_on)
    }

    companion object {
        private const val ARG_FORMAT = "arg_format"

        fun newInstance(format: ExportFormat): ExportDataDialog = ExportDataDialog().apply {
            arguments = Bundle().apply { putString(ARG_FORMAT, format.name) }
        }
    }
}
