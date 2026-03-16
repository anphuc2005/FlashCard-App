package com.example.flashcardapp.ui.feature.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentForgotPasswordBinding

class ForgotPasswordFragment : Fragment(R.layout.fragment_forgot_password) {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentForgotPasswordBinding.bind(view)

        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.buttonSendVerificationCode.setOnClickListener {
            // TODO: trigger forgot-password request and navigate to OTP verification
            findNavController().navigate(R.id.action_forgotPasswordFragment_to_otpVerificationFragment)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
