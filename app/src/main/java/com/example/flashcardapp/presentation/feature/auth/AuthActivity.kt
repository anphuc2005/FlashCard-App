package com.example.flashcardapp.presentation.feature.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.ActivityAuthBinding

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.auth_nav_host) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.auth_nav_graph)

        val openLogin = intent.getBooleanExtra("OPEN_LOGIN", false)
        if (openLogin) {
            navGraph.setStartDestination(R.id.loginFragment)
        } else {
            navGraph.setStartDestination(R.id.onboardingFragment)
        }
        navController.graph = navGraph
    }
}
