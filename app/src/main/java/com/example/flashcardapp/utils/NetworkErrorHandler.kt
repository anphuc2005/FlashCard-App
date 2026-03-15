package com.example.flashcardapp.utils

import retrofit2.HttpException
import java.io.IOException

object NetworkErrorHandler {

    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is HttpException -> {
                when (throwable.code()) {
                    400 -> "Bad Request - Kiểm tra dữ liệu gửi"
                    401 -> "Unauthorized - Vui lòng đăng nhập"
                    403 -> "Forbidden - Bạn không có quyền truy cập"
                    404 -> "Not Found - Không tìm thấy dữ liệu"
                    500 -> "Internal Server Error - Lỗi server"
                    else -> "HTTP Error ${throwable.code()}"
                }
            }
            is IOException -> "Network Error - Kiểm tra kết nối internet"
            else -> throwable.message ?: "Unknown Error"
        }
    }
}

