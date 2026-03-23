package com.example.flashcardapp.network

import okhttp3.Interceptor
import okhttp3.Response

/** Tự gắn access token vào header của các request cần xác thực. */
class AuthTokenInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val accessToken = tokenProvider()?.takeIf { it.isNotBlank() }
        val request = if (accessToken == null) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()
        }
        return chain.proceed(request)
    }
}
