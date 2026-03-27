package com.example.flashcardapp.data.datasource.remote.auth

import com.example.flashcardapp.core.constants.Constants
import com.example.flashcardapp.core.utils.NetworkErrorHandler
import com.example.flashcardapp.data.datasource.remote.api.AuthApiService
import com.example.flashcardapp.data.datasource.remote.model.auth.ForgotPasswordRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.ForgotPasswordResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.LoginRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.LoginResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.RegisterRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.RegisterResponse
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

