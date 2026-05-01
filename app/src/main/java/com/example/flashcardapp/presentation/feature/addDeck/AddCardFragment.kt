package com.example.flashcardapp.presentation.feature.addDeck

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.flashcardapp.databinding.FragmentAddCardBinding

import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.presentation.common.notification.showAppError
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class AddCardFragment : Fragment() {
    private lateinit var binding: FragmentAddCardBinding
    private var selectedImageUri: Uri? = null
    private var selectedAudioUri: Uri? = null
    private var saveProgressJob: Job? = null
    private val defaultSaveText = "Hoàn tất - lưu bộ thẻ"

    private val openImagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                binding.layoutImageEmpty.visibility = View.GONE
                binding.ivSelectedImage.visibility = View.VISIBLE
                binding.ivSelectedImage.setImageURI(it)
                createTempFileFromUri(it)?.let { imageFile ->
                    viewModel.uploadImage(imageFile)
                } ?: showAppError("Không thể đọc ảnh đã chọn")
            }
        }

    private val openAudioPicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedAudioUri = it
                binding.layoutAudioEmpty.visibility = View.GONE
                binding.layoutAudioSelected.visibility = View.VISIBLE
            }
        }

    private val viewModel: AddCardViewModel by viewModels {
        val appContainer = (requireActivity().application as FlashcardApp).container
        AddCardViewModelFactory(
            appContainer.addFlashCardsBulkUseCase,
            appContainer.uploadImageUseCase
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAddCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupListeners()
        observeViewModel()
    }

    override fun onDestroyView() {
        saveProgressJob?.cancel()
        super.onDestroyView()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnAddAnother.setOnClickListener {
            addCardToList()
        }

        binding.btnSave.setOnClickListener {
            saveAllCards()
        }

        binding.cardImage.setOnClickListener {
            checkAndRequestPermission(isImage = true)
        }

        binding.cardAudio.setOnClickListener {
            checkAndRequestPermission(isImage = false)
        }
    }

    private fun checkAndRequestPermission(isImage: Boolean) {
        if (isImage) {
            openImagePicker.launch("image/*")
        } else {
            openAudioPicker.launch("audio/*")
        }
    }

    private fun addCardToList() {
        val question = binding.etFront.text.toString().trim()
        val answer = binding.etBack.text.toString().trim()
        val deckId = arguments?.getString("DECK_ID") ?: "default_deck_id"
        viewModel.addCardToPending(question, answer, deckId)
    }

    private fun saveAllCards() {
        val question = binding.etFront.text.toString().trim()
        val answer = binding.etBack.text.toString().trim()
        val deckId = arguments?.getString("DECK_ID") ?: "default_deck_id"
        viewModel.saveAllPendingCards(question, answer, deckId)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Lắng nghe state của add card
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is AddCardState.Idle -> {
                                binding.loadingOverlay.isVisible = false
                                renderSaveProgressLoading(false)
                            }
                            is AddCardState.Loading -> {
                                binding.loadingOverlay.isVisible = false
                                renderSaveProgressLoading(true)
                            }
                            is AddCardState.CardQueued -> {
                                binding.loadingOverlay.isVisible = false
                                renderSaveProgressLoading(false)
                                binding.etFront.text?.clear()
                                binding.etBack.text?.clear()
                                
                                binding.layoutImageEmpty.visibility = View.VISIBLE
                                binding.ivSelectedImage.visibility = View.GONE
                                binding.ivSelectedImage.setImageURI(null)
                                selectedImageUri = null
                                
                                binding.layoutAudioEmpty.visibility = View.VISIBLE
                                binding.layoutAudioSelected.visibility = View.GONE
                                selectedAudioUri = null
                                viewModel.resetState()
                            }
                            is AddCardState.AllSaved -> {
                                binding.loadingOverlay.isVisible = false
                                renderSaveProgressLoading(false, forceComplete = true)
                                binding.etFront.text?.clear()
                                binding.etBack.text?.clear()
                                binding.layoutImageEmpty.visibility = View.VISIBLE
                                binding.ivSelectedImage.visibility = View.GONE
                                binding.ivSelectedImage.setImageURI(null)
                                selectedImageUri = null
                                binding.layoutAudioEmpty.visibility = View.VISIBLE
                                binding.layoutAudioSelected.visibility = View.GONE
                                selectedAudioUri = null
                                requireActivity().finish()
                                viewModel.resetState()
                            }
                            is AddCardState.Error -> {
                                binding.loadingOverlay.isVisible = false
                                renderSaveProgressLoading(false)
                                showAppError(state.message)
                            }
                        }
                    }
                }

                // Lắng nghe state của upload image
                launch {
                    viewModel.uploadState.collect { state ->
                        when (state) {
                            is UploadState.Idle -> {
                                binding.imageUploadOverlay.isVisible = false
                            }
                            is UploadState.Loading -> {
                                binding.imageUploadOverlay.isVisible = true
                                binding.ivSelectedImage.alpha = 0.55f
                            }
                            is UploadState.Success -> {
                                binding.imageUploadOverlay.isVisible = false
                                binding.ivSelectedImage.alpha = 1f
                                viewModel.resetUploadState()
                            }
                            is UploadState.Error -> {
                                binding.imageUploadOverlay.isVisible = false
                                binding.ivSelectedImage.alpha = 1f
                                showAppError(state.message)
                                viewModel.resetUploadState()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun renderSaveProgressLoading(isLoading: Boolean, forceComplete: Boolean = false) {
        if (!isLoading) {
            saveProgressJob?.cancel()
            if (forceComplete) {
                binding.saveProgressBar.progress = 100
            } else {
                binding.saveProgressBar.progress = 1
            }
            binding.btnSave.isEnabled = true
            binding.btnAddAnother.isEnabled = true
            binding.btnSave.text = defaultSaveText
            binding.saveProgressBar.isVisible = false
            return
        }

        binding.btnSave.isEnabled = false
        binding.btnAddAnother.isEnabled = false
        binding.btnSave.text = ""
        binding.saveProgressBar.isVisible = true
        binding.saveProgressBar.progress = 1

        saveProgressJob?.cancel()
        saveProgressJob = viewLifecycleOwner.lifecycleScope.launch {
            var value = 1
            while (value < 99) {
                delay(55)
                value += if (value < 40) 5 else if (value < 75) 3 else 2
                if (value > 99) value = 99
                binding.saveProgressBar.progress = value
            }
        }
    }

    private fun createTempFileFromUri(uri: Uri): File? {
        return runCatching {
            val inputStream = requireContext().contentResolver.openInputStream(uri) ?: return null
            val file = File(requireContext().cacheDir, "temp_img_${System.currentTimeMillis()}.jpg")
            inputStream.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            file
        }.getOrNull()
    }
}
