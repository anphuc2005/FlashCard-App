package com.example.flashcardapp.ui.feature.launch

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.flashcardapp.AppSessionManager
import com.example.flashcardapp.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class LaunchRouterFragment : Fragment(R.layout.fragment_launch_router) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideMainChrome()

        if (savedInstanceState == null) {
            val destination = when {
                !AppSessionManager(requireContext()).hasOnboarded -> R.id.action_launchRouterFragment_to_onboardingFragment
                AppSessionManager(requireContext()).isLoggedIn -> R.id.action_launchRouterFragment_to_homeFragment
                else -> R.id.action_launchRouterFragment_to_loginFragment
            }
            findNavController().navigate(destination)
        }
    }

    private fun hideMainChrome() {
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)?.visibility = View.GONE
        requireActivity().findViewById<FloatingActionButton>(R.id.fabChat)?.visibility = View.GONE
    }
}
