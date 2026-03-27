package com.example.flashcardapp.presentation.feature.learning

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.flashcardapp.databinding.FragmentStudyResultBinding

class StudyResultFragment : Fragment() {
    private lateinit var binding: FragmentStudyResultBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStudyResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListener()
    }

    private fun setupClickListener() {
        binding.btnBack.setOnClickListener { requireActivity().finish() }
        binding.btnContinue.setOnClickListener { requireActivity().finish() }
    }
}

