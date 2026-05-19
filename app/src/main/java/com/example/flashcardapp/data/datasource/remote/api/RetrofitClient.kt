package com.example.flashcardapp.data.datasource.remote.api

import com.example.flashcardapp.core.constants.Constants
import com.example.flashcardapp.BuildConfig
import com.example.flashcardapp.data.datasource.remote.model.auth.RefreshTokenRequest
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.Interceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    var tokenProvider: (() -> String?)? = null
    var refreshTokenProvider: (() -> String?)? = null
    var tokenRefreshHandler: ((accessToken: String, refreshToken: String?) -> Unit)? = null
    var authExpiredHandler: (() -> Unit)? = null

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BASIC
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val requestBuilder = originalRequest.newBuilder()
        val hasAuthorizationHeader = originalRequest.header("Authorization") != null
        val skipsAuth = originalRequest.header(NO_AUTH_HEADER) != null

        if (skipsAuth) {
            requestBuilder.removeHeader(NO_AUTH_HEADER)
        } else if (!hasAuthorizationHeader) {
            tokenProvider?.invoke()?.let { token ->
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }
        }
        val response = chain.proceed(requestBuilder.build())
        if ((response.code == HTTP_UNAUTHORIZED || response.code == HTTP_FORBIDDEN) && !skipsAuth) {
            authExpiredHandler?.invoke()
        }
        response
    }

    private val tokenAuthenticator = Authenticator { _: Route?, response ->
        if (response.code != HTTP_UNAUTHORIZED || response.request.header(NO_AUTH_HEADER) != null) {
            return@Authenticator null
        }
        if (responseCount(response) >= MAX_AUTH_RETRY_COUNT) {
            authExpiredHandler?.invoke()
            return@Authenticator null
        }

        val refreshToken = refreshTokenProvider?.invoke().orEmpty()
        if (refreshToken.isBlank()) {
            authExpiredHandler?.invoke()
            return@Authenticator null
        }

        synchronized(this) {
            val requestToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")
                ?.trim()
            val latestToken = tokenProvider?.invoke()
            if (!latestToken.isNullOrBlank() && latestToken != requestToken) {
                return@synchronized response.request.newBuilder()
                    .header("Authorization", "Bearer $latestToken")
                    .build()
            }

            val refreshResponse = runCatching {
                authRefreshApiService.refreshAccessToken(RefreshTokenRequest(refreshToken)).execute()
            }.getOrNull()

            val refreshedSession = refreshResponse
                ?.takeIf { it.isSuccessful }
                ?.body()
                ?.takeIf { it.isSuccess() }
                ?.data

            if (refreshedSession == null) {
                authExpiredHandler?.invoke()
                return@synchronized null
            }

            tokenRefreshHandler?.invoke(
                refreshedSession.accessToken,
                refreshedSession.refreshToken
            )

            response.request.newBuilder()
                .header("Authorization", "Bearer ${refreshedSession.accessToken}")
                .build()
        }
    }

    private val defaultClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .authenticator(tokenAuthenticator)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val refreshClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(defaultClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val refreshRetrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(refreshClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApiService: AuthApiService = retrofit.create(AuthApiService::class.java)
    private val authRefreshApiService: AuthApiService = refreshRetrofit.create(AuthApiService::class.java)
    val chatApiService: ChatApiService = retrofit.create(ChatApiService::class.java)
    val deckApiService: DeckApiService = retrofit.create(DeckApiService::class.java)
    val cardApiService: CardApiService = retrofit.create(CardApiService::class.java)
    val categoriesApiService: CategoriesApiService = retrofit.create(CategoriesApiService::class.java)
    val profileApiService: ProfileApiService = retrofit.create(ProfileApiService::class.java)
    val notificationApiService: NotificationApiService = retrofit.create(NotificationApiService::class.java)
    val reportApiService: ReportApiService = retrofit.create(ReportApiService::class.java)
    val uploadApiService: UploadApiService = retrofit.create(UploadApiService::class.java)
    val studyApiService: StudyApiService = retrofit.create(StudyApiService::class.java)
    val statisticsApiService: StatisticsApiService = retrofit.create(StatisticsApiService::class.java)

    private fun responseCount(response: okhttp3.Response): Int {
        var count = 1
        var priorResponse = response.priorResponse
        while (priorResponse != null) {
            count += 1
            priorResponse = priorResponse.priorResponse
        }
        return count
    }

    private const val NO_AUTH_HEADER = "No-Auth"
    private const val HTTP_UNAUTHORIZED = 401
    private const val HTTP_FORBIDDEN = 403
    private const val MAX_AUTH_RETRY_COUNT = 2
}
