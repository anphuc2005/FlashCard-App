package com.example.flashcardapp.ui.module_learn_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentDeckDetailBinding

class DeckDetailFragment : Fragment() {
    private lateinit var binding: FragmentDeckDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDeckDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListener()
    }

    private fun setupClickListener() {
        binding.btnBack.setOnClickListener {
            requireActivity().finish()
        }

        binding.btnMenu.setOnClickListener {
            // TODO: Show menu options
        }

        binding.ctaButton.setOnClickListener {
            findNavController().navigate(R.id.action_deckDetailFragment_to_sessionSettingFragment)
        }

        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        // Show all cards
                    }
                    1 -> {
                        // Show marked cards
                    }
                    2 -> {
                        // Show mastery cards
                    }
                }
            }

            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }
}