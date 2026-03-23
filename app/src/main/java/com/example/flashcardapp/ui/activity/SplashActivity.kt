package com.example.flashcardapp.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flashcardapp.AppSessionManager
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.ActivitySplashBinding
import com.example.flashcardapp.ui.feature.splash.SplashViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Splash mở app, đọc session và route sang onboarding, login hoặc main.
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var viewModel: SplashViewModel
    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Tạo ViewModel ngay tại Activity để bớt một lớp trung gian.
        viewModel = ViewModelProvider(
            this,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(SplashViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return SplashViewModel(AppSessionManager(applicationContext)) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
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
                                // Route sang onboarding nếu người dùng chưa xem intro.
                                startActivity(
                                    AuthActivity.createIntent(
                                        context = this@SplashActivity,
                                        startDestination = AuthActivity.START_DESTINATION_ONBOARDING
                                    ).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                                )
                                finish()
                            }
                        }
                        is SplashViewModel.SplashState.NavigateToLogin -> {
                            if (!hasNavigated) {
                                hasNavigated = true
                                delay(NAVIGATION_DELAY_MS)
                                // Route sang login nếu chưa có phiên đăng nhập.
                                startActivity(
                                    AuthActivity.createIntent(
                                        context = this@SplashActivity,
                                        startDestination = AuthActivity.START_DESTINATION_LOGIN
                                    ).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                                )
                                finish()
                            }
                        }
                        is SplashViewModel.SplashState.NavigateToHome -> {
                            if (!hasNavigated) {
                                hasNavigated = true
                                delay(NAVIGATION_DELAY_MS)
                                // Vào thẳng app chính nếu đã có session hợp lệ.
                                startActivity(
                                    Intent(this@SplashActivity, MainActivity::class.java).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    }
                                )
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
