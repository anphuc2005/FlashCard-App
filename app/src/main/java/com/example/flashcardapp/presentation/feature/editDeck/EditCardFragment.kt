package com.example.flashcardapp.presentation.feature.editDeck

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentEditCardBinding
import com.example.flashcardapp.presentation.common.dialog.accountDialog.AppConfirmDialog
import com.example.flashcardapp.presentation.common.notification.showAppError
import com.example.flashcardapp.presentation.common.notification.showAppSuccess
import kotlinx.coroutines.launch
import java.io.File

class EditCardFragment : Fragment() {

    private var _binding: FragmentEditCardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditCardViewModel by viewModels {
        val appContainer = (requireActivity().application as FlashcardApp).container
        EditCardViewModelFactory(
            appContainer.updateFlashCardUseCase,
            appContainer.deleteFlashCardUseCase,
            appContainer.uploadImageUseCase
        )
    }

    private var cardId: String = ""
    private var deckId: String = ""
    private var initialQuestion: String = ""
    private var initialAnswer: String = ""
    private var initialImageUrl: String? = null

    private val openImagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            val bindingRef = _binding ?: return@registerForActivityResult
            uri?.let {
                bindingRef.layoutImageEmpty.visibility = View.GONE
                bindingRef.ivSelectedImage.visibility = View.VISIBLE
                bindingRef.ivSelectedImage.setImageURI(it)
                createTempFileFromUri(it)?.let { imageFile ->
                    viewModel.uploadImage(imageFile)
                } ?: run {
                    showAppError("Không thể đọc ảnh đã chọn")
                    renderImagePreview(viewModel.getCurrentImageUrl())
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditCardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.headerTitle.text = getString(R.string.edit_card_title)
        if (!bindInitialData()) {
            showAppError(getString(R.string.edit_card_missing_arguments))
            findNavController().popBackStack()
            return
        }

        setupListeners()
        observeData()
    }

    private fun bindInitialData(): Boolean {
        cardId = arguments?.getString("CARD_ID").orEmpty()
        deckId = arguments?.getString("DECK_ID").orEmpty()
        initialQuestion = arguments?.getString("QUESTION").orEmpty()
        initialAnswer = arguments?.getString("ANSWER").orEmpty()
        initialImageUrl = arguments?.getString("IMAGE_URL")

        if (cardId.isBlank() || deckId.isBlank()) {
            return false
        }

        viewModel.setInitialImageUrl(initialImageUrl)
        binding.etFront.setText(initialQuestion)
        binding.etBack.setText(initialAnswer)
        renderImagePreview(initialImageUrl)
        return true
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.cardImage.setOnClickListener {
            openImagePicker.launch("image/*")
        }

        binding.btnSave.setOnClickListener {
            val question = binding.etFront.text?.toString()?.trim().orEmpty()
            val answer = binding.etBack.text?.toString()?.trim().orEmpty()
            if (cardId.isBlank() || deckId.isBlank()) {
                showAppError(getString(R.string.edit_card_missing_arguments))
                return@setOnClickListener
            }
            viewModel.updateCard(cardId, question, answer, deckId, viewModel.getCurrentImageUrl())
        }

        binding.btnAddAnother.setOnClickListener {
            if (cardId.isBlank() || deckId.isBlank()) {
                showAppError(getString(R.string.edit_card_missing_arguments))
                return@setOnClickListener
            }

            val dialog = AppConfirmDialog.newInstance(
                title = getString(R.string.delete_confirm_title),
                message = getString(R.string.delete_confirm_message_card),
                confirmText = getString(R.string.delete_confirm_action),
                cancelText = getString(R.string.delete_confirm_cancel),
                iconRes = R.drawable.ic_delete,
                destructive = true
            )
            dialog.listener = object : AppConfirmDialog.Listener {
                override fun onConfirm() {
                    viewModel.deleteCard(cardId, initialQuestion, initialAnswer, deckId)
                }
            }
            dialog.show(childFragmentManager, "delete_card_confirm")
        }
        binding.btnAddAnother.text = getString(R.string.edit_card_delete)
    }

    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is EditCardState.Idle -> renderLoading(false)
                            is EditCardState.Loading -> renderLoading(true)
                            is EditCardState.Success -> {
                                renderLoading(false)
                                showAppSuccess(getString(R.string.edit_card_update_success))
                                viewModel.resetState()
                                findNavController().popBackStack()
                            }
                            is EditCardState.Deleted -> {
                                renderLoading(false)
                                showAppSuccess(getString(R.string.delete_success_card))
                                viewModel.resetState()
                                findNavController().popBackStack()
                            }
                            is EditCardState.Error -> {
                                renderLoading(false)
                                showAppError(state.message)
                                viewModel.resetState()
                            }
                        }
                    }
                }

                launch {
                    viewModel.uploadState.collect { state ->
                        when (state) {
                            is EditCardUploadState.Idle -> {
                                renderImageUploadLoading(false)
                            }
                            is EditCardUploadState.Loading -> {
                                renderImageUploadLoading(true)
                            }
                            is EditCardUploadState.Success -> {
                                renderImageUploadLoading(false)
                                viewModel.resetUploadState()
                            }
                            is EditCardUploadState.Error -> {
                                renderImageUploadLoading(false)
                                showAppError(state.message)
                                renderImagePreview(viewModel.getCurrentImageUrl())
                                viewModel.resetUploadState()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun renderImagePreview(imageUrl: String?) {
        if (imageUrl.isNullOrBlank()) {
            binding.layoutImageEmpty.visibility = View.VISIBLE
            binding.ivSelectedImage.visibility = View.GONE
            Glide.with(this).clear(binding.ivSelectedImage)
            binding.ivSelectedImage.setImageDrawable(null)
            return
        }

        binding.layoutImageEmpty.visibility = View.GONE
        binding.ivSelectedImage.visibility = View.VISIBLE
        Glide.with(this)
            .load(imageUrl)
            .centerCrop()
            .into(binding.ivSelectedImage)
    }

    private fun renderImageUploadLoading(isLoading: Boolean) {
        binding.imageUploadOverlay.isVisible = isLoading
        binding.ivSelectedImage.alpha = if (isLoading) 0.55f else 1f
    }

    private fun renderLoading(isLoading: Boolean) {
        binding.btnBack.isEnabled = !isLoading
        binding.btnSave.isEnabled = !isLoading
        binding.btnAddAnother.isEnabled = !isLoading

        val actionAlpha = if (isLoading) 0.65f else 1f
        binding.btnSave.alpha = actionAlpha
        binding.btnAddAnother.alpha = actionAlpha
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
