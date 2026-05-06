package com.example.flashcardapp.presentation.common.dialog.accountDialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.DialogReportDeckBinding
import kotlin.math.min

class ReportDeckDialog : DialogFragment() {

    interface Listener {
        fun onSubmit(deckId: String, reason: String)
    }

    var listener: Listener? = null

    private var _binding: DialogReportDeckBinding? = null
    private val binding get() = _binding!!

    private var targetDeckId: String = ""
    private var targetDeckName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        targetDeckId = arguments?.getString(ARG_DECK_ID).orEmpty()
        targetDeckName = arguments?.getString(ARG_DECK_NAME).orEmpty()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogReportDeckBinding.inflate(LayoutInflater.from(requireContext()))
        setupUi()

        return Dialog(requireContext()).apply {
            setContentView(binding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onStart() {
        super.onStart()
        val screenWidth = resources.displayMetrics.widthPixels
        val maxWidth = (520 * resources.displayMetrics.density).toInt()
        val targetWidth = min((screenWidth * 0.9f).toInt(), maxWidth)
        dialog?.window?.setLayout(
            targetWidth,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupUi() {
        binding.tvDeckName.text = getString(R.string.report_dialog_deck_label, targetDeckName)

        binding.reasonInput.doAfterTextChanged {
            binding.reasonInputLayout.error = null
        }

        binding.btnCancel.setOnClickListener {
            dismiss()
        }

        binding.btnSubmit.setOnClickListener {
            val reason = binding.reasonInput.text?.toString()?.trim().orEmpty()
            val validationError = validateReason(reason)
            if (validationError != null) {
                binding.reasonInputLayout.error = validationError
                return@setOnClickListener
            }

            listener?.onSubmit(targetDeckId, reason)
            dismiss()
        }
    }

    private fun validateReason(reason: String): String? {
        return when {
            reason.length < 10 -> getString(R.string.report_dialog_reason_too_short)
            reason.length > 500 -> getString(R.string.report_dialog_reason_too_long)
            else -> null
        }
    }

    companion object {
        private const val ARG_DECK_ID = "arg_deck_id"
        private const val ARG_DECK_NAME = "arg_deck_name"

        fun newInstance(deckId: String, deckName: String): ReportDeckDialog {
            return ReportDeckDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_DECK_ID, deckId)
                    putString(ARG_DECK_NAME, deckName)
                }
            }
        }
    }
}
