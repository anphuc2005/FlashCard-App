package com.example.flashcardapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flashcardapp.databinding.ActivitySplashBinding
import com.example.flashcardapp.ui.feature.splash.SplashViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel: SplashViewModel by viewModels()
    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeSplashState()
    }

    private fun observeSplashState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.progress.collect { progress ->
                    renderProgress(progress)
                    if (progress >= 100 && !hasNavigated) {
                        hasNavigated = true
                        delay(NAVIGATION_DELAY_MS)
                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                        finish()
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
