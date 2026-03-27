package com.example.flashcardapp.presentation.feature.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentOtpVerificationBinding
import com.example.flashcardapp.presentation.feature.learning.LearningActivity
import com.example.flashcardapp.presentation.main.MainActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class OtpVerificationFragment : Fragment(R.layout.fragment_otp_verification) {

    private var _binding: FragmentOtpVerificationBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentOtpVerificationBinding.bind(view)

        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        hideMainChrome()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupListeners() {
        binding.buttonBack.setOnClickListener {
            findNavController().popBackStack()
        }
        binding.buttonConfirmOtp.setOnClickListener {
            verifyOtp()
        }
    }

    private fun verifyOtp() {
        val otp = binding.inputOtp1.text.toString() + binding.inputOtp2.text.toString() +
                binding.inputOtp3.text.toString() + binding.inputOtp4.text.toString()
        if (otp.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter OTP", Toast.LENGTH_SHORT).show()
            return
        }
        // TODO: Implement OTP verification
//        findNavController().navigate(R.id.action_otpVerificationFragment_to_homeFragment)
        val intent = Intent(requireActivity(), MainActivity::class.java)
        requireActivity().startActivity(intent)
    }

    private fun hideMainChrome() {
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav)?.visibility = View.GONE
        requireActivity().findViewById<FloatingActionButton>(R.id.fabChat)?.visibility = View.GONE
    }
}

