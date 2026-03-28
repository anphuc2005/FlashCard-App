package com.example.flashcardapp.presentation.feature.aiChat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flashcardapp.di.ChatModule
import com.example.flashcardapp.domain.usecase.chat.ChatUseCases
import com.example.flashcardapp.databinding.FragmentChatAIBinding
import com.example.flashcardapp.presentation.common.adapter.ChatMessageAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.collections.get

class ChatAIFragment : Fragment() {

    private var _binding: FragmentChatAIBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChatAIViewModel
    private lateinit var adapter: ChatMessageAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatAIBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupViewModel() {
        val useCases = ChatModule.provideChatUseCases(requireContext())
        viewModel = ViewModelProvider(
            this,
            ChatAIViewModelFactory(useCases)
        )[ChatAIViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = ChatMessageAdapter()

        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }
            adapter = this@ChatAIFragment.adapter
        }
    }

    private fun setupListeners() {
        binding.btnSend.setOnClickListener {
            val userText = binding.etMessage.text.toString().trim()
            if (userText.isNotEmpty()) {
                viewModel.sendMessage(userText)
                binding.etMessage.text?.clear()
            }
        }

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnAttach.setOnClickListener {
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.messages.collectLatest { messages ->
                adapter.submitList(messages)
                if (messages.isNotEmpty()) {
                    binding.recyclerViewChat.scrollToPosition(messages.size - 1)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.etMessage.isEnabled = !isLoading
                binding.btnSend.isEnabled = !isLoading
            }
        }

        lifecycleScope.launch {
            viewModel.error.collectLatest { error ->
                if (error != null) {
                    showError(error)
                }
            }
        }
    }

    private fun showError(error: String) {
        // TODO: add Snackbar/Toast if needed
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance() = ChatAIFragment()
    }
}

class ChatAIViewModelFactory(
    private val useCases: ChatUseCases
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatAIViewModel(useCases) as T
    }
}