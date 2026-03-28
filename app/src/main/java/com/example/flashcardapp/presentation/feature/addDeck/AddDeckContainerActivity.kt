package com.example.flashcardapp.presentation.feature.addDeck

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.ActivityAddDeckContainerBinding

class AddDeckContainerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddDeckContainerBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDeckContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_add_deck) as NavHostFragment
        navController = navHostFragment.navController

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}

