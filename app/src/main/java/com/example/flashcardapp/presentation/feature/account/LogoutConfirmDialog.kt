package com.example.flashcardapp.presentation.feature.account

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.DialogFragment
import com.example.flashcardapp.databinding.DialogLogoutConfirmBinding

class LogoutConfirmDialog : DialogFragment() {

    interface Listener {
        fun onConfirmLogout()
        fun onCancelLogout() {}
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

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupUi() {
        binding.btnCancel.setOnClickListener {
            listener?.onCancelLogout()
            dismiss()
        }

        binding.btnLogout.setOnClickListener {
            listener?.onConfirmLogout()
            dismiss()
        }
    }
}
