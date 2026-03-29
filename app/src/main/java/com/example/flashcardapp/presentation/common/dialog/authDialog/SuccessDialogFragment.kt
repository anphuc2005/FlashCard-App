package com.example.flashcardapp.presentation.common.dialog.authDialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.DialogSuccessBinding

class SuccessDialogFragment : DialogFragment() {

    private var _binding: DialogSuccessBinding? = null
    private val binding get() = _binding!!

    private var title: String? = null
    private var message: String? = null
    private var buttonText: String? = null
    private var onContinueClick: (() -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_FlashCardApp)
        arguments?.let {
            title = it.getString(ARG_TITLE)
            message = it.getString(ARG_MESSAGE)
            buttonText = it.getString(ARG_BUTTON_TEXT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogSuccessBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false

        if (!title.isNullOrBlank()) {
            binding.tvTitle.text = title
        }

        if (!message.isNullOrBlank()) {
            binding.tvMessage.text = message
        }

        if (!buttonText.isNullOrBlank()) {
            binding.btnContinue.text = buttonText
        }

        binding.btnContinue.setOnClickListener {
            dismiss()
            onContinueClick?.invoke()
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    fun setOnContinueClickListener(listener: () -> Unit) {
        onContinueClick = listener
    }

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_MESSAGE = "arg_message"
        private const val ARG_BUTTON_TEXT = "arg_button_text"

        fun newInstance(
            title: String? = null,
            message: String? = null,
            buttonText: String? = null
        ): SuccessDialogFragment {
            return SuccessDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_MESSAGE, message)
                    putString(ARG_BUTTON_TEXT, buttonText)
                }
            }
        }
    }
}

