package com.example.flashcardapp.presentation.feature.aiChat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.databinding.FragmentChatAIBinding
import com.example.flashcardapp.di.ChatModule
import com.example.flashcardapp.presentation.common.adapter.ChatMessageAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatAIFragment : Fragment() {

    private var _binding: FragmentChatAIBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChatAIViewModel
    private lateinit var adapter: ChatMessageAdapter
    private val voicePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            launchSpeechToText()
        } else {
            showError("Bạn cần cấp quyền micro để dùng nhập bằng giọng nói.")
        }
    }

    private val speechToTextLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK || result.data == null) return@registerForActivityResult
        val spokenText = result.data
            ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            ?.firstOrNull()
            ?.trim()
            .orEmpty()
        if (spokenText.isNotEmpty()) {
            binding.etMessage.setText(spokenText)
            binding.etMessage.setSelection(spokenText.length)
        }
    }

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
        val container = (requireActivity().application as FlashcardApp).container
        val useCases = ChatModule.provideChatUseCases(requireContext())
        val exploreDecksUseCase = container.exploreDecksUseCase
        val addDeckUseCase = container.addDeckUseCase
        val addFlashCardUseCase = container.addFlashCardUseCase
        val getAllCategoriesUseCase = container.getAllCategoriesUseCase

        viewModel = ViewModelProvider(
            this,
            ChatAIViewModelFactory(
                useCases = useCases,
                exploreDecksUseCase = exploreDecksUseCase,
                addDeckUseCase = addDeckUseCase,
                addFlashCardUseCase = addFlashCardUseCase,
                getAllCategoriesUseCase = getAllCategoriesUseCase
            )
        )[ChatAIViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = ChatMessageAdapter(
            onConfirmDeckCreation = { viewModel.confirmPendingDeckCreationFromUi() },
            onCancelDeckCreation = { viewModel.cancelPendingDeckCreationFromUi() }
        )

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

        binding.btnMic.setOnClickListener {
            handleVoiceInputClick()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    viewModel.messages.collectLatest { messages ->
                        adapter.submitList(messages)
                        if (messages.isNotEmpty()) {
                            binding.recyclerViewChat.scrollToPosition(messages.size - 1)
                        }
                    }
                }

                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        binding.etMessage.isEnabled = !isLoading
                        binding.btnSend.isEnabled = !isLoading
                    }
                }

                launch {
                    viewModel.error.collectLatest { error ->
                        if (error != null) {
                            showError(error)
                        }
                    }
                }

                launch {
                    viewModel.pendingDeckDraftMessageId.collectLatest { pendingMessageId ->
                        adapter.setPendingDeckDraftMessageId(pendingMessageId)
                    }
                }
            }
        }
    }

    private fun showError(error: String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
    }

    private fun handleVoiceInputClick() {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchSpeechToText()
        } else {
            voicePermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    private fun launchSpeechToText() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói nội dung bạn muốn gửi")
        }
        runCatching {
            speechToTextLauncher.launch(intent)
        }.onFailure {
            showError("Thiết bị chưa hỗ trợ nhập giọng nói.")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
