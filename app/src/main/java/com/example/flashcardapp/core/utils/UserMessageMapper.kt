package com.example.flashcardapp.core.utils

import com.google.gson.JsonParser

object UserMessageMapper {

    fun extractReadableMessage(rawMessage: String?): String? {
        val trimmed = rawMessage?.trim().orEmpty()
        if (trimmed.isBlank()) return null

        val extracted = extractJsonMessage(trimmed) ?: trimmed
        return mapKnownMessage(extracted)
    }

    private fun extractJsonMessage(rawMessage: String): String? {
        return runCatching {
            val jsonElement = JsonParser.parseString(rawMessage)
            if (!jsonElement.isJsonObject) return null

            jsonElement.asJsonObject.get("message")
                ?.takeIf { it.isJsonPrimitive }
                ?.asString
                ?.takeIf { it.isNotBlank() }
        }.getOrNull()
    }

    private fun mapKnownMessage(message: String): String {
        val normalized = message.trim().lowercase()

        return when {
            normalized.contains("email already registered") ->
                "Email này đã được đăng ký."
            normalized.contains("user not found") ->
                "Không tìm thấy tài khoản với email này."
            normalized.contains("invalid credentials") ||
                normalized.contains("incorrect password") ||
                normalized.contains("wrong password") ->
                "Email hoặc mật khẩu chưa chính xác."
            normalized.contains("otp") && (
                normalized.contains("invalid") ||
                    normalized.contains("incorrect")
                ) ->
                "Mã OTP không hợp lệ."
            normalized.contains("otp") && (
                normalized.contains("expired") ||
                    normalized.contains("timeout")
                ) ->
                "Mã OTP đã hết hạn."
            normalized.contains("verification code sent") ->
                "Mã xác minh đã được gửi tới email của bạn."
            normalized.contains("registration successful") ->
                "Đăng ký thành công."
            normalized.contains("login successful") ->
                "Đăng nhập thành công."
            normalized.contains("google login failed") ->
                "Đăng nhập Google thất bại."
            normalized.contains("response data is empty") ->
                "Không nhận được dữ liệu phản hồi từ hệ thống."
            normalized.contains("auth request failed") ->
                "Yêu cầu xác thực không thành công."
            else -> message.trim()
        }
    }
}
