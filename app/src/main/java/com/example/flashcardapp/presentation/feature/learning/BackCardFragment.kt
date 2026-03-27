package com.example.flashcardapp.presentation.feature.learning

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentBackCardBinding

class BackCardFragment : Fragment() {
    private lateinit var binding: FragmentBackCardBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBackCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListener()
    }

    private fun setupClickListener() {
        binding.btnClose.setOnClickListener {
            requireActivity().finish()
        }

        binding.cardAgain.setOnClickListener { navigateToNextCard() }
        binding.cardHard.setOnClickListener { navigateToNextCard() }
        binding.cardGood.setOnClickListener { navigateToNextCard() }
        binding.cardEasy.setOnClickListener { navigateToNextCard() }
    }

    private fun navigateToNextCard() {
        findNavController().navigate(R.id.action_backCardFragment_to_studyResultFragment)
    }
}

