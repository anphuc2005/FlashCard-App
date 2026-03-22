package com.example.flashcardapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.flashcardapp.databinding.ActivityMainBinding
import com.example.flashcardapp.ui.feature.auth.presentation.ForgotPasswordFragment
import com.example.flashcardapp.ui.feature.auth.presentation.LoginFragment
import com.example.flashcardapp.ui.feature.auth.presentation.OtpVerificationFragment
import com.example.flashcardapp.ui.feature.auth.presentation.RegisterFragment
import com.example.flashcardapp.ui.feature.onboarding.OnboardingFragment

class MainActivity : AppCompatActivity(), AppNavigator {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sessionManager: AppSessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        sessionManager = AppSessionManager(this)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (savedInstanceState == null) {
            routeFromAppStart()
        } else {
            restoreScreenState()
        }
    }

    override fun openLogin(clearBackStack: Boolean) {
        showAuthScreen(
            fragment = LoginFragment(),
            tag = LOGIN_TAG,
            addToBackStack = false,
            clearBackStack = clearBackStack
        )
    }

    override fun openRegister() {
        showAuthScreen(
            fragment = RegisterFragment(),
            tag = REGISTER_TAG,
            addToBackStack = true,
            clearBackStack = false
        )
    }

    override fun openForgotPassword() {
        showAuthScreen(
            fragment = ForgotPasswordFragment(),
            tag = FORGOT_PASSWORD_TAG,
            addToBackStack = true,
            clearBackStack = false
        )
    }

    override fun openOtpVerification() {
        showAuthScreen(
            fragment = OtpVerificationFragment(),
            tag = OTP_TAG,
            addToBackStack = true,
            clearBackStack = false
        )
    }

    override fun completeOnboarding() {
        sessionManager.markOnboardingCompleted()
        openLogin(clearBackStack = true)
    }

    override fun completeLogin(accessToken: String?) {
        sessionManager.saveLoginSession(accessToken)
        showMainScreen()
    }

    override fun navigateBack() {
        onBackPressedDispatcher.onBackPressed()
    }

    private fun routeFromAppStart() {
        when {
            !sessionManager.hasOnboarded -> {
                showAuthScreen(
                    fragment = OnboardingFragment(),
                    tag = ONBOARDING_TAG,
                    addToBackStack = false,
                    clearBackStack = true
                )
            }

            sessionManager.isLoggedIn -> showMainScreen()

            else -> openLogin(clearBackStack = true)
        }
    }

    private fun restoreScreenState() {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.nav_host)
        if (currentFragment is NavHostFragment) {
            attachMainNavigation(currentFragment)
            showMainChrome(true)
        } else {
            showMainChrome(false)
        }
    }

    private fun showAuthScreen(
        fragment: Fragment,
        tag: String,
        addToBackStack: Boolean,
        clearBackStack: Boolean
    ) {
        if (clearBackStack) {
            clearAuthBackStack()
        }

        showMainChrome(false)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.nav_host, fragment, tag)
            if (addToBackStack) {
                addToBackStack(tag)
            }
        }
    }

    private fun showMainScreen() {
        clearAuthBackStack()

        val navHostFragment = NavHostFragment.create(R.navigation.nav_graph)
        supportFragmentManager.commit {
            setReorderingAllowed(true)
            replace(R.id.nav_host, navHostFragment, MAIN_NAV_HOST_TAG)
            setPrimaryNavigationFragment(navHostFragment)
        }
        supportFragmentManager.executePendingTransactions()

        attachMainNavigation(navHostFragment)
        showMainChrome(true)
    }

    private fun attachMainNavigation(navHostFragment: NavHostFragment) {
        binding.bottomNav.setupWithNavController(navHostFragment.navController)
    }

    private fun showMainChrome(isVisible: Boolean) {
        binding.bottomNav.isVisible = isVisible
        binding.fabChat.isVisible = isVisible
    }

    private fun clearAuthBackStack() {
        supportFragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    private companion object {
        const val MAIN_NAV_HOST_TAG = "main_nav_host"
        const val ONBOARDING_TAG = "onboarding"
        const val LOGIN_TAG = "login"
        const val REGISTER_TAG = "register"
        const val FORGOT_PASSWORD_TAG = "forgot_password"
        const val OTP_TAG = "otp_verification"
    }
}
