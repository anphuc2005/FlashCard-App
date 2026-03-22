package com.example.flashcardapp.ui.feature.auth.presentation

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flashcardapp.AppNavigator
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentRegisterBinding
import com.example.flashcardapp.ui.feature.auth.di.AuthDependencyProvider
import com.example.flashcardapp.ui.feature.auth.state.AuthOperationState
import kotlinx.coroutines.launch

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: RegisterViewModel by viewModels {
        AuthDependencyProvider.provideViewModelFactory()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterBinding.bind(view)

        setupListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupListeners() {
        binding.buttonBack.setOnClickListener { (activity as? AppNavigator)?.navigateBack() }
        binding.textLoginNow.setOnClickListener {
            (activity as? AppNavigator)?.navigateBack()
        }
        binding.buttonRegister.setOnClickListener { viewModel.submit() }

        binding.inputFullName.doAfterTextChanged {
            viewModel.onFullNameChanged(it?.toString().orEmpty())
        }
        binding.inputEmail.doAfterTextChanged {
            viewModel.onEmailChanged(it?.toString().orEmpty())
        }
        binding.inputPassword.doAfterTextChanged {
            viewModel.onPasswordChanged(it?.toString().orEmpty())
        }
        binding.inputConfirmPassword.doAfterTextChanged {
            viewModel.onConfirmPasswordChanged(it?.toString().orEmpty())
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.formState.collect { state ->
                        binding.layoutFullName.error = state.fullNameError
                        binding.layoutEmail.error = state.emailError
                        binding.layoutPassword.error = state.passwordError
                        binding.layoutConfirmPassword.error = state.confirmPasswordError
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
                                (activity as? AppNavigator)?.openOtpVerification()
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
        binding.buttonRegister.isEnabled = !isLoading
        binding.buttonRegister.alpha = if (isLoading) 0.7f else 1f
    }
}
