package com.example.flashcardapp.presentation.feature.auth.register

import com.example.flashcardapp.presentation.feature.auth.*
import com.example.flashcardapp.presentation.feature.auth.AuthViewModelFactory
import com.example.flashcardapp.presentation.feature.auth.PasswordToggleConfigurator

import android.graphics.Color
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentRegisterBinding
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.presentation.common.dialog.authDialog.LoadingDialogFragment
import com.example.flashcardapp.presentation.common.notification.showAppError
import com.example.flashcardapp.presentation.main.DocumentViewerActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: RegisterViewModel
    private var loadingDialog: LoadingDialogFragment? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterBinding.bind(view)

        setupViewModel()
        setupPasswordToggle()
        setupTermsText()
        setupListeners()
        observeViewModel()
    }

    private fun setupPasswordToggle() {
        PasswordToggleConfigurator.setup(binding.layoutPassword, binding.inputPassword)
        PasswordToggleConfigurator.setup(binding.layoutConfirmPassword, binding.inputConfirmPassword)
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
        )[RegisterViewModel::class.java]
    }

    private fun setupListeners() {
        binding.buttonBack.setOnClickListener { findNavController().popBackStack() }
        binding.textLoginNow.setOnClickListener {
            findNavController().popBackStack()
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

    private fun setupTermsText() {
        val prefix = getString(R.string.register_terms_prefix).trim()
        val terms = getString(R.string.register_terms_service)
        val and = getString(R.string.register_terms_and).trim()
        val privacy = getString(R.string.register_terms_privacy)
        val suffix = getString(R.string.register_terms_suffix).trimStart()
        val builder = SpannableStringBuilder()

        builder.append(prefix)
        builder.append(" ")

        val termsStart = builder.length
        builder.append(terms)

        builder.append(" ")
        builder.append(and)
        builder.append(" ")

        val privacyStart = builder.length
        builder.append(privacy)
        builder.append(" ")
        builder.append(suffix)

        val spannable = SpannableString(builder)

        spannable.setSpan(
            buildDocumentSpan(
                title = getString(R.string.document_terms_title),
                assetPath = TERMS_ASSET_PATH
            ),
            termsStart,
            termsStart + terms.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannable.setSpan(
            buildDocumentSpan(
                title = getString(R.string.document_privacy_title),
                assetPath = PRIVACY_ASSET_PATH
            ),
            privacyStart,
            privacyStart + privacy.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.textRegisterTerms.text = spannable
        binding.textRegisterTerms.movementMethod = LinkMovementMethod.getInstance()
        binding.textRegisterTerms.highlightColor = Color.TRANSPARENT
    }

    private fun buildDocumentSpan(title: String, assetPath: String): ClickableSpan {
        return object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(
                    DocumentViewerActivity.createIntent(
                        context = requireContext(),
                        title = title,
                        assetPath = assetPath
                    )
                )
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.color = ContextCompat.getColor(requireContext(), R.color.md_button)
                ds.isUnderlineText = false
            }
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
                            AuthOperationState.Idle -> {
                                renderLoading(false)
                                loadingDialog?.dismiss()
                                loadingDialog = null
                            }
                            AuthOperationState.Loading -> {
                                renderLoading(true)
                                if (loadingDialog == null || loadingDialog?.isVisible == false) {
                                    loadingDialog = LoadingDialogFragment.newInstance("Đang tạo tài khoản...")
                                    loadingDialog?.show(childFragmentManager, "LoadingDialog")
                                }
                            }
                            is AuthOperationState.Success -> {
                                renderLoading(false)
                                loadingDialog?.dismiss()
                                loadingDialog = null
                                viewModel.resetUiState()
                                findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
                            }

                            is AuthOperationState.Error -> {
                                renderLoading(false)
                                loadingDialog?.dismiss()
                                loadingDialog = null
                                showAppError(state.message)
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

    private fun hideMainChrome() {
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)?.visibility = View.GONE
        requireActivity().findViewById<FloatingActionButton>(R.id.fabChat)?.visibility = View.GONE
    }


    private companion object {
        const val TERMS_ASSET_PATH = "docs/terms_of_service.html"
        const val PRIVACY_ASSET_PATH = "docs/privacy_policy.html"
    }
}

