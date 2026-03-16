package com.example.flashcardapp.ui.feature.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentLoginBinding

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        setupListeners()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupListeners() {
        binding.textRegisterNow.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        binding.textForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }
        binding.buttonLogin.setOnClickListener { submitLogin() }

        binding.inputEmail.doAfterTextChanged { binding.layoutEmail.error = null }
        binding.inputPassword.doAfterTextChanged { binding.layoutPassword.error = null }
    }

    private fun submitLogin() {
        val email = binding.inputEmail.text?.toString()?.trim().orEmpty()
        val password = binding.inputPassword.text?.toString().orEmpty()

        val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val isPasswordValid = password.length >= MIN_PASSWORD_LENGTH

        binding.layoutEmail.error = if (isEmailValid) null else "Email không hợp lệ"
        binding.layoutPassword.error =
            if (isPasswordValid) null else "Mật khẩu phải có ít nhất 6 ký tự"

        if (!isEmailValid || !isPasswordValid) return

        val payload = mapOf(
            "email" to email,
            "password" to password
        )

        handleLogin(payload)
    }

    // TODO: integrate login API here
    private fun handleLogin(payload: Map<String, String>) {
        // Backend login request will be added later.
        // Example payload: { email, password }
    }

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
    }
}
