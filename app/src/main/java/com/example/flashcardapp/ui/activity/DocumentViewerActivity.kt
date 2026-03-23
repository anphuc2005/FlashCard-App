package com.example.flashcardapp.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import com.example.flashcardapp.R
import com.example.flashcardapp.databinding.ActivityDocumentViewerBinding

class DocumentViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDocumentViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityDocumentViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        applyInsets()
        binding.toolbar.title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
        binding.toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }

        binding.webViewDocument.apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = false
            settings.allowFileAccess = true
            loadUrl(DOCUMENT_ASSET_PREFIX + intent.getStringExtra(EXTRA_ASSET_PATH).orEmpty())
        }
    }

    private fun applyInsets() {
        val toolbarTopPadding = binding.toolbar.paddingTop
        val rootBottomPadding = binding.root.paddingBottom

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { _, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            binding.toolbar.updatePadding(top = toolbarTopPadding + systemBars.top)
            binding.root.updatePadding(bottom = rootBottomPadding + systemBars.bottom)
            insets
        }
    }

    companion object {
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_ASSET_PATH = "extra_asset_path"
        private const val DOCUMENT_ASSET_PREFIX = "file:///android_asset/"

        fun createIntent(context: Context, title: String, assetPath: String): Intent {
            return Intent(context, DocumentViewerActivity::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_ASSET_PATH, assetPath)
            }
        }
    }
}
