package com.example.flashcardapp.presentation.feature.aiChat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.flashcardapp.databinding.ActivityChatAiBinding

class ChatAIActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatAiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatAiBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}

