package com.example.flashcardapp

import android.app.Application
import com.example.flashcardapp.network.RetrofitClient

// FlashCardApplication khởi tạo dependency dùng chung ngay khi app mở.
class FlashCardApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Khởi tạo lớp mạng dùng chung một lần cho toàn app.
        RetrofitClient.initialize(this)
    }
}
