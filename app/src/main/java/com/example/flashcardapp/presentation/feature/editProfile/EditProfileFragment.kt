package com.example.flashcardapp.presentation.feature.editProfile

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
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
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentEditProfileBinding
import kotlinx.coroutines.launch
import java.io.File

class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EditProfileViewModel
    private var selectedImageFile: File? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@registerForActivityResult
        binding.avatar.setImageURI(uri)
        selectedImageFile = createTempImageFile(uri)
        if (selectedImageFile == null) {
            Toast.makeText(requireContext(), "Không thể đọc ảnh đã chọn", Toast.LENGTH_SHORT).show()
        }
        viewModel.updateAvatarPreview(uri.toString())
    }

    private val galleryPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                openImagePicker()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupActions()
        observeUiState()
        viewModel.loadProfile()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupViewModel() {
        val container = (requireActivity().application as FlashcardApp).container
        viewModel = ViewModelProvider(
            this,
            EditProfileViewModelFactory(
                getMyProfileUseCase = container.getMyProfileUseCase,
                updateMyProfileUseCase = container.updateMyProfileUseCase,
                uploadImageUseCase = container.uploadImageUseCase
            )
        )[EditProfileViewModel::class.java]
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener { findNavController().popBackStack() }
        binding.btnEditAvatar.setOnClickListener { requestGalleryPermissionAndPick() }
        binding.btnSave.setOnClickListener { viewModel.submit(selectedImageFile) }

        binding.etFullName.doAfterTextChangedCompat { text ->
            viewModel.onDisplayNameChanged(text)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    binding.btnSave.isEnabled = !state.isLoading && !state.isSaving
                    binding.btnSave.alpha = if (state.isSaving) 0.7f else 1f

                    binding.tilFullName.error = state.displayNameError
                    if (binding.etFullName.text?.toString() != state.displayName) {
                        binding.etFullName.setText(state.displayName)
                    }
                    if (binding.etEmail.text?.toString() != state.email) {
                        binding.etEmail.setText(state.email)
                    }

                    binding.tvUserName.text = state.displayName.ifBlank { "Người dùng" }
                    binding.memberSince.text = formatMemberSince(state.createdAt)

                    loadAvatar(state.avatarUrl)

                    state.loadError?.let { error ->
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                        viewModel.consumeLoadError()
                    }

                    state.saveError?.let { error ->
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                        viewModel.consumeSaveError()
                    }

                    if (state.isSaved) {
                        selectedImageFile = null
                        Toast.makeText(requireContext(), "Đã lưu thay đổi", Toast.LENGTH_SHORT).show()
                        viewModel.consumeSavedEvent()
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    private fun requestGalleryPermissionAndPick() {
        val permission = getGalleryPermission()
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openImagePicker()
        } else {
            galleryPermissionLauncher.launch(permission)
        }
    }

    private fun getGalleryPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    private fun openImagePicker() {
        pickImageLauncher.launch("image/*")
    }

    private fun createTempImageFile(uri: Uri): File? {
        return runCatching {
            val tempFile = File(requireContext().cacheDir, "profile_${System.currentTimeMillis()}.jpg")
            requireContext().contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            tempFile
        }.getOrNull()
    }

    private fun loadAvatar(avatarUrl: String?) {
        if (avatarUrl.isNullOrBlank()) {
            binding.avatar.setImageResource(R.drawable.user)
            return
        }

        val avatarUri = Uri.parse(avatarUrl)
        if (avatarUri.scheme != null && avatarUri.scheme != "http" && avatarUri.scheme != "https") {
            binding.avatar.setImageURI(avatarUri)
            return
        }

        Glide.with(this)
            .load(avatarUrl)
            .placeholder(R.drawable.user)
            .error(R.drawable.user)
            .into(binding.avatar)
    }

    private fun formatMemberSince(createdAt: String?): String {
        if (createdAt.isNullOrBlank()) return "Thành viên"

        val yearFromInstant = runCatching {
            java.time.Instant.parse(createdAt).atZone(java.time.ZoneId.systemDefault()).year
        }.getOrNull()

        val yearFromText = Regex("(\\d{4})")
            .find(createdAt)
            ?.groupValues
            ?.getOrNull(1)
            ?.toIntOrNull()

        val year = yearFromInstant ?: yearFromText ?: return "Thành viên"
        return "Thành viên từ năm $year"
    }
}

private inline fun android.widget.EditText.doAfterTextChangedCompat(
    crossinline action: (String) -> Unit
) {
    addTextChangedListener(object : android.text.TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
        override fun afterTextChanged(s: android.text.Editable?) = action(s?.toString().orEmpty())
    })
}
