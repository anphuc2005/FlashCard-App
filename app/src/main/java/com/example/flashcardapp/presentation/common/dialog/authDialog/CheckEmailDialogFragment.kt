package com.example.flashcardapp.presentation.common.dialog.authDialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.DialogCheckEmailBinding

class CheckEmailDialogFragment : DialogFragment() {

    private var _binding: DialogCheckEmailBinding? = null
    private val binding get() = _binding!!

    private var email: String? = null
    private var onUnderstandClick: (() -> Unit)? = null
    private var onResendClick: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_FlashCardApp)
        email = arguments?.getString(ARG_EMAIL)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            setDimAmount(0.5f)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCheckEmailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false

        binding.tvMessage.text = getString(
            R.string.dialog_check_email_message,
            email ?: getString(R.string.app_login_email_hint)
        )

        binding.btnOk.setOnClickListener {
            dismiss()
            onUnderstandClick?.invoke()
        }

        binding.btnResend.setOnClickListener {
            dismiss()
            onResendClick?.invoke()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    fun setOnUnderstandClickListener(listener: () -> Unit) {
        onUnderstandClick = listener
    }

    fun setOnResendClickListener(listener: () -> Unit) {
        onResendClick = listener
    }

    companion object {
        private const val ARG_EMAIL = "arg_email"

        fun newInstance(email: String? = null): CheckEmailDialogFragment {
            return CheckEmailDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_EMAIL, email)
                }
            }
        }
    }
}

