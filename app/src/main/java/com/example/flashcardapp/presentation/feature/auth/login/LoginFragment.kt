package com.example.flashcardapp.presentation.feature.auth.login

import com.example.flashcardapp.presentation.feature.auth.*
import com.example.flashcardapp.presentation.feature.auth.AuthViewModelFactory
import com.example.flashcardapp.presentation.feature.auth.PasswordToggleConfigurator
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.result.contract.ActivityResultContracts

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentLoginBinding
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.presentation.common.dialog.authDialog.LoadingDialogFragment
import com.example.flashcardapp.presentation.common.notification.showAppError
import com.example.flashcardapp.presentation.main.MainActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn.getClient
import com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: LoginViewModel
    private var loadingDialog: LoadingDialogFragment? = null
    private lateinit var googleSignInClient: GoogleSignInClient

    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { idToken ->
                viewModel.googleLogin(idToken)
            } ?: run {
                showAppError("Không lấy được mã xác thực Google")
            }
        } catch (e: ApiException) {
            Log.e("LoginFragment", "Google sign in failed", e)
            val errorMessage = when (e.statusCode) {
                7 -> "Lỗi mạng hoặc chưa cài đặt Google Play Services"
                12501 -> "Bạn đã hủy đăng nhập"
                1033 -> "Lỗi kết nối đến server. Vui lòng thử lại sau"
                else -> "Đăng nhập Google thất bại (Mã: ${e.statusCode})"
            }
            showAppError(errorMessage)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)

        setupViewModel()
        setupPasswordToggle()
        setupGoogleSignIn()
        setupListeners()
        observeViewModel()
    }

    private fun setupPasswordToggle() {
        PasswordToggleConfigurator.setup(binding.layoutPassword, binding.inputPassword)
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = getClient(requireActivity(), gso)
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
        )[LoginViewModel::class.java]
    }

    private fun setupListeners() {
        binding.textRegisterNow.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        binding.textForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
        }
        binding.buttonLogin.setOnClickListener { viewModel.submit() }
        binding.buttonGoogle.setOnClickListener {
            googleSignInLauncher.launch(googleSignInClient.signInIntent)
        }

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
                        val endIconTintColor = if (state.passwordError != null) {
                            ContextCompat.getColor(requireContext(), R.color.md_icon_red)
                        } else {
                            ContextCompat.getColor(requireContext(), R.color.auth_input_hint)
                        }
                        binding.layoutPassword.setEndIconTintList(ColorStateList.valueOf(endIconTintColor))
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
                                    loadingDialog = LoadingDialogFragment.newInstance("Đang đăng nhập...")
                                    loadingDialog?.show(childFragmentManager, "LoadingDialog")
                                }
                            }
                            is AuthOperationState.Success -> {
                                renderLoading(false)
                                loadingDialog?.dismiss()
                                loadingDialog = null
                                viewModel.resetUiState()
                                val intent = Intent(requireContext(), MainActivity::class.java)
                                startActivity(intent)
                                requireActivity().finish()
                            }

                            is AuthOperationState.Error -> {
                                renderLoading(false)
                                loadingDialog?.dismiss()
                                loadingDialog = null
                                val msg = when {
                                    state.message.contains("1033") -> "Lỗi kết nối đến server"
                                    state.message.contains("401") -> "Sai tài khoản hoặc mật khẩu"
                                    else -> state.message
                                }
                                showAppError(msg)
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

    private fun hideMainChrome() {
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)?.visibility = View.GONE
        requireActivity().findViewById<FloatingActionButton>(R.id.fabChat)?.visibility = View.GONE
    }
}
