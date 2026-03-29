package com.example.flashcardapp.presentation.feature.auth

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentResetPasswordBinding
import com.example.flashcardapp.di.AuthModule
import com.example.flashcardapp.presentation.common.dialog.authDialog.LoadingDialogFragment
import com.example.flashcardapp.presentation.common.dialog.authDialog.SuccessDialogFragment
import kotlinx.coroutines.launch

class ResetPasswordFragment : Fragment(R.layout.fragment_reset_password) {

    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ResetPasswordViewModel
    private var emailArg: String? = null
    private var otpArg: String? = null
    private var loadingDialog: LoadingDialogFragment? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentResetPasswordBinding.bind(view)

        emailArg = arguments?.getString(OtpVerificationFragment.ARG_EMAIL)
        otpArg = arguments?.getString(OtpVerificationFragment.ARG_OTP)

        setupViewModel()
        setupListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupViewModel() {
        val useCases = AuthModule.provideAuthUseCases(requireContext())
        viewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(useCases)
        )[ResetPasswordViewModel::class.java]
        viewModel.setContext(emailArg, otpArg)
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnCancel.setOnClickListener { findNavController().popBackStack(R.id.loginFragment, false) }
        binding.btnUpdate.setOnClickListener { viewModel.submit() }

        binding.etNewPass.doAfterTextChanged { viewModel.onNewPasswordChanged(it?.toString().orEmpty()) }
        binding.etConfirmPass.doAfterTextChanged { viewModel.onConfirmPasswordChanged(it?.toString().orEmpty()) }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.formState.collect { state ->
                        binding.tilNewPass.error = state.passwordError
                        binding.tilConfirmPass.error = state.confirmPasswordError
                        renderStrength(state.strength)
                    }
                }

                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            AuthOperationState.Idle -> {
                                renderLoading(false)
                                loadingDialog?.dismiss()
                                loadingDialog = null
                            }
                            AuthOperationState.Loading -> {
                                renderLoading(true)
                                if (loadingDialog == null || loadingDialog?.isVisible == false) {
                                    loadingDialog = LoadingDialogFragment.newInstance("Đang đặt lại mật khẩu...")
                                    loadingDialog?.show(childFragmentManager, "LoadingDialog")
                                }
                            }
                            is AuthOperationState.Success -> {
                                renderLoading(false)
                                loadingDialog?.dismiss()
                                loadingDialog = null
                                viewModel.resetUiState()
                                showSuccessDialog()
                            }
                            is AuthOperationState.Error -> {
                                renderLoading(false)
                                loadingDialog?.dismiss()
                                loadingDialog = null
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
        binding.btnUpdate.isEnabled = !isLoading
        binding.btnUpdate.alpha = if (isLoading) 0.7f else 1f
    }

    private fun renderStrength(strength: PasswordStrength) {
        val inactiveColor = Color.parseColor("#E5E9F1")
        val activeColorRes = when (strength) {
            PasswordStrength.EMPTY -> R.color.md_icon_gray
            PasswordStrength.WEAK -> R.color.md_icon_red
            PasswordStrength.FAIR -> R.color.md_icon_orange
            PasswordStrength.GOOD -> R.color.md_icon_green
            PasswordStrength.STRONG -> R.color.md_icon_blue
        }

        val activeColor = ContextCompat.getColor(requireContext(), activeColorRes)
        val labelColor = if (strength == PasswordStrength.EMPTY)
            ContextCompat.getColor(requireContext(), R.color.md_sub_title) else activeColor

        binding.tvStrengthLabel.text = strength.label
        binding.tvStrengthLabel.setTextColor(labelColor)

        listOf(binding.seg1, binding.seg2, binding.seg3, binding.seg4)
            .forEachIndexed { index, view ->
                val color = if (index < strength.activeSegments) activeColor else inactiveColor
                view.setBackgroundColor(color)
            }
    }

    private fun showSuccessDialog() {
        val dialog = SuccessDialogFragment.newInstance(
            title = getString(R.string.reset_password_success_title),
            message = getString(R.string.reset_password_success_message),
            buttonText = getString(R.string.app_login)
        )
        dialog.setOnContinueClickListener {
            findNavController().navigate(R.id.action_resetPasswordFragment_to_loginFragment)
        }
        dialog.show(childFragmentManager, "SuccessDialog")
    }
}