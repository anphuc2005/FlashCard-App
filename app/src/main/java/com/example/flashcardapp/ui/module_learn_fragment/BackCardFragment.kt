package com.example.flashcardapp.ui.module_learn_fragment

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

        // Feedback buttons
        binding.cardAgain.setOnClickListener {
            // Save feedback as "Again" and move to next card or result
            navigateToNextCard()
        }

        binding.cardHard.setOnClickListener {
            // Save feedback as "Hard" and move to next card or result
            navigateToNextCard()
        }

        binding.cardGood.setOnClickListener {
            // Save feedback as "Good" and move to next card or result
            navigateToNextCard()
        }

        binding.cardEasy.setOnClickListener {
            // Save feedback as "Easy" and move to next card or result
            navigateToNextCard()
        }
    }

    private fun navigateToNextCard() {
        // Check if more cards exist
        // If yes, navigate back to FrontCardFragment with next card
        // If no, navigate to StudyResultFragment
        findNavController().navigate(R.id.action_backCardFragment_to_studyResultFragment)
    }
}