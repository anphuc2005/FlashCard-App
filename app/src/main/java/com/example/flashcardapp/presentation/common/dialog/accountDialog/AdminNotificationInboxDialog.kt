package com.example.flashcardapp.presentation.common.dialog.accountDialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.flashcardapp.FlashcardApp
import com.example.flashcardapp.databinding.DialogAdminNotificationInboxBinding
import com.example.flashcardapp.presentation.common.adapter.NotificationInboxAdapter
import com.example.flashcardapp.presentation.common.notification.showAppError
import kotlinx.coroutines.launch
import kotlin.math.min

class AdminNotificationInboxDialog : DialogFragment() {

    interface Listener {
        fun onUnreadCountChanged(unreadCount: Int)
    }

    var listener: Listener? = null

    private var _binding: DialogAdminNotificationInboxBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AdminNotificationInboxViewModel by viewModels {
        val appContainer = (requireActivity().application as FlashcardApp).container
        AdminNotificationInboxViewModelFactory(
            appContainer.getNotificationPageUseCase,
            appContainer.markNotificationAsReadUseCase
        )
    }

    private lateinit var notificationAdapter: NotificationInboxAdapter
    private var unreadCount: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        unreadCount = arguments?.getInt(ARG_UNREAD_COUNT, 0)?.coerceAtLeast(0) ?: 0
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        _binding = DialogAdminNotificationInboxBinding.inflate(LayoutInflater.from(requireContext()))
        setupUi()
        observeUi()
        viewModel.loadInitial(force = true)

        return Dialog(requireContext()).apply {
            setContentView(binding.root)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onStart() {
        super.onStart()
        val screenWidth = resources.displayMetrics.widthPixels
        val maxWidth = (520 * resources.displayMetrics.density).toInt()
        val targetWidth = min((screenWidth * 0.9f).toInt(), maxWidth)
        dialog?.window?.setLayout(
            targetWidth,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun setupUi() {
        notificationAdapter = NotificationInboxAdapter { notification ->
            viewModel.markAsRead(notification.id)
        }

        val layoutManager = LinearLayoutManager(requireContext())
        binding.rvNotifications.apply {
            adapter = notificationAdapter
            this.layoutManager = layoutManager
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (dy <= 0) return
                    val lastVisible = layoutManager.findLastVisibleItemPosition()
                    if (lastVisible >= notificationAdapter.itemCount - 3) {
                        viewModel.loadMore()
                    }
                }
            })
        }

        binding.btnClose.setOnClickListener { dismiss() }
    }

    private fun observeUi() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.notifications.collect { notifications ->
                        notificationAdapter.submitList(notifications)
                        binding.tvEmpty.isVisible = notifications.isEmpty() && !viewModel.isInitialLoading.value
                    }
                }

                launch {
                    viewModel.isInitialLoading.collect { isLoading ->
                        binding.progressInitial.isVisible = isLoading
                        if (isLoading) {
                            binding.tvEmpty.isVisible = false
                        }
                    }
                }

                launch {
                    viewModel.isLoadingMore.collect { isLoadingMore ->
                        binding.progressLoadMore.isVisible = isLoadingMore
                    }
                }

                launch {
                    viewModel.error.collect { message ->
                        if (message.isNotBlank()) {
                            showAppError(message)
                        }
                    }
                }

                launch {
                    viewModel.readSuccess.collect {
                        if (unreadCount > 0) {
                            unreadCount -= 1
                            listener?.onUnreadCountChanged(unreadCount)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val ARG_UNREAD_COUNT = "arg_unread_count"

        fun newInstance(unreadCount: Int): AdminNotificationInboxDialog {
            return AdminNotificationInboxDialog().apply {
                arguments = Bundle().apply {
                    putInt(ARG_UNREAD_COUNT, unreadCount)
                }
            }
        }
    }
}
