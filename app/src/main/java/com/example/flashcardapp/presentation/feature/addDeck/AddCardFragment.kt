package com.example.flashcardapp.presentation.feature.addDeck

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.flashcardapp.databinding.FragmentAddCardBinding

import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.flashcardapp.FlashcardApp
import kotlinx.coroutines.launch
import java.io.File

class AddCardFragment : Fragment() {
    private lateinit var binding: FragmentAddCardBinding
    private var selectedImageUri: Uri? = null
    private var selectedAudioUri: Uri? = null

    private val requestImagePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) openImagePicker.launch("image/*")
            else Toast.makeText(requireContext(), "Cần cấp quyền để thêm ảnh", Toast.LENGTH_SHORT).show()
        }

    private val requestAudioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) openAudioPicker.launch("audio/*")
            else Toast.makeText(requireContext(), "Cần cấp quyền để thêm âm thanh", Toast.LENGTH_SHORT).show()
        }

    private val openImagePicker =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                binding.layoutImageEmpty.visibility = View.GONE
                binding.ivSelectedImage.visibility = View.VISIBLE
                binding.ivSelectedImage.setImageURI(it)
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
            appContainer.addFlashCardUseCase,
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

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnAddAnother.setOnClickListener {
            submitCard()
        }

        binding.btnSave.setOnClickListener {
            submitCard(shouldFinish = true)
        }

        binding.cardImage.setOnClickListener {
            checkAndRequestPermission(isImage = true)
        }

        binding.cardAudio.setOnClickListener {
            checkAndRequestPermission(isImage = false)
        }
    }

    private fun checkAndRequestPermission(isImage: Boolean) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (isImage) Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            if (isImage) openImagePicker.launch("image/*") else openAudioPicker.launch("audio/*")
        } else {
            if (isImage) requestImagePermissionLauncher.launch(permission) else requestAudioPermissionLauncher.launch(permission)
        }
    }

    private fun submitCard(shouldFinish: Boolean = false) {
        val question = binding.etFront.text.toString().trim()
        val answer = binding.etBack.text.toString().trim()
        
        // TODO: Get actual deckId from arguments
        val deckId = arguments?.getString("DECK_ID") ?: "default_deck_id"
        
        // Lệnh này dùng để check state finish hay continue phía sau
        view?.setTag(com.example.flashcardapp.R.id.nav_host_fragment_add_deck, shouldFinish)
        
        var imageFile: File? = null
        selectedImageUri?.let { uri ->
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            imageFile = File(requireContext().cacheDir, "temp_img_${System.currentTimeMillis()}.jpg")
            imageFile?.outputStream()?.use { out -> inputStream?.copyTo(out) }
        }

        viewModel.submitCard(question, answer, deckId, imageFile)
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Lắng nghe state của add card
                launch {
                    viewModel.uiState.collect { state ->
                        when (state) {
                            is AddCardState.Idle -> {
                                // Do nothing
                            }
                            is AddCardState.Loading -> {
                                // Show loading
                            }
                            is AddCardState.Success -> {
                                Toast.makeText(requireContext(), "Lưu thẻ thành công!", Toast.LENGTH_SHORT).show()
                                binding.etFront.text?.clear()
                                binding.etBack.text?.clear()
                                
                                binding.layoutImageEmpty.visibility = View.VISIBLE
                                binding.ivSelectedImage.visibility = View.GONE
                                binding.ivSelectedImage.setImageURI(null)
                                selectedImageUri = null
                                
                                binding.layoutAudioEmpty.visibility = View.VISIBLE
                                binding.layoutAudioSelected.visibility = View.GONE
                                selectedAudioUri = null
                                
                                val shouldFinish = view?.getTag(com.example.flashcardapp.R.id.nav_host_fragment_add_deck) as? Boolean ?: false
                                if (shouldFinish) {
                                    requireActivity().finish()
                                }
                                viewModel.resetState()
                            }
                            is AddCardState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                // Lắng nghe state của upload image
                launch {
                    viewModel.uploadState.collect { state ->
                        when (state) {
                            is UploadState.Idle -> {}
                            is UploadState.Loading -> {
                                Toast.makeText(requireContext(), "Đang tải ảnh lên...", Toast.LENGTH_SHORT).show()
                            }
                            is UploadState.Success -> {
                                Toast.makeText(requireContext(), "Tải ảnh xong", Toast.LENGTH_SHORT).show()
                                // TODO: state.url là link cloud, b có thể lưu url này vào để truyền cùng submitCard.
                                viewModel.resetUploadState()
                            }
                            is UploadState.Error -> {
                                Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                                viewModel.resetUploadState()
                            }
                        }
                    }
                }
            }
        }
    }
}
