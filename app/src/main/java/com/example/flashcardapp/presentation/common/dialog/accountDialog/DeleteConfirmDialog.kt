package com.example.flashcardapp.presentation.common.dialog.accountDialog

import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.example.flashcardapp.databinding.DialogDeleteConfirmBinding

class DeleteConfirmDialog : DialogFragment() {

    interface Listener {
        fun onConfirmDelete()
        fun onCancelDelete() {}
    }

    var listener: Listener? = null

    private var _binding: DialogDeleteConfirmBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogDeleteConfirmBinding.inflate(LayoutInflater.from(requireContext()))
        setupUi()

        return Dialog(requireContext()).apply {
            setContentView(binding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        listener?.onCancelDelete()
        super.onCancel(dialog)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupUi() {
        binding.tvTitle.text = requireArguments().getString(ARG_TITLE)
        binding.tvMessage.text = requireArguments().getString(ARG_MESSAGE)
        binding.btnDelete.text = requireArguments().getString(ARG_ACTION_TEXT)
        binding.btnCancel.text = requireArguments().getString(ARG_CANCEL_TEXT)

        binding.btnDelete.setOnClickListener {
            listener?.onConfirmDelete()
            dismiss()
        }

        binding.btnCancel.setOnClickListener {
            listener?.onCancelDelete()
            dismiss()
        }
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_MESSAGE = "message"
        private const val ARG_ACTION_TEXT = "action_text"
        private const val ARG_CANCEL_TEXT = "cancel_text"

        fun newInstance(
            title: String,
            message: String,
            actionText: String,
            cancelText: String
        ): DeleteConfirmDialog {
            return DeleteConfirmDialog().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_MESSAGE, message)
                    putString(ARG_ACTION_TEXT, actionText)
                    putString(ARG_CANCEL_TEXT, cancelText)
                }
            }
        }
    }
}
