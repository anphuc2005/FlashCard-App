package com.example.flashcardapp.ui.feature.auth.presentation

import android.os.Bundle
import android.os.CountDownTimer
import android.view.KeyEvent
import android.view.View
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentOtpVerificationBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class OtpVerificationFragment : Fragment(R.layout.fragment_otp_verification) {

    private var _binding: FragmentOtpVerificationBinding? = null
    private val binding get() = _binding!!

    private var resendTimer: CountDownTimer? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOtpVerificationBinding.bind(view)
        hideMainChrome()
        setupListeners()
        setupOtpInputs()
        startResendCountdown()
    }

    override fun onDestroyView() {
        resendTimer?.cancel()
        _binding = null
        super.onDestroyView()
    }

    private fun setupListeners() {
        binding.buttonBack.setOnClickListener { findNavController().popBackStack() }
        binding.buttonConfirmOtp.setOnClickListener {
            if (otpCode().length < OTP_LENGTH) {
                Toast.makeText(requireContext(), R.string.otp_error_incomplete, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(requireContext(), otpCode(), Toast.LENGTH_SHORT).show()
        }
        binding.textResendCode.setOnClickListener {
            if (binding.textResendCode.isEnabled) {
                Toast.makeText(requireContext(), R.string.otp_resent_success, Toast.LENGTH_SHORT).show()
                startResendCountdown()
            }
        }
    }

    private fun setupOtpInputs() {
        val inputs = listOf(binding.inputOtp1, binding.inputOtp2, binding.inputOtp3, binding.inputOtp4)
        inputs.forEachIndexed { index, editText ->
            editText.doAfterTextChanged { editable ->
                val value = editable?.toString().orEmpty()
                if (value.length == 1 && index < inputs.lastIndex) {
                    inputs[index + 1].requestFocus()
                }
            }
            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL &&
                    event.action == KeyEvent.ACTION_DOWN &&
                    editText.text.isNullOrEmpty() &&
                    index > 0
                ) {
                    inputs[index - 1].requestFocus()
                    inputs[index - 1].setSelection(inputs[index - 1].text?.length ?: 0)
                    true
                } else {
                    false
                }
            }
        }
    }

    private fun startResendCountdown() {
        resendTimer?.cancel()
        binding.textResendCode.isEnabled = false
        resendTimer = object : CountDownTimer(RESEND_DURATION_MS, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = (millisUntilFinished / 1000L).toInt()
                binding.textResendCode.text =
                    getString(R.string.otp_resend_countdown, secondsRemaining)
            }

            override fun onFinish() {
                binding.textResendCode.isEnabled = true
                binding.textResendCode.text = getString(R.string.otp_resend_ready)
            }
        }.start()
    }

    private fun otpCode(): String {
        return listOf(binding.inputOtp1, binding.inputOtp2, binding.inputOtp3, binding.inputOtp4)
            .joinToString(separator = "") { it.text?.toString().orEmpty().trim() }
    }

    private fun hideMainChrome() {
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)?.visibility = View.GONE
        requireActivity().findViewById<FloatingActionButton>(R.id.fabChat)?.visibility = View.GONE
    }

    private companion object {
        const val OTP_LENGTH = 4
        const val RESEND_DURATION_MS = 59_000L
    }
}
