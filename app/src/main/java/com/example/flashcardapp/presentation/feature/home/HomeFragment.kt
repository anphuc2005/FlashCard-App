package com.example.flashcardapp.presentation.feature.home

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.navigation.fragment.findNavController
import androidx.navigation.NavOptions
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.bumptech.glide.Glide
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.FragmentHomeBinding
import com.example.flashcardapp.domain.model.study.StudyRecentSession
import com.example.flashcardapp.presentation.common.dialog.accountDialog.NotificationDialog
import com.example.flashcardapp.presentation.feature.addDeck.AddDeckContainerActivity
import com.example.flashcardapp.presentation.feature.learning.LearningActivity
import com.example.flashcardapp.presentation.feature.learning.EXTRA_AUTO_START_SESSION
import com.example.flashcardapp.presentation.feature.learning.EXTRA_CARD_SEQUENCE
import com.example.flashcardapp.presentation.feature.learning.EXTRA_DECK_ID
import com.example.flashcardapp.presentation.feature.learning.EXTRA_START_INDEX
import com.example.flashcardapp.presentation.feature.learning.EXTRA_STUDY_MODE
import com.example.flashcardapp.presentation.common.adapter.RecentDeckAdapter
import com.example.flashcardapp.presentation.common.adapter.ShortcutAdapter
import com.example.flashcardapp.presentation.common.dialog.accountDialog.AppConfirmDialog
import com.example.flashcardapp.presentation.common.dialog.accountDialog.ExportDataDialog
import com.example.flashcardapp.presentation.common.dialog.accountDialog.ThemeDialog
import com.example.flashcardapp.presentation.common.dialog.accountDialog.ReminderScheduler
import com.example.flashcardapp.presentation.common.notification.showAppError
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import androidx.core.view.isVisible

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()

    private lateinit var shortcutAdapter: ShortcutAdapter
    private lateinit var recentDeckAdapter: RecentDeckAdapter
    private var isExpandedRecentDecks = false

    private var themeOption = ThemeDialog.ThemeOption.LIGHT
    private var notifStudy = true
    private var notifNewDeck = false
    private var notifAchievement = true
    private var exportFormat = ExportDataDialog.ExportFormat.CSV

    private var reminderHour = 8
    private var reminderMinute = 0
    private var reminderEnabled = true
    private var avatarLoadJob: Job? = null
    private var skipNextResumeHomeRefresh = true
    private var skipNextResumeAvatarRefresh = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        binding.tvProgressPercent.text = getString(R.string.home_progress_format, 0)
        binding.progressBar.setProgress(0f)
        observeUiState()
        setupListeners()
        renderCachedAvatar()
        loadUserAvatar(forceRefresh = false)
    }

    override fun onResume() {
        super.onResume()
        if (skipNextResumeHomeRefresh) {
            skipNextResumeHomeRefresh = false
        } else {
            viewModel.refreshHomeRealtime()
        }

        if (skipNextResumeAvatarRefresh) {
            skipNextResumeAvatarRefresh = false
            return
        }
        loadUserAvatar(forceRefresh = true)
    }

    private fun setupAdapters() {
        // Setup Shortcuts Adapter
        shortcutAdapter = ShortcutAdapter { shortcut ->
            handleShortcutClick(shortcut.action)
        }
        binding.rvShortcuts.apply {
            adapter = shortcutAdapter
            layoutManager = GridLayoutManager(requireContext(), 4)
        }

        // Setup Recent Decks Adapter
        recentDeckAdapter = RecentDeckAdapter { deck ->
            navigateToDeckDetail(deck.id)
        }
        binding.rvDeckRecently.apply {
            adapter = recentDeckAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    val spacing = (12 * resources.displayMetrics.density).toInt()
                    outRect.right = spacing
                    outRect.bottom = spacing
                }
            })
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Update shortcuts
                    shortcutAdapter.submitList(state.shortcuts)

                    // Update recent decks
                    val displayedDecks = if (isExpandedRecentDecks) {
                        state.recentDecks
                    } else {
                        state.recentDecks.take(2)
                    }
                    recentDeckAdapter.submitList(displayedDecks)

                    // Update active deck info
                    state.activeDeck?.let { deck ->
                        binding.apply {
                            tvCourseTitle.text = deck.name
                            tvProgressPercent.text = getString(
                                com.example.flashcardapp.R.string.home_progress_format,
                                state.userProgress
                            )
                            progressBar.setProgress(state.userProgressRaw)
                        }
                    } ?: run {
                        binding.apply {
                            tvCourseTitle.text = getString(com.example.flashcardapp.R.string.home_no_active_deck)
                            tvProgressPercent.text = getString(
                                com.example.flashcardapp.R.string.home_progress_format,
                                0
                            )
                            progressBar.setProgress(0f)
                        }
                    }

                    // Update user info
                    binding.apply {
                        tvHello.text = state.userGreeting.ifBlank {
                            getString(com.example.flashcardapp.R.string.home_welcome_back)
                        }
                        tvStreak.text = state.userStreak.toString()
                    }

                    // Handle loading state
                    binding.btnStart.isEnabled = !state.isLoading && state.activeDeck != null
                    val contentAlpha = if (state.isLoading) 0.55f else 1f
                    binding.tvCourseTitle.alpha = contentAlpha
                    binding.progressBar.alpha = contentAlpha
                    binding.rvShortcuts.alpha = contentAlpha
                    binding.rvDeckRecently.alpha = contentAlpha

                    // Handle error state
                    state.error?.let { error ->
                        showError(error)
                    }
                }
            }
        }
    }

    private fun setupListeners() {
        binding.apply {
            btnStart.setOnClickListener {
                val state = viewModel.uiState.value
                val activeDeck = state.activeDeck ?: return@setOnClickListener
                val recentSession = state.recentStudySession
                if (recentSession != null &&
                    recentSession.deckId == activeDeck.id &&
                    recentSession.canResume
                ) {
                    showRecentSessionDialog(activeDeck.id, recentSession)
                } else {
                    navigateToDeckDetail(activeDeck.id)
                }
            }

            btnSeeAll.setOnClickListener {
                isExpandedRecentDecks = !isExpandedRecentDecks
                
                // Đổi chữ hiển thị của nút
                btnSeeAll.text = if (isExpandedRecentDecks) "Thu gọn" else getString(R.string.app_home_deck_all)
                
                // Đổi LayoutManager: Nằm ngang khi thu gọn, Lưới 2 dòng cuộn ngang khi mở rộng
                binding.rvDeckRecently.layoutManager = if (isExpandedRecentDecks) {
                    GridLayoutManager(requireContext(), 2, GridLayoutManager.HORIZONTAL, false)
                } else {
                    LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
                }

                // Cập nhật lại danh sách hiển thị
                val currentDecks = viewModel.uiState.value.recentDecks
                val displayedDecks = if (isExpandedRecentDecks) currentDecks else currentDecks.take(2)
                recentDeckAdapter.submitList(displayedDecks)
            }

        }
    }

    private fun handleShortcutClick(action: String?) {
        when (action) {
            "CREATE" -> navigateToCreateDeck()
            "NOTIFICATIONS" -> showNotifications()
            "EXPORT_DATA" -> showExportDataDialog()
            "CHANGE_THEME" -> showChangeThemeDialog()
            else -> {}
        }
    }

    private fun showError(@Suppress("UNUSED_PARAMETER") error: String) {
        viewModel.clearError()
    }

    private fun navigateToDeckDetail(
        deckId: String,
        autoStartSession: Boolean = false,
        mode: String? = null,
        startIndex: Int = 0,
        cardSequence: List<String> = emptyList()
    ) {
        viewModel.markDeckAsStudied(deckId)
        val intent = Intent(requireActivity(), LearningActivity::class.java).apply {
            putExtra(EXTRA_DECK_ID, deckId)
            putExtra(EXTRA_AUTO_START_SESSION, autoStartSession)
            putExtra(EXTRA_START_INDEX, startIndex)
            if (!mode.isNullOrBlank()) {
                putExtra(EXTRA_STUDY_MODE, mode)
            }
            if (cardSequence.isNotEmpty()) {
                putStringArrayListExtra(EXTRA_CARD_SEQUENCE, ArrayList(cardSequence))
            }
        }
        startActivity(intent)
    }

    private fun showRecentSessionDialog(deckId: String, recentSession: StudyRecentSession) {
        val dialog = AppConfirmDialog.newInstance(
            title = getString(R.string.learning_recent_dialog_title),
            message = getString(
                R.string.learning_recent_dialog_message,
                (recentSession.currentIndex + 1).coerceAtLeast(1),
                recentSession.totalCards.coerceAtLeast(1)
            ),
            confirmText = getString(R.string.learning_recent_dialog_restart),
            cancelText = getString(R.string.learning_recent_dialog_resume),
            iconRes = R.drawable.ic_cards,
            destructive = false
        )
        dialog.isCancelable = false
        dialog.listener = object : AppConfirmDialog.Listener {
            override fun onConfirm() {
                viewModel.restartRecentSession { success, message ->
                    if (!isAdded) return@restartRecentSession
                    if (success) {
                        navigateToDeckDetail(
                            deckId = deckId,
                            autoStartSession = true,
                            mode = recentSession.mode,
                            startIndex = 0
                        )
                    } else {
                        showAppError(message ?: getString(R.string.learning_recent_delete_failed))
                    }
                }
            }

            override fun onCancel() {
                viewModel.resolveRecentSessionForResume { payload, _ ->
                    if (!isAdded) return@resolveRecentSessionForResume
                    val resumePayload = payload ?: run {
                        showAppError(getString(R.string.learning_recent_delete_failed))
                        return@resolveRecentSessionForResume
                    }
                    navigateToDeckDetail(
                        deckId = deckId,
                        autoStartSession = true,
                        mode = resumePayload.mode,
                        startIndex = resumePayload.currentIndex,
                        cardSequence = resumePayload.cardSequence
                    )
                }
            }
        }
        dialog.show(childFragmentManager, "recent_learning_session_dialog")
    }

    private fun navigateToAllDecks() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(findNavController().graph.findStartDestination().id, false)
            .setLaunchSingleTop(true)
            .setRestoreState(true)
            .build()
        findNavController().navigate(R.id.deckFragment, null, navOptions)
    }

    private fun navigateToCreateDeck() {
        startActivity(Intent(requireContext(), AddDeckContainerActivity::class.java))
    }

    private fun showNotifications() {
        val dialog = NotificationDialog.newInstance(notifStudy, notifNewDeck, notifAchievement)
        dialog.listener = object : NotificationDialog.Listener {
            override fun onApply(study: Boolean, newDeck: Boolean, achievement: Boolean) {
                notifStudy = study
                notifNewDeck = newDeck
                notifAchievement = achievement
                ReminderScheduler.schedule(
                    requireContext(),
                    reminderHour,
                    reminderMinute,
                    reminderEnabled,
                    notifStudy
                )
            }
        }
        dialog.show(childFragmentManager, "NotificationDialog")
    }
    
    private fun showExportDataDialog(){
        val dialog = ExportDataDialog.newInstance(exportFormat)
        dialog.listener = object : ExportDataDialog.Listener {
            override fun onExport(format: ExportDataDialog.ExportFormat) {
                exportFormat = format
            }
        }
        dialog.show(childFragmentManager, "ExportDataDialog")
    }

    private fun showChangeThemeDialog(){
        val dialog = ThemeDialog.newInstance(themeOption)
        dialog.listener = object : ThemeDialog.Listener {
            override fun onThemeSaved(option: ThemeDialog.ThemeOption) {
                themeOption = option
                when (option) {
                    ThemeDialog.ThemeOption.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    ThemeDialog.ThemeOption.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                }
            }
        }
        dialog.show(childFragmentManager, "ChangeThemeDialog")
    }

    private fun renderCachedAvatar() {
        val cachedProfile = (requireActivity().application as FlashcardApp)
            .container
            .getMyProfileUseCase
            .getCachedProfile()
        renderAvatar(cachedProfile?.avatarUrl)
    }

    private fun loadUserAvatar(forceRefresh: Boolean) {
        avatarLoadJob?.cancel()
        avatarLoadJob = viewLifecycleOwner.lifecycleScope.launch {
            val profileResult = (requireActivity().application as FlashcardApp)
                .container
                .getMyProfileUseCase(forceRefresh = forceRefresh)

            profileResult.onSuccess { profile ->
                renderAvatar(profile.avatarUrl)
            }

            profileResult.onFailure {
                renderAvatar(null)
            }
        }
    }

    private fun renderAvatar(avatarUrl: String?) {
        if (avatarUrl.isNullOrBlank()) {
            binding.imgAvatar.setImageResource(R.drawable.user)
        } else {
            Glide.with(this@HomeFragment)
                .load(avatarUrl)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .into(binding.imgAvatar)
        }
    }


    override fun onDestroyView() {
        avatarLoadJob?.cancel()
        avatarLoadJob = null
        skipNextResumeAvatarRefresh = true
        super.onDestroyView()
        _binding = null
    }

}
