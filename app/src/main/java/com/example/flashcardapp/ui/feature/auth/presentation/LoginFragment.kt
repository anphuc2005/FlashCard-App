package com.example.flashcardapp.ui.feature.auth.presentation

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flashcardapp.MainActivity
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentLoginBinding
import com.example.flashcardapp.ui.feature.auth.di.AuthDependencyProvider
import com.example.flashcardapp.ui.feature.auth.state.AuthOperationState
import kotlinx.coroutines.launch

// LoginFragment nhận thông tin đăng nhập và chuyển sang Main khi thành công.
class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels {
        AuthDependencyProvider.provideViewModelFactory(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        setupPasswordToggle(binding.layoutPassword, binding.inputPassword)
        setupListeners()
        observeViewModel()
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
        binding.buttonLogin.setOnClickListener { viewModel.submit() }

        binding.inputEmail.doAfterTextChanged {
            viewModel.onEmailChanged(it?.toString().orEmpty())
        }
        binding.inputPassword.doAfterTextChanged {
            viewModel.onPasswordChanged(it?.toString().orEmpty())
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.formState.collect { state ->
                        binding.layoutEmail.error = state.emailError
                        binding.layoutPassword.error = state.passwordError
                    }
                }

                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            AuthOperationState.Idle -> renderLoading(false)
                            AuthOperationState.Loading -> renderLoading(true)
                            is AuthOperationState.Success -> {
                                renderLoading(false)
                                viewModel.resetUiState()
                                // Xóa auth khỏi back stack sau khi vào app chính.
                                startActivity(
                                    Intent(requireContext(), MainActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                                )
                                requireActivity().finish()
                            }

                            is AuthOperationState.Error -> {
                                renderLoading(false)
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                                viewModel.resetUiState()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun renderLoading(isLoading: Boolean) {
        binding.buttonLogin.isEnabled = !isLoading
        binding.buttonLogin.alpha = if (isLoading) 0.7f else 1f
    }
}
