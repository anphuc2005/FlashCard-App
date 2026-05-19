package com.example.flashcardapp.core.utils

import retrofit2.HttpException
import java.io.IOException

object NetworkErrorHandler {

    fun getErrorMessage(throwable: Throwable): String {
        return when (throwable) {
            is HttpException -> {
                when (throwable.code()) {
                    400 -> "Dữ liệu gửi lên chưa hợp lệ."
                    401 -> "Phiên đăng nhập không hợp lệ. Vui lòng đăng nhập lại."
                    403 -> "Bạn không có quyền thực hiện thao tác này."
                    404 -> "Không tìm thấy dữ liệu cần thiết."
                    500 -> "Hệ thống đang bận. Vui lòng thử lại sau."
                    else -> "Yêu cầu mạng thất bại (mã ${throwable.code()})."
                }
            }
            is IOException -> "Không thể kết nối internet. Vui lòng kiểm tra mạng."
            else -> UserMessageMapper.extractReadableMessage(throwable.message)
                ?: "Đã có lỗi xảy ra. Vui lòng thử lại."
        }
    }
}
