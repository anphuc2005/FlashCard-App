package com.example.flashcardapp.presentation.feature.auth.forgotpassword

import com.example.flashcardapp.presentation.feature.auth.*
import com.example.flashcardapp.presentation.feature.auth.AuthViewModelFactory
import com.example.flashcardapp.presentation.feature.auth.PasswordToggleConfigurator
import com.example.flashcardapp.presentation.feature.auth.otp.OtpVerificationFragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentForgotPasswordBinding
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.presentation.common.dialog.authDialog.CheckEmailDialogFragment
import com.example.flashcardapp.presentation.common.dialog.authDialog.LoadingDialogFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class ForgotPasswordFragment : Fragment(R.layout.fragment_forgot_password) {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ForgotPasswordViewModel
    private var loadingDialog: LoadingDialogFragment? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentForgotPasswordBinding.bind(view)

        setupViewModel()
        setupListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        hideMainChrome()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupViewModel() {
        val appContainer = (requireActivity().application as FlashcardApp).container
        val useCases = appContainer.authUseCases
        viewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(useCases)
        )[ForgotPasswordViewModel::class.java]
    }

    private fun setupListeners() {
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.buttonSendVerificationCode.setOnClickListener {
            viewModel.submit()
        }
        binding.inputEmail.doAfterTextChanged {
            viewModel.onEmailChanged(it?.toString().orEmpty())
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.formState.collect { state ->
                        binding.layoutEmail.error = state.emailError
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
                                    loadingDialog = LoadingDialogFragment.newInstance("Đang gửi mã xác minh...")
                                    loadingDialog?.show(childFragmentManager, "LoadingDialog")
                                }
                            }
                            is AuthOperationState.Success -> {
                                renderLoading(false)
                                loadingDialog?.dismiss()
                                loadingDialog = null
                                showCheckEmailDialog(state.email)
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
        binding.buttonSendVerificationCode.isEnabled = !isLoading
        binding.buttonSendVerificationCode.alpha = if (isLoading) 0.7f else 1f
    }

    private fun showCheckEmailDialog(email: String?) {
        val dialog = CheckEmailDialogFragment.newInstance(email)
        dialog.setOnUnderstandClickListener {
            navigateToOtp(email)
            viewModel.resetUiState()
        }
        dialog.setOnResendClickListener {
            viewModel.submit()
        }
        dialog.show(childFragmentManager, "CheckEmailDialog")
    }

    private fun navigateToOtp(email: String?) {
        if (findNavController().currentDestination?.id == R.id.forgotPasswordFragment) {
            val args = Bundle().apply {
                putString(OtpVerificationFragment.ARG_EMAIL, email)
            }
            findNavController().navigate(
                R.id.action_forgotPasswordFragment_to_otpVerificationFragment,
                args
            )
        }
    }

    private fun hideMainChrome() {
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)?.visibility = View.GONE
        requireActivity().findViewById<FloatingActionButton>(R.id.fabChat)?.visibility = View.GONE
    }
}
