package com.example.flashcardapp.ui.feature.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterBinding.bind(view)

        setupListeners()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupListeners() {
        binding.buttonBack.setOnClickListener { findNavController().popBackStack() }
        binding.textLoginNow.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }
        binding.buttonRegister.setOnClickListener { submitRegister() }

        binding.inputFullName.doAfterTextChanged { binding.layoutFullName.error = null }
        binding.inputEmail.doAfterTextChanged { binding.layoutEmail.error = null }
        binding.inputPassword.doAfterTextChanged { binding.layoutPassword.error = null }
        binding.inputConfirmPassword.doAfterTextChanged { binding.layoutConfirmPassword.error = null }
    }

    private fun submitRegister() {
        val fullName = binding.inputFullName.text?.toString()?.trim().orEmpty()
        val email = binding.inputEmail.text?.toString()?.trim().orEmpty()
        val password = binding.inputPassword.text?.toString().orEmpty()
        val confirmPassword = binding.inputConfirmPassword.text?.toString().orEmpty()

        val isFullNameValid = fullName.isNotBlank()
        val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val isPasswordValid = password.length >= MIN_PASSWORD_LENGTH
        val isConfirmPasswordValid = password == confirmPassword && confirmPassword.isNotBlank()

        binding.layoutFullName.error = if (isFullNameValid) null else "Vui lòng nhập họ và tên"
        binding.layoutEmail.error = if (isEmailValid) null else "Email không hợp lệ"
        binding.layoutPassword.error =
            if (isPasswordValid) null else "Mật khẩu phải có ít nhất 6 ký tự"
        binding.layoutConfirmPassword.error =
            if (isConfirmPasswordValid) null else "Mật khẩu xác nhận không khớp"

        if (!isFullNameValid || !isEmailValid || !isPasswordValid || !isConfirmPasswordValid) {
            return
        }

        val payload = mapOf(
            "fullName" to fullName,
            "email" to email,
            "password" to password,
            "confirmPassword" to confirmPassword
        )

        handleRegister(payload)
    }

    // TODO: integrate register API here
    private fun handleRegister(payload: Map<String, String>) {
        // Backend register request will be added later.
        // Example payload: { fullName, email, password, confirmPassword }
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
