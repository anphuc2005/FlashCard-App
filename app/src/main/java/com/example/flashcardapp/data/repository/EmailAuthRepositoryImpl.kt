package com.example.flashcardapp.data.repository

import android.util.Log
import com.example.flashcardapp.core.utils.NetworkErrorHandler
import com.example.flashcardapp.core.utils.UserMessageMapper
import com.example.flashcardapp.data.datasource.local.session.AuthSessionStore
import com.example.flashcardapp.data.datasource.remote.api.AuthApiService
import com.example.flashcardapp.data.datasource.remote.model.ApiResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.ForgotPasswordRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.ForgotPasswordResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.GoogleLoginRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.GoogleLoginResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.LoginRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.LoginResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.RegisterRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.RegisterResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.ResetPasswordRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.ResetPasswordResponse
import com.example.flashcardapp.data.datasource.remote.model.auth.VerifyOtpRequest
import com.example.flashcardapp.data.datasource.remote.model.auth.VerifyOtpResponse
import com.example.flashcardapp.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class EmailAuthRepositoryImpl(
    private val authApiService: AuthApiService,
    private val sessionStore: AuthSessionStore,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : AuthRepository {

    override suspend fun login(request: LoginRequest): Result<LoginResponse> = withContext(ioDispatcher) {
        executeRequest("login", request) { authApiService.login(request) }
    }

    override suspend fun googleLogin(request: GoogleLoginRequest): Result<GoogleLoginResponse> = withContext(ioDispatcher) {
        executeRequest("googleLogin", request) { authApiService.googleLogin(request) }
    }

    override suspend fun register(request: RegisterRequest): Result<RegisterResponse> = withContext(ioDispatcher) {
        executeRequest("register", request) { authApiService.register(request) }
    }

    override suspend fun forgotPassword(request: ForgotPasswordRequest): Result<ForgotPasswordResponse> = withContext(ioDispatcher) {
        executeRequest(
            methodName = "forgotPassword",
            request = request,
            allowEmptyData = true,
            emptyMapper = { api -> ForgotPasswordResponse(message = api.message ?: "Success") }
        ) { authApiService.forgotPassword(request) }
    }

    override suspend fun verifyOtp(request: VerifyOtpRequest): Result<VerifyOtpResponse> = withContext(ioDispatcher) {
        executeRequest(
            methodName = "verifyOtp",
            request = request,
            allowEmptyData = true,
            emptyMapper = { api -> VerifyOtpResponse(message = api.message ?: "Success") }
        ) { authApiService.verifyOtp(request) }
    }

    override suspend fun resetPassword(request: ResetPasswordRequest): Result<ResetPasswordResponse> = withContext(ioDispatcher) {
        executeRequest(
            methodName = "resetPassword",
            request = request,
            allowEmptyData = true,
            emptyMapper = { api -> ResetPasswordResponse(message = api.message ?: "Success") }
        ) { authApiService.resetPassword(request) }
    }

    override fun saveLoginSession(accessToken: String?, refreshToken: String?) {
        sessionStore.saveLoginSession(accessToken, refreshToken)
    }

    override fun clearLoginSession() {
        sessionStore.clearLoginSession()
    }

    private suspend fun <T, R> executeRequest(
        methodName: String,
        request: T,
        allowEmptyData: Boolean = false,
        emptyMapper: ((ApiResponse<R>) -> R)? = null,
        apiCall: suspend () -> Response<ApiResponse<R>>
    ): Result<R> {
        return try {
            Log.d("EmailAuthRepository", "Starting $methodName request with: $request")
            
            val response = apiCall()
            
            Log.d("EmailAuthRepository", "$methodName response code: ${response.code()}")
            
            if (response.isSuccessful) {
                val apiResponse = response.body()
                Log.d("EmailAuthRepository", "$methodName ApiResponse: status=${apiResponse?.status}, message=${apiResponse?.message}")
                
                if (apiResponse != null && apiResponse.isSuccess()) {
                    apiResponse.data?.let {
                        Log.d("EmailAuthRepository", "$methodName success with data")
                        Result.success(it)
                    } ?: run {
                        if (allowEmptyData && emptyMapper != null) {
                            Log.d("EmailAuthRepository", "$methodName success without data, using emptyMapper")
                            Result.success(emptyMapper(apiResponse))
                        } else {
                            val errorMsg = "Response data is empty"
                            Log.e("EmailAuthRepository", errorMsg)
                            Result.failure(IllegalStateException(errorMsg))
                        }
                    }
                } else {
                    val errorMsg = UserMessageMapper.extractReadableMessage(apiResponse?.message)
                        ?: "Yêu cầu xác thực không thành công."
                    Log.e("EmailAuthRepository", "$methodName failed: $errorMsg (status=${apiResponse?.status})")
                    Result.failure(IllegalStateException(errorMsg))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMsg = UserMessageMapper.extractReadableMessage(errorBody)
                    ?: UserMessageMapper.extractReadableMessage(response.message())
                    ?: "Yêu cầu xác thực thất bại (mã ${response.code()})."
                Log.e("EmailAuthRepository", "$methodName HTTP error (${response.code()}): $errorMsg")
                Result.failure(IllegalStateException(errorMsg))
            }
        } catch (throwable: Throwable) {
            val errorMsg = NetworkErrorHandler.getErrorMessage(throwable)
            Log.e("EmailAuthRepository", "$methodName exception: $errorMsg", throwable)
            Result.failure(IllegalStateException(errorMsg))
        }
    }
}
