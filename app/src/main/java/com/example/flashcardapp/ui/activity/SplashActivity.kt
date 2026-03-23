package com.example.flashcardapp.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.ActivitySplashBinding
import com.example.flashcardapp.ui.feature.splash.SplashViewModel
import com.example.flashcardapp.ui.feature.splash.SplashViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var viewModel: SplashViewModel
    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize ViewModel with Application
        viewModel = ViewModelProvider(
            this,
            SplashViewModelFactory(application)
        ).get(SplashViewModel::class.java)

        observeSplashState()
    }

    private fun observeSplashState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.splashState.collect { state ->
                    when (state) {
                        is SplashViewModel.SplashState.Loading -> {
                            renderProgress(state.progress)
                        }
                        is SplashViewModel.SplashState.NavigateToOnBoarding -> {
                            if (!hasNavigated) {
                                hasNavigated = true
                                delay(NAVIGATION_DELAY_MS)
                                // Route to AuthActivity (onboarding flow)
                                startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
                                finish()
                            }
                        }
                        is SplashViewModel.SplashState.NavigateToLogin -> {
                            if (!hasNavigated) {
                                hasNavigated = true
                                delay(NAVIGATION_DELAY_MS)
                                // Route to AuthActivity (login flow)
                                startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
                                finish()
                            }
                        }
                        is SplashViewModel.SplashState.NavigateToHome -> {
                            if (!hasNavigated) {
                                hasNavigated = true
                                delay(NAVIGATION_DELAY_MS)
                                // Route to MainActivity (home flow)
                                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                finish()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun renderProgress(progress: Int) {
        binding.textProgress.text = getString(R.string.splash_progress_format, progress)
        binding.progressLoading.progress = progress
    }

    private companion object {
        const val NAVIGATION_DELAY_MS = 180L
    }
}