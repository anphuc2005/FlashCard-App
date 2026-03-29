package com.example.flashcardapp.data.datasource.remote.auth

import android.util.Log
import com.example.flashcardapp.core.utils.NetworkErrorHandler
import com.example.flashcardapp.data.datasource.remote.api.AuthApiService
import com.example.flashcardapp.data.datasource.remote.model.auth.ForgotPasswordRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.ForgotPasswordResponse
import com.example.flashcardapp.data.datasource.remote.model.ApiResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.LoginRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.LoginResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.RegisterRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.RegisterResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.ResetPasswordRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.ResetPasswordResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.VerifyOtpRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.VerifyOtpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class AuthRemoteDataSourceImpl(
    private val authApiService: AuthApiService
) : AuthRemoteDataSource {

    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return withContext(Dispatchers.IO) {
            executeRequest("login", request) { authApiService.login(request) }
        }
    }

    override suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return withContext(Dispatchers.IO) {
            executeRequest("register", request) { authApiService.register(request) }
        }
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): Result<ForgotPasswordResponse> {
        return withContext(Dispatchers.IO) {
            executeRequest(
                methodName = "forgotPassword",
                request = request,
                allowEmptyData = true,
                emptyMapper = { api -> ForgotPasswordResponse(message = api.message ?: "Success") }
            ) { authApiService.forgotPassword(request) }
        }
    }

    override suspend fun verifyOtp(request: VerifyOtpRequest): Result<VerifyOtpResponse> {
        return withContext(Dispatchers.IO) {
            executeRequest(
                methodName = "verifyOtp",
                request = request,
                allowEmptyData = true,
                emptyMapper = { api -> VerifyOtpResponse(message = api.message ?: "Success") }
            ) { authApiService.verifyOtp(request) }
        }
    }

    override suspend fun resetPassword(request: ResetPasswordRequest): Result<ResetPasswordResponse> {
        return withContext(Dispatchers.IO) {
            executeRequest(
                methodName = "resetPassword",
                request = request,
                allowEmptyData = true,
                emptyMapper = { api -> ResetPasswordResponse(message = api.message ?: "Success") }
            ) { authApiService.resetPassword(request) }
        }
    }

    private suspend fun <T, R> executeRequest(
        methodName: String,
        request: T,
        allowEmptyData: Boolean = false,
        emptyMapper: ((ApiResponse<R>) -> R)? = null,
        apiCall: suspend () -> Response<ApiResponse<R>>
    ): Result<R> {
        return try {
            Log.d("AuthRemoteDataSource", "Starting $methodName request with: $request")
            
            val response = apiCall()
            
            Log.d("AuthRemoteDataSource", "$methodName response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                Log.d("AuthRemoteDataSource", "$methodName ApiResponse: status=${apiResponse?.status}, message=${apiResponse?.message}")
                
                if (apiResponse != null && apiResponse.isSuccess()) {
                    apiResponse.data?.let {
                        Log.d("AuthRemoteDataSource", "$methodName success with data")
                        Result.success(it)
                    } ?: run {
                        if (allowEmptyData && emptyMapper != null) {
                            Log.d("AuthRemoteDataSource", "$methodName success without data, using emptyMapper")
                            Result.success(emptyMapper(apiResponse))
                        } else {
                            val errorMsg = "Response data is empty"
                            Log.e("AuthRemoteDataSource", errorMsg)
                            Result.failure(IllegalStateException(errorMsg))
                        }
                    }
                } else {
                    val errorMsg = apiResponse?.message ?: "Auth request failed"
                    Log.e("AuthRemoteDataSource", "$methodName failed: $errorMsg (status=${apiResponse?.status})")
                    Result.failure(IllegalStateException(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = errorBody?.takeIf { it.isNotBlank() }
                    ?: response.message().takeIf { it.isNotBlank() }
                    ?: "Auth request failed with code ${response.code()}"
                Log.e("AuthRemoteDataSource", "$methodName HTTP error (${response.code()}): $errorMsg")
                Result.failure(IllegalStateException(errorMsg))
            }
        } catch (throwable: Throwable) {
            val errorMsg = NetworkErrorHandler.getErrorMessage(throwable)
            Log.e("AuthRemoteDataSource", "$methodName exception: $errorMsg", throwable)
            Result.failure(IllegalStateException(errorMsg))
        }
    }
}
