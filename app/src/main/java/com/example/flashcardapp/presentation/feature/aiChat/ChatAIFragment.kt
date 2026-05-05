package com.example.flashcardapp.presentation.feature.aiChat

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentChatAIBinding
import com.example.flashcardapp.di.ChatModule
import com.example.flashcardapp.presentation.common.adapter.ChatMessageAdapter
import com.example.flashcardapp.presentation.common.dialog.accountDialog.AppConfirmDialog
import com.example.flashcardapp.presentation.feature.aiChat.adapter.ChatSessionAdapter
import com.example.flashcardapp.presentation.feature.aiChat.model.ChatSessionUiModel
import com.example.flashcardapp.presentation.feature.learning.EXTRA_DECK_ID
import com.example.flashcardapp.presentation.feature.learning.LearningActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class ChatAIFragment : Fragment() {

    private var _binding: FragmentChatAIBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChatAIViewModel
    private lateinit var messageAdapter: ChatMessageAdapter
    private lateinit var sessionAdapter: ChatSessionAdapter

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
        setupRecyclerViews()
        setupBackPressHandling()
        setupListeners()
        observeViewModel()
    }

    private fun setupViewModel() {
        val useCases = ChatModule.provideChatUseCases()

        viewModel = ViewModelProvider(
            this,
            ChatAIViewModelFactory(
                useCases = useCases
            )
        )[ChatAIViewModel::class.java]
    }

    private fun setupRecyclerViews() {
        messageAdapter = ChatMessageAdapter(
            onOpenDeck = { deckId ->
                val intent = Intent(requireContext(), LearningActivity::class.java).apply {
                    putExtra(EXTRA_DECK_ID, deckId)
                }
                startActivity(intent)
            }
        )

        binding.recyclerViewChat.apply {
            layoutManager = LinearLayoutManager(context).apply { stackFromEnd = true }
            adapter = messageAdapter
        }

        sessionAdapter = ChatSessionAdapter(
            onSessionClick = { session ->
                viewModel.selectSession(session.id)
                hideHistoryPanel()
            },
            onDeleteClick = { session ->
                showDeleteSessionDialog(session)
            }
        )

        binding.recyclerViewSessions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sessionAdapter
        }
    }

    private fun setupBackPressHandling() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (binding.historyPanel.isVisible) {
                        hideHistoryPanel()
                        return
                    }

                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        )
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
            if (binding.historyPanel.isVisible) {
                hideHistoryPanel()
            } else {
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

        binding.btnAttach.setOnClickListener {
        }

        binding.btnMic.setOnClickListener {
            handleVoiceInputClick()
        }

        binding.btnHistory.setOnClickListener {
            toggleHistoryPanel()
        }

        binding.btnNewSession.setOnClickListener {
            viewModel.createNewSession()
            hideHistoryPanel()
        }

        binding.btnCreateSession.setOnClickListener {
            viewModel.createNewSession()
            hideHistoryPanel()
        }

        binding.historyScrim.setOnClickListener {
            hideHistoryPanel()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch {
                    viewModel.messages.collectLatest { messages ->
                        messageAdapter.submitList(messages)
                        if (messages.isNotEmpty()) {
                            binding.recyclerViewChat.scrollToPosition(messages.size - 1)
                        }
                    }
                }

                launch {
                    viewModel.chatSessions.collectLatest { sessions ->
                        sessionAdapter.submitList(sessions)
                        binding.tvEmptySessions.isVisible = sessions.isEmpty()
                        updateCurrentSessionLabel(viewModel.selectedSessionId.value, sessions)
                    }
                }

                launch {
                    viewModel.selectedSessionId.collectLatest { sessionId ->
                        sessionAdapter.setSelectedSessionId(sessionId)
                        updateCurrentSessionLabel(sessionId, sessionAdapter.currentList)
                    }
                }

                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        binding.etMessage.isEnabled = !isLoading
                        binding.btnSend.isEnabled = !isLoading
                        binding.progressBarChat.isVisible = isLoading
                    }
                }

                launch {
                    viewModel.error.collectLatest { error ->
                        if (error != null) {
                            showError(error)
                        }
                    }
                }
            }
        }
    }

    private fun updateCurrentSessionLabel(
        selectedSessionId: String?,
        sessions: List<ChatSessionUiModel>
    ) {
        val selectedTitle = sessions.firstOrNull { it.id == selectedSessionId }?.title
        binding.tvCurrentSession.text = selectedTitle
            ?.uppercase(Locale.getDefault())
            ?: getString(R.string.chat_session_current_default)
    }

    private fun toggleHistoryPanel() {
        if (binding.historyPanel.isVisible) {
            hideHistoryPanel()
        } else {
            showHistoryPanel()
        }
    }

    private fun showHistoryPanel() {
        binding.historyScrim.isVisible = true
        binding.historyPanel.isVisible = true
    }

    private fun hideHistoryPanel() {
        binding.historyScrim.isVisible = false
        binding.historyPanel.isVisible = false
    }

    private fun showDeleteSessionDialog(session: ChatSessionUiModel) {
        val dialog = AppConfirmDialog.newInstance(
            title = getString(R.string.chat_session_delete_title),
            message = getString(R.string.chat_session_delete_message, session.title),
            confirmText = getString(R.string.delete_confirm_action),
            cancelText = getString(R.string.delete_confirm_cancel),
            iconRes = R.drawable.ic_delete,
            destructive = true
        )
        dialog.listener = object : AppConfirmDialog.Listener {
            override fun onConfirm() {
                viewModel.deleteSession(session.id)
            }
        }
        dialog.show(childFragmentManager, "delete_chat_session_confirm")
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
