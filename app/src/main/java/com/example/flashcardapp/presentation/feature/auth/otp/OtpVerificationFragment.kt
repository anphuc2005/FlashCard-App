package com.example.flashcardapp.presentation.feature.auth.otp

import com.example.flashcardapp.presentation.feature.auth.*
import com.example.flashcardapp.presentation.feature.auth.AuthViewModelFactory
import com.example.flashcardapp.presentation.feature.auth.PasswordToggleConfigurator

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentOtpVerificationBinding
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.presentation.common.dialog.authDialog.LoadingDialogFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class OtpVerificationFragment : Fragment(R.layout.fragment_otp_verification) {

    private var _binding: FragmentOtpVerificationBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: OtpVerificationViewModel
    private var emailArg: String? = null
    private var loadingDialog: LoadingDialogFragment? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOtpVerificationBinding.bind(view)

        emailArg = arguments?.getString(ARG_EMAIL)

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

    private fun setupListeners() {
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.buttonConfirmOtp.setOnClickListener {
            verifyOtp()
        }
        setupOtpInputs()
    }

    private fun setupOtpInputs() {
        val otpInputs = listOf(
            binding.inputOtp1,
            binding.inputOtp2,
            binding.inputOtp3,
            binding.inputOtp4,
            binding.inputOtp5,
            binding.inputOtp6
        )

        for (i in otpInputs.indices) {
            val currentInput = otpInputs[i]
            val nextInput = if (i < otpInputs.size - 1) otpInputs[i + 1] else null
            val prevInput = if (i > 0) otpInputs[i - 1] else null

            currentInput.addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (s?.length == 1) {
                        nextInput?.requestFocus()
                    }
                }
                override fun afterTextChanged(s: android.text.Editable?) {}
            })

            currentInput.setOnKeyListener { _, keyCode, event ->
                if (event.action == android.view.KeyEvent.ACTION_DOWN && keyCode == android.view.KeyEvent.KEYCODE_DEL) {
                    if (currentInput.text.isNullOrEmpty()) {
                        prevInput?.text?.clear()
                        prevInput?.requestFocus()
                    }
                }
                false
            }
        }
    }

    private fun verifyOtp() {
        val otp = collectedOtp()
        Log.d("OtpVerificationFragment", "Submitting OTP email=${emailArg ?: "<null>"} otp=$otp")
        if (otp.length < 6) {
            Toast.makeText(requireContext(), "Please enter OTP", Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.verify(otp)
    }

    private fun setupViewModel() {
        val appContainer = (requireActivity().application as FlashcardApp).container
        val useCases = appContainer.authUseCases
        viewModel = ViewModelProvider(
            this,
            AuthViewModelFactory(useCases)
        )[OtpVerificationViewModel::class.java]
        viewModel.setContext(emailArg)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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
                                loadingDialog = LoadingDialogFragment.newInstance("Đang xác minh mã OTP...")
                                loadingDialog?.show(childFragmentManager, "LoadingDialog")
                            }
                        }
                        is AuthOperationState.Success -> {
                            renderLoading(false)
                            loadingDialog?.dismiss()
                            loadingDialog = null
                            viewModel.resetState()
                            val otpValue = collectedOtp()
                            Log.d("OtpVerificationFragment", "OTP verified email=${state.email ?: emailArg} otp=$otpValue")
                            val args = Bundle().apply {
                                putString(ARG_EMAIL, state.email)
                                putString(ARG_OTP, otpValue)
                            }
                            findNavController().navigate(R.id.action_otpVerificationFragment_to_resetPasswordFragment, args)
                        }

                        is AuthOperationState.Error -> {
                            renderLoading(false)
                            loadingDialog?.dismiss()
                            loadingDialog = null
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            Log.e("OtpVerificationFragment", "Error: ${state.message}")
                            viewModel.resetState()
                        }
                    }
                }
            }
        }
    }

    private fun collectedOtp(): String =
        binding.inputOtp1.text.toString() +
                binding.inputOtp2.text.toString() +
                binding.inputOtp3.text.toString() +
                binding.inputOtp4.text.toString() +
                binding.inputOtp5.text.toString() +
                binding.inputOtp6.text.toString()


    private fun renderLoading(isLoading: Boolean) {
        binding.buttonConfirmOtp.isEnabled = !isLoading
        binding.buttonConfirmOtp.alpha = if (isLoading) 0.7f else 1f
    }

    private fun hideMainChrome() {
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)?.visibility = View.GONE
        requireActivity().findViewById<FloatingActionButton>(R.id.fabChat)?.visibility = View.GONE
    }

    companion object {
        const val ARG_EMAIL = "arg_email"
        const val ARG_OTP = "arg_otp"
    }
}
