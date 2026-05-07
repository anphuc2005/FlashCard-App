package com.example.flashcardapp.presentation.common.dialog.accountDialog

import android.app.Dialog
import android.content.res.ColorStateList
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.DialogLogoutConfirmBinding
import com.google.android.material.color.MaterialColors

class AppConfirmDialog : DialogFragment() {

    interface Listener {
        fun onConfirm()
        fun onCancel() {}
    }

    var listener: Listener? = null

    private var _binding: DialogLogoutConfirmBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogLogoutConfirmBinding.inflate(LayoutInflater.from(requireContext()))
        setupUi()
        return Dialog(requireContext()).apply {
            setContentView(binding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        listener?.onCancel()
        super.onCancel(dialog)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupUi() {
        val args = requireArguments()
        binding.tvTitle.text = args.getString(ARG_TITLE)
        binding.tvMessage.text = args.getString(ARG_MESSAGE)
        binding.btnLogout.text = args.getString(ARG_CONFIRM_TEXT)
        binding.btnCancel.text = args.getString(ARG_CANCEL_TEXT)
        binding.imageView.setImageResource(args.getInt(ARG_ICON_RES))

        val destructive = args.getBoolean(ARG_DESTRUCTIVE, true)
        val actionColor = if (destructive) {
            ContextCompat.getColor(requireContext(), R.color.md_icon_red)
        } else {
            MaterialColors.getColor(binding.root, R.attr.buttonColor)
        }
        binding.btnLogout.backgroundTintList = ColorStateList.valueOf(actionColor)

        binding.btnCancel.setOnClickListener {
            listener?.onCancel()
            dismiss()
        }
        binding.btnLogout.setOnClickListener {
            listener?.onConfirm()
            dismiss()
        }
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_MESSAGE = "message"
        private const val ARG_CONFIRM_TEXT = "confirm_text"
        private const val ARG_CANCEL_TEXT = "cancel_text"
        private const val ARG_ICON_RES = "icon_res"
        private const val ARG_DESTRUCTIVE = "destructive"

        fun newInstance(
            title: String,
            message: String,
            confirmText: String,
            cancelText: String,
            iconRes: Int = R.drawable.ic_logout,
            destructive: Boolean = true
        ): AppConfirmDialog {
            return AppConfirmDialog().apply {
                arguments = bundleOf(
                    ARG_TITLE to title,
                    ARG_MESSAGE to message,
                    ARG_CONFIRM_TEXT to confirmText,
                    ARG_CANCEL_TEXT to cancelText,
                    ARG_ICON_RES to iconRes,
                    ARG_DESTRUCTIVE to destructive
                )
            }
        }
    }
}
