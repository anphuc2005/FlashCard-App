package com.example.flashcardapp.presentation.feature.account

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.flashcardapp.R
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.data.datasource.local.session.ReminderSettingsStore
import com.example.flashcardapp.databinding.FragmentAccountBinding
import com.example.flashcardapp.databinding.ItemSettingRowBinding
import com.example.flashcardapp.domain.model.UserProfile
import com.example.flashcardapp.presentation.common.dialog.accountDialog.AppConfirmDialog
import com.example.flashcardapp.presentation.common.dialog.accountDialog.ExportDataDialog
import com.example.flashcardapp.presentation.common.dialog.accountDialog.ExportDataDialog.ExportFormat
import com.example.flashcardapp.presentation.common.dialog.accountDialog.NotificationDialog
import com.example.flashcardapp.presentation.common.dialog.accountDialog.RatingDialog
import com.example.flashcardapp.presentation.common.dialog.accountDialog.ReminderDialog
import com.example.flashcardapp.presentation.common.dialog.accountDialog.ReminderScheduler
import com.example.flashcardapp.presentation.common.dialog.accountDialog.ThemeDialog
import com.example.flashcardapp.presentation.common.dialog.accountDialog.ThemeDialog.ThemeOption
import com.example.flashcardapp.presentation.feature.auth.AuthActivity
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { binding.avatar.setImageURI(it) }
    }

    private var reminderHour = 8
    private var reminderMinute = 0
    private var reminderEnabled = true

    private var themeOption = ThemeOption.LIGHT
    private var notifStudy = true
    private var notifNewDeck = false
    private var notifAchievement = true

    private var exportFormat = ExportFormat.CSV

    private var ratingValue = 4
    private var ratingComment = ""
    private var profileLoadJob: Job? = null
    private var skipNextResumeRefresh = true

    private companion object {
        const val TAG = "AccountFragment"
    }

    private val galleryPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) openImagePicker()
    }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadReminderAndNotificationSettings()
        setupRows()
        setupActions()
        renderCachedProfile()
        loadProfile(forceRefresh = false)
    }

    override fun onResume() {
        super.onResume()
        if (skipNextResumeRefresh) {
            skipNextResumeRefresh = false
            return
        }
        loadProfile(forceRefresh = true)
    }

    override fun onDestroyView() {
        profileLoadJob?.cancel()
        profileLoadJob = null
        skipNextResumeRefresh = true
        _binding = null
        super.onDestroyView()
    }

    private fun setupRows() {
        configureRow(
            row = binding.rowReminder,
            title = "Nhắc nhở",
            iconRes = R.drawable.ic_time,
            iconColorAttr = R.attr.iconBlue,
            iconBgRes = android.R.color.transparent
        )

        configureRow(
            row = binding.rowTheme,
            title = "Giao diện",
            iconRes = R.drawable.ic_theme,
            iconColorAttr = R.attr.iconBlue,
            iconBgRes = android.R.color.transparent
        )

        configureRow(
            row = binding.rowNotification,
            title = "Thông báo",
            iconRes = R.drawable.ic_notif_shortcut,
            iconColorAttr = R.attr.iconBlue,
            iconBgRes = android.R.color.transparent
        )

//        configureRow(
//            row = binding.rowExport,
//            title = "Xuất dữ liệu",
//            iconRes = R.drawable.ic_export_data,
//            iconColorAttr = R.attr.iconBlue,
//            iconBgRes = android.R.color.transparent
//        )
//
//        configureRow(
//            row = binding.rowRate,
//            title = "Đánh giá ứng dụng",
//            iconRes = R.drawable.ic_rating,
//            iconColorAttr = R.attr.iconBlue,
//            iconBgRes = android.R.color.transparent
//        )
    }

    private fun setupActions() {
        binding.btnEditAvatar.setOnClickListener { requestGalleryPermissionAndPick() }

        binding.rowReminder.root.setOnClickListener {
            showReminderDialog()
        }
        binding.rowTheme.root.setOnClickListener { showThemeDialog() }
        binding.rowNotification.root.setOnClickListener {
            showNotificationDialog()
        }
//        binding.rowExport.root.setOnClickListener { showExportDialog() }
//        binding.rowRate.root.setOnClickListener { showRatingDialog() }
        binding.btnSettings.setOnClickListener {
            findNavController().navigate(R.id.action_accountFragment_to_editProfileFragment)
        }

        binding.btnLogout.setOnClickListener { showLogoutDialog() }
    }

    private fun configureRow(
        row: ItemSettingRowBinding,
        title: String,
        iconRes: Int,
        iconColorAttr: Int? = null,
        iconBgRes: Int
    ) {
        row.title.text = title
        row.icon.setImageResource(iconRes)
        iconColorAttr?.let { attr ->
            val color = com.google.android.material.color.MaterialColors.getColor(row.root, attr)
            row.icon.imageTintList = ColorStateList.valueOf(color)
        }
        row.iconBg.setBackgroundResource(iconBgRes)
    }

    private fun requestGalleryPermissionAndPick() {
        val permission = getGalleryPermission()

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
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

    private fun requestNotificationPermissionIfNeeded() {
        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            return
        }
        notificationPermissionLauncher.launch(permission)
    }

    private fun scheduleReminderIfNeeded() {
        ReminderScheduler.schedule(
            requireContext(),
            reminderHour,
            reminderMinute,
            reminderEnabled,
            notifStudy
        )
    }

    private fun loadReminderAndNotificationSettings() {
        val reminderSettings = ReminderSettingsStore.getReminderSettings(requireContext())
        reminderHour = reminderSettings.hour
        reminderMinute = reminderSettings.minute
        reminderEnabled = reminderSettings.enabled

        val notificationSettings = ReminderSettingsStore.getNotificationSettings(requireContext())
        notifStudy = notificationSettings.study
        notifNewDeck = notificationSettings.newDeck
        notifAchievement = notificationSettings.achievement
    }

    private fun showReminderDialog() {
        val dialog = ReminderDialog.newInstance(reminderHour, reminderMinute, reminderEnabled)
        dialog.listener = object : ReminderDialog.Listener {
            override fun onReminderSaved(hour: Int, minute: Int, enabled: Boolean) {
                reminderHour = hour
                reminderMinute = minute
                reminderEnabled = enabled
                scheduleReminderIfNeeded()
                if (enabled && notifStudy) {
                    requestNotificationPermissionIfNeeded()
                }
            }

            override fun onReminderHistory() {
                // No history source yet; placeholder for future hook
            }
        }
        dialog.show(childFragmentManager, "ReminderDialog")
    }

    private fun showThemeDialog() {
        val dialog = ThemeDialog.newInstance(themeOption)
        dialog.listener = object : ThemeDialog.Listener {
            override fun onThemeSaved(option: ThemeOption) {
                themeOption = option
                when (option) {
                    ThemeOption.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    ThemeOption.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
        }
        dialog.show(childFragmentManager, "ThemeDialog")
    }

    private fun showNotificationDialog() {
        childFragmentManager.setFragmentResultListener(
            NotificationDialog.RESULT_KEY,
            viewLifecycleOwner
        ) { _, result ->
            val study = result.getBoolean(NotificationDialog.RESULT_STUDY)
            val newDeck = result.getBoolean(NotificationDialog.RESULT_NEW_DECK)
            val achievement = result.getBoolean(NotificationDialog.RESULT_ACHIEVEMENT)
            applyNotificationSettings(study, newDeck, achievement)
        }
        val dialog = NotificationDialog.newInstance(notifStudy, notifNewDeck, notifAchievement)
        dialog.show(childFragmentManager, "NotificationDialog")
    }

    private fun applyNotificationSettings(
        study: Boolean,
        newDeck: Boolean,
        achievement: Boolean
    ) {
        notifStudy = study
        notifNewDeck = newDeck
        notifAchievement = achievement
        ReminderSettingsStore.saveNotificationSettings(
            requireContext(),
            study = study,
            newDeck = newDeck,
            achievement = achievement
        )
        scheduleReminderIfNeeded()
        if (study || newDeck || achievement) {
            requestNotificationPermissionIfNeeded()
        }
    }

    private fun showExportDialog() {
        val dialog = ExportDataDialog.newInstance(exportFormat)
        dialog.listener = object : ExportDataDialog.Listener {
            override fun onExport(format: ExportFormat) {
                exportFormat = format
                // Hook to actual export use-case here
            }
        }
        dialog.show(childFragmentManager, "ExportDataDialog")
    }

    private fun showRatingDialog() {
        val dialog = RatingDialog.newInstance(ratingValue, ratingComment)
        dialog.listener = object : RatingDialog.Listener {
            override fun onSubmit(rating: Int, comment: String) {
                ratingValue = rating
                ratingComment = comment
                // Hook to submit rating flow here
            }
        }
        dialog.show(childFragmentManager, "RatingDialog")
    }

    private fun showLogoutDialog() {
        val dialog = AppConfirmDialog.newInstance(
            title = getString(R.string.logout_confirm_title),
            message = getString(R.string.logout_confirm_message),
            confirmText = getString(R.string.logout_confirm_action),
            cancelText = getString(R.string.logout_confirm_cancel),
            iconRes = R.drawable.ic_logout,
            destructive = true
        )
        dialog.listener = object : AppConfirmDialog.Listener {
            override fun onConfirm() {
                viewLifecycleOwner.lifecycleScope.launch {
                    val container = (requireActivity().application as FlashcardApp).container
                    container.profileRepository.clearCachedProfile()
                    container.clearAccountLocalCache()
                    val sessionManager = container.sessionManager
                    sessionManager.clearLoginSession()

                    val intent = Intent(requireContext(), AuthActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        putExtra("OPEN_LOGIN", true)
                    }
                    startActivity(intent)
                }
            }
        }
        dialog.show(childFragmentManager, "logout_confirm")
    }

    private fun renderCachedProfile() {
        val cachedProfile = (requireActivity().application as FlashcardApp)
            .container
            .getMyProfileUseCase
            .getCachedProfile()
        cachedProfile?.let { renderProfile(it) }
    }

    private fun loadProfile(forceRefresh: Boolean) {
        profileLoadJob?.cancel()
        profileLoadJob = viewLifecycleOwner.lifecycleScope.launch {
            Log.i(TAG, "loadProfile() called -> requesting users/me")
            val profileResult = (requireActivity().application as FlashcardApp)
                .container
                .getMyProfileUseCase(forceRefresh = forceRefresh)

            profileResult.onSuccess { profile ->
                Log.i(
                    TAG,
                    "Render profile from users/me -> email=${profile.email}, displayName=${profile.displayName}, avatarUrl=${profile.avatarUrl}, createdAt=${profile.createdAt}"
                )
                renderProfile(profile)
            }
            profileResult.onFailure { throwable ->
                Log.e(TAG, "Failed to load profile from users/me", throwable)
            }
        }
    }

    private fun renderProfile(profile: UserProfile) {
        binding.name.text = profile.displayName.ifBlank { "Người dùng" }
        binding.memberSince.text = formatMemberSince(profile.createdAt)

        if (profile.avatarUrl.isNullOrBlank()) {
            binding.avatar.setImageResource(R.drawable.user)
        } else {
            Glide.with(this@AccountFragment)
                .load(profile.avatarUrl)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(binding.avatar)
        }
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
