package com.example.flashcardapp.ui.feature.auth.data

import com.example.flashcardapp.network.AuthApiService
import com.example.flashcardapp.ui.feature.auth.model.ForgotPasswordRequest
import com.example.flashcardapp.ui.feature.auth.model.ForgotPasswordResponse
import com.example.flashcardapp.ui.feature.auth.model.LoginRequest
import com.example.flashcardapp.ui.feature.auth.model.LoginResponse
import com.example.flashcardapp.ui.feature.auth.model.RegisterRequest
import com.example.flashcardapp.ui.feature.auth.model.RegisterResponse
import com.example.flashcardapp.utils.Constants
import com.example.flashcardapp.utils.NetworkErrorHandler
import retrofit2.Response

class AuthRemoteDataSourceImpl(
    private val authApiService: AuthApiService
) : AuthRemoteDataSource {

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return executeRequest { authApiService.login(request) }
    }

    override suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return executeRequest { authApiService.register(request) }
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): Result<ForgotPasswordResponse> {
        return executeRequest { authApiService.forgotPassword(request) }
    }

    private suspend fun <T> executeRequest(apiCall: suspend () -> Response<T>): Result<T> {
        if (Constants.BASE_URL.contains("your-api-domain.com")) {
            return Result.failure(
                IllegalStateException("Vui lòng cấu hình BASE_URL và endpoint auth trước khi gọi API")
            )
        }

        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) }
                    ?: Result.failure(IllegalStateException("Response body is empty"))
            } else {
                Result.failure(
                    IllegalStateException(
                        response.errorBody()?.string()?.takeIf { it.isNotBlank() }
                            ?: response.message().takeIf { it.isNotBlank() }
                            ?: "Auth request failed with code ${response.code()}"
                    )
                )
            }
        } catch (throwable: Throwable) {
            Result.failure(IllegalStateException(NetworkErrorHandler.getErrorMessage(throwable)))
        }
    }
}
