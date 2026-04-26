package com.example.flashcardapp.presentation.feature.learning

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.ActivityLearningBinding
import com.example.flashcardapp.presentation.common.notification.showAppWarning

const val EXTRA_DECK_ID = "DECK_ID"
private const val TAG = "LearningActivity"

class LearningActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLearningBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLearningBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyWindowInsets()
        setupLearningGraph()
    }

    private fun applyWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupLearningGraph() {
        val deckId = intent.getStringExtra(EXTRA_DECK_ID)
        if (deckId.isNullOrBlank()) {
            Log.w(TAG, "setupLearningGraph aborted: missing deckId in intent")
            showAppWarning(getString(R.string.learning_missing_deck))
            finish()
            return
        }
        Log.d(TAG, "setupLearningGraph: deckId=$deckId")

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_learning_host) as NavHostFragment
        val navController = navHostFragment.navController
        val navGraph = navController.navInflater.inflate(R.navigation.nav_learning)
        navController.setGraph(
            navGraph,
            Bundle().apply { putString(EXTRA_DECK_ID, deckId) }
        )
    }
}
