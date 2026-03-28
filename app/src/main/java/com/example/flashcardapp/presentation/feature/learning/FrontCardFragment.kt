package com.example.flashcardapp.presentation.feature.learning

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentFrontCardBinding

class FrontCardFragment : Fragment() {
    private lateinit var binding: FragmentFrontCardBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFrontCardBinding.inflate(inflater, container, false)
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

        binding.btnFlip.setOnClickListener {
            findNavController().navigate(R.id.action_frontCardFragment_to_backCardFragment)
        }

        binding.cardFront.setOnClickListener {
            findNavController().navigate(R.id.action_frontCardFragment_to_backCardFragment)
        }
    }
}

