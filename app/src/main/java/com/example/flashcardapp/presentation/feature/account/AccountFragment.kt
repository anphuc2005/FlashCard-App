package com.example.flashcardapp.presentation.feature.account

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatDelegate
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentAccountBinding
import com.example.flashcardapp.databinding.ItemSettingRowBinding
import com.example.flashcardapp.presentation.common.dialog.accountDialog.ExportDataDialog
import com.example.flashcardapp.presentation.common.dialog.accountDialog.ExportDataDialog.ExportFormat
import com.example.flashcardapp.presentation.common.dialog.accountDialog.LogoutConfirmDialog
import com.example.flashcardapp.presentation.common.dialog.accountDialog.NotificationDialog
import com.example.flashcardapp.presentation.common.dialog.accountDialog.RatingDialog
import com.example.flashcardapp.presentation.common.dialog.accountDialog.ReminderDialog
import com.example.flashcardapp.presentation.common.dialog.accountDialog.ReminderScheduler
import com.example.flashcardapp.presentation.common.dialog.accountDialog.ThemeDialog
import com.example.flashcardapp.presentation.common.dialog.accountDialog.ThemeDialog.ThemeOption
import com.example.flashcardapp.presentation.feature.auth.AuthActivity
import com.example.flashcardapp.FlashcardApp

class AccountFragment : Fragment() {

    private var _binding: FragmentAccountBinding? = null
    private val binding get() = _binding!!

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { binding.avatar.setImageURI(it) }
    }

    private var pendingNotificationAction: (() -> Unit)? = null

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

    private val galleryPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) openImagePicker()
    }

    private val notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            pendingNotificationAction?.invoke()
        }
        pendingNotificationAction = null
    }

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
        setupRows()
        setupActions()
    }

    override fun onDestroyView() {
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

        configureRow(
            row = binding.rowExport,
            title = "Xuất dữ liệu",
            iconRes = R.drawable.ic_export_data,
            iconColorAttr = R.attr.iconBlue,
            iconBgRes = android.R.color.transparent
        )

        configureRow(
            row = binding.rowRate,
            title = "Đánh giá ứng dụng",
            iconRes = R.drawable.ic_rating,
            iconColorAttr = R.attr.iconBlue,
            iconBgRes = android.R.color.transparent
        )
    }

    private fun setupActions() {
        binding.btnEditAvatar.setOnClickListener { requestGalleryPermissionAndPick() }

        binding.rowReminder.root.setOnClickListener {
            ensureNotificationPermission { showReminderDialog() }
        }
        binding.rowTheme.root.setOnClickListener { showThemeDialog() }
        binding.rowNotification.root.setOnClickListener {
            ensureNotificationPermission { showNotificationDialog() }
        }
        binding.rowExport.root.setOnClickListener { showExportDialog() }
        binding.rowRate.root.setOnClickListener { showRatingDialog() }

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
        val permission =
            Manifest.permission.READ_MEDIA_IMAGES

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
        ) {
            openImagePicker()
        } else {
            galleryPermissionLauncher.launch(permission)
        }
    }

    private fun openImagePicker() {
        pickImageLauncher.launch("image/*")
    }

    private fun ensureNotificationPermission(onGranted: () -> Unit) {

        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            onGranted()
            return
        }

        pendingNotificationAction = onGranted
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

    private fun showReminderDialog() {
        val dialog = ReminderDialog.newInstance(reminderHour, reminderMinute, reminderEnabled)
        dialog.listener = object : ReminderDialog.Listener {
            override fun onReminderSaved(hour: Int, minute: Int, enabled: Boolean) {
                reminderHour = hour
                reminderMinute = minute
                reminderEnabled = enabled
                scheduleReminderIfNeeded()
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
        val dialog = NotificationDialog.newInstance(notifStudy, notifNewDeck, notifAchievement)
        dialog.listener = object : NotificationDialog.Listener {
            override fun onApply(study: Boolean, newDeck: Boolean, achievement: Boolean) {
                notifStudy = study
                notifNewDeck = newDeck
                notifAchievement = achievement
                scheduleReminderIfNeeded()
            }
        }
        dialog.show(childFragmentManager, "NotificationDialog")
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
        val dialog = LogoutConfirmDialog()
        dialog.listener = object : LogoutConfirmDialog.Listener {
            override fun onConfirmLogout() {
                val sessionManager = (requireActivity().application as FlashcardApp).container.sessionManager
                sessionManager.clearLoginSession()

                val intent = Intent(requireContext(), AuthActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    putExtra("OPEN_LOGIN", true)
                }
                startActivity(intent)
            }
        }
        dialog.show(childFragmentManager, "LogoutConfirmDialog")
    }
}
