package com.example.flashcardapp.presentation.feature.learning

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
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }

        binding.rbRandom.setOnClickListener { /* random order */ }
        binding.rbOrdered.setOnClickListener { /* ordered */ }

        binding.chipAll.setOnClickListener { /* all cards */ }
        binding.chipNew.setOnClickListener { /* new cards */ }
        binding.chipReview.setOnClickListener { /* review only */ }

        binding.btnStart.setOnClickListener {
            findNavController().navigate(R.id.action_sessionSettingFragment_to_frontCardFragment)
        }
    }
}

