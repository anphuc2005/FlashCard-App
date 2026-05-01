package com.example.flashcardapp.presentation.feature.addDeck

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.ActivityAddDeckContainerBinding

class AddDeckContainerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddDeckContainerBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAddDeckContainerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_add_deck) as NavHostFragment
        navController = navHostFragment.navController

        // Check if we need to start with Edit Deck
        val isEditMode = intent.getBooleanExtra("IS_EDIT_MODE", false)
        val deckId = intent.getStringExtra("DECK_ID")

        val navGraph = navController.navInflater.inflate(R.navigation.nav_add_deck)
        if (isEditMode) {
            navGraph.setStartDestination(R.id.editDeckFragment)
        } else {
            navGraph.setStartDestination(R.id.addDeckFragment)
        }

        // Pass arguments
        val startArgs = Bundle().apply {
            if (isEditMode && deckId != null) putString("DECK_ID", deckId)
        }
        navController.setGraph(navGraph, startArgs)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
