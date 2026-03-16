package com.example.flashcardapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.flashcardapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_FlashCardApp)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Kết nối Bottom Nav với Navigation
        val navHostFragment =
            supportFragmentManager.findFragmentById(binding.navHost.id) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav: BottomNavigationView = binding.bottomNav

        bottomNav.setupWithNavController(navController)
        redirectIfOnboardingAlreadyCompleted(navController)

        // Chỉ hiện Bottom Nav ở các màn chính
        navController.addOnDestinationChangedListener { _, destination, _ ->
            bottomNav.isVisible = destination.id in setOf(
                R.id.homeFragment,
                R.id.deckListFragment,
                R.id.exploreFragment,
                R.id.statsFragment,
                R.id.profileFragment
            )
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun redirectIfOnboardingAlreadyCompleted(navController: NavController) {
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val onboardingCompleted = prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false)
        if (onboardingCompleted && navController.currentDestination?.id == R.id.onboardingFragment) {
            navController.navigate(R.id.action_onboardingFragment_to_loginFragment)
        }
    }

    private companion object {
        const val PREFS_NAME = "flashcard_prefs"
        const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
    }
}
