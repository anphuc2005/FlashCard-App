package com.example.flashcardapp.data.repository

import android.util.Log
import com.example.flashcardapp.data.datasource.remote.api.ProfileApiService
import com.example.flashcardapp.data.datasource.remote.model.profile.UpdateProfileRequest
import com.example.flashcardapp.data.datasource.remote.model.profile.toDomain
import com.example.flashcardapp.domain.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProfileRepository(
    private val profileApiService: ProfileApiService,
    private val sessionTokenProvider: () -> String?
) {

    private companion object {
        const val TAG = "ProfileRepository"
    }
    @Volatile
    private var cachedProfile: UserProfile? = null
    @Volatile
    private var cachedSessionKey: String? = null

    fun getCachedProfile(): UserProfile? {
        val currentSessionKey = currentSessionKey()
        return if (isCacheValidForSession(currentSessionKey)) {
            cachedProfile
        } else {
            clearCachedProfile()
            null
        }
    }

    fun clearCachedProfile() {
        cachedProfile = null
        cachedSessionKey = null
    }

    suspend fun getMyProfile(forceRefresh: Boolean = false): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            val currentSessionKey = currentSessionKey()
            if (!forceRefresh && isCacheValidForSession(currentSessionKey)) {
                Log.i(TAG, "GET users/me -> return cached profile: email=${cachedProfile?.email}")
                return@withContext Result.success(cachedProfile!!)
            }

            if (cachedProfile != null && cachedSessionKey != currentSessionKey) {
                Log.i(TAG, "Session changed -> invalidate profile cache before GET users/me")
                clearCachedProfile()
            }

            try {
                val response = profileApiService.getMyProfile()
                Log.i(
                    TAG,
                    "GET users/me -> status=${response.status}, message=${response.message}, " +
                        "email=${response.data?.email}, displayName=${response.data?.displayName}"
                )
                if (response.isSuccess() && response.data != null) {
                    val profile = response.data.toDomain()
                    cacheProfile(profile, currentSessionKey)
                    Result.success(profile)
                } else {
                    Log.w(TAG, "GET users/me returned invalid payload (data is null or non-success)")
                    Result.failure(Exception(response.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "GET users/me failed", e)
                if (isCacheValidForSession(currentSessionKey)) {
                    Log.w(TAG, "GET users/me failed -> fallback to cached profile")
                    return@withContext Result.success(cachedProfile!!)
                }
                Result.failure(e)
            }
        }
    }

    suspend fun updateMyProfile(displayName: String, imageUrl: String? = null): Result<UserProfile> {
        return withContext(Dispatchers.IO) {
            val currentSessionKey = currentSessionKey()
            try {
                val response = profileApiService.updateMyProfile(
                    UpdateProfileRequest(
                        displayName = displayName.trim(),
                        imageUrl = imageUrl
                    )
                )
                Log.i(
                    TAG,
                    "PATCH users/me -> status=${response.status}, message=${response.message}, " +
                        "email=${response.data?.email}, displayName=${response.data?.displayName}, " +
                        "avatarUrl=${response.data?.avatarUrl}"
                )
                if (response.isSuccess() && response.data != null) {
                    val profile = response.data.toDomain()
                    cacheProfile(profile, currentSessionKey)
                    Result.success(profile)
                } else {
                    Log.w(TAG, "PATCH users/me returned invalid payload (data is null or non-success)")
                    Result.failure(Exception(response.message ?: "Unknown error"))
                }
            } catch (e: Exception) {
                Log.e(TAG, "PATCH users/me failed", e)
                Result.failure(e)
            }
        }
    }

    private fun currentSessionKey(): String? {
        return sessionTokenProvider()
            ?.trim()
            ?.takeIf { it.isNotBlank() }
    }

    private fun isCacheValidForSession(sessionKey: String?): Boolean {
        return cachedProfile != null && cachedSessionKey == sessionKey
    }

    private fun cacheProfile(profile: UserProfile, sessionKey: String?) {
        cachedProfile = profile
        cachedSessionKey = sessionKey
    }
}
