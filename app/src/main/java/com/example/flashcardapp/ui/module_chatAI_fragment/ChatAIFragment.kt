package com.example.flashcardapp.ui.module_chatAI_fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flashcardapp.BuildConfig
import com.example.flashcardapp.databinding.FragmentChatAIBinding
import com.example.flashcardapp.network.RetrofitClient
import com.example.flashcardapp.repository.GroqRepository
import com.example.flashcardapp.ui.adapter.ChatMessageAdapter
import com.example.flashcardapp.viewmodel.ChatAIViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import android.content.Context

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
        val groqRepository = GroqRepository(
            RetrofitClient.groqApiService,
            BuildConfig.GROQ_API_KEY
        )
        viewModel = ViewModelProvider(
            this,
            ChatAIViewModelFactory(groqRepository, requireContext())
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
    private val repository: GroqRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        return ChatAIViewModel(repository, context) as T
    }
}
