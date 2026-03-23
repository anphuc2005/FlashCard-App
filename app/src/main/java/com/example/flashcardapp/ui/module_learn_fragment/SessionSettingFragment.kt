package com.example.flashcardapp.ui.module_learn_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentSessionSettingBinding

class SessionSettingFragment : Fragment() {
    private lateinit var binding: FragmentSessionSettingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSessionSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListener()
    }

    private fun setupClickListener() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        // Radio button selection for order
        binding.rbRandom.setOnClickListener {
            // Select random order
        }

        binding.rbOrdered.setOnClickListener {
            // Select ordered
        }

        // Chip selection for card type
        binding.chipAll.setOnClickListener {
            // Select all cards
        }

        binding.chipNew.setOnClickListener {
            // Select new cards only
        }

        binding.chipReview.setOnClickListener {
            // Select review cards only
        }

        // Start button
        binding.btnStart.setOnClickListener {
            // Navigate to FrontCardFragment to start learning
            findNavController().navigate(R.id.action_sessionSettingFragment_to_frontCardFragment)
        }
    }
}