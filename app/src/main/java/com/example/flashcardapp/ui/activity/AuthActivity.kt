package com.example.flashcardapp.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.ActivityAuthBinding

// AuthActivity host flow onboarding và đăng nhập.
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.auth_nav_host) as NavHostFragment
        val navController = navHostFragment.navController
        if (savedInstanceState == null) {
            // Mở đúng màn đầu theo route được SplashActivity truyền vào.
            val navGraph = navController.navInflater.inflate(R.navigation.auth_nav_graph).apply {
                setStartDestination(resolveStartDestination())
            }
            navController.graph = navGraph
        }
    }

    private fun resolveStartDestination(): Int {
        return when (intent.getStringExtra(EXTRA_START_DESTINATION)) {
            START_DESTINATION_ONBOARDING -> R.id.onboardingFragment
            else -> R.id.loginFragment
        }
    }

    companion object {
        private const val EXTRA_START_DESTINATION = "extra_start_destination"
        const val START_DESTINATION_ONBOARDING = "onboarding"
        const val START_DESTINATION_LOGIN = "login"

        // Tạo intent để AuthActivity biết cần mở onboarding hay login.
        fun createIntent(
            context: Context,
            startDestination: String,
        ): Intent {
            return Intent(context, AuthActivity::class.java).apply {
                putExtra(EXTRA_START_DESTINATION, startDestination)
            }
        }
    }
}
