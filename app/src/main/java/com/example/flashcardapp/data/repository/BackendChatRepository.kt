package com.example.flashcardapp.data.repository

import android.util.Log
import com.example.flashcardapp.core.utils.NetworkErrorHandler
import com.example.flashcardapp.core.utils.UserMessageMapper
import com.example.flashcardapp.data.datasource.remote.api.ChatApiService
import com.example.flashcardapp.data.datasource.remote.model.ApiResponse
import com.example.flashcardapp.data.datasource.remote.model.ChatMessageDto
import com.example.flashcardapp.data.datasource.remote.model.ChatSessionDto
import com.example.flashcardapp.data.datasource.remote.model.SendChatMessageRequest
import com.example.flashcardapp.domain.model.ChatMessage
import com.example.flashcardapp.domain.model.ChatSession
import com.example.flashcardapp.domain.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class BackendChatRepository(
    private val chatApiService: ChatApiService
) : ChatRepository {

    companion object {
        private const val TAG = "ChatBackendRepo"
        private const val MAX_LOG_CHUNK = 3500
    }

    override suspend fun createSession(): Result<ChatSession> = withContext(Dispatchers.IO) {
        runApiCall(operation = "createSession") {
            val response = chatApiService.createSession()
            response.toSingleResult { it.toDomain() }
        }
    }

    override suspend fun getSessions(): Result<List<ChatSession>> = withContext(Dispatchers.IO) {
        runApiCall(operation = "getSessions") {
            val response = chatApiService.getSessions()
            response.toListResult { it.toDomain() }
        }
    }

    override suspend fun getMessages(sessionId: String): Result<List<ChatMessage>> = withContext(Dispatchers.IO) {
        runApiCall(
            operation = "getMessages",
            requestSummary = "sessionId=$sessionId"
        ) {
            val response = chatApiService.getMessages(sessionId)
            response.toListResult { it.toDomain() }
        }
    }

    override suspend fun sendMessage(sessionId: String, content: String): Result<ChatMessage> = withContext(Dispatchers.IO) {
        runApiCall(
            operation = "sendMessage",
            requestSummary = "sessionId=$sessionId, contentLength=${content.length}"
        ) {
            val response = chatApiService.sendMessage(
                sessionId = sessionId,
                request = SendChatMessageRequest(content = content)
            )
            response.toSingleResult { it.toDomain(fallbackSessionId = sessionId) }
        }
    }

    override suspend fun deleteSession(sessionId: String): Result<Unit> = withContext(Dispatchers.IO) {
        runApiCall(
            operation = "deleteSession",
            requestSummary = "sessionId=$sessionId"
        ) {
            val response = chatApiService.deleteSession(sessionId)
            if (response.isSuccess()) {
                Result.success(Unit)
            } else {
                Result.failure(Exception(response.message ?: "Không thể xóa phiên chat"))
            }
        }
    }

    private inline fun <T> runApiCall(
        operation: String,
        requestSummary: String = "",
        block: () -> Result<T>
    ): Result<T> {
        Log.d(
            TAG,
            if (requestSummary.isBlank()) "[$operation] start"
            else "[$operation] start request=[$requestSummary]"
        )

        return try {
            val result = block()
            if (result.isSuccess) {
                Log.d(TAG, "[$operation] success")
            } else {
                val failure = result.exceptionOrNull()
                Log.e(
                    TAG,
                    "[$operation] failed without exception handler, request=[$requestSummary], reason=${failure?.message}",
                    failure
                )
            }
            result
        } catch (throwable: Throwable) {
            val errorBody = throwable.extractHttpErrorBody()
            val mappedMessage = mapErrorMessage(throwable, errorBody)

            Log.e(
                TAG,
                buildString {
                    append("[$operation] exception. ")
                    if (requestSummary.isNotBlank()) append("request=[$requestSummary], ")
                    if (throwable is HttpException) append("httpCode=${throwable.code()}, ")
                    append("mappedMessage=$mappedMessage")
                },
                throwable
            )

            if (!errorBody.isNullOrBlank()) {
                logLong(
                    priority = Log.ERROR,
                    tag = TAG,
                    prefix = "[$operation] errorBody=",
                    message = errorBody
                )
            }

            Result.failure(Exception(mappedMessage, throwable))
        }
    }

    private fun mapErrorMessage(
        throwable: Throwable,
        rawErrorBody: String?
    ): String {
        if (throwable is HttpException) {
            val errorBodyMessage = UserMessageMapper.extractReadableMessage(rawErrorBody)

            return when (throwable.code()) {
                400 -> "Vui lòng nhập nội dung."
                401 -> "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại."
                403 -> "Bạn không có quyền truy cập phiên chat này."
                404 -> "Phiên chat không tồn tại hoặc đã bị xóa."
                in 500..599 -> "AI tạm thời không khả dụng, vui lòng thử lại sau."
                else -> errorBodyMessage
                    ?: NetworkErrorHandler.getErrorMessage(throwable)
            }
        }
        return NetworkErrorHandler.getErrorMessage(throwable)
    }

    private fun Throwable.extractHttpErrorBody(): String? {
        if (this !is HttpException) return null
        return runCatching {
            response()?.errorBody()?.string()
        }.getOrNull()?.trim()
    }

    private fun logLong(priority: Int, tag: String, prefix: String, message: String) {
        if (message.length <= MAX_LOG_CHUNK) {
            Log.println(priority, tag, "$prefix$message")
            return
        }

        var index = 0
        var chunkNo = 1
        while (index < message.length) {
            val end = (index + MAX_LOG_CHUNK).coerceAtMost(message.length)
            Log.println(
                priority,
                tag,
                "$prefix(chunk:$chunkNo) ${message.substring(index, end)}"
            )
            index = end
            chunkNo += 1
        }
    }
}

private inline fun <T, R> ApiResponse<T>.toSingleResult(mapper: (T) -> R): Result<R> {
    if (!isSuccess()) {
        return Result.failure(Exception(message ?: "Yêu cầu thất bại"))
    }
    val payload = data ?: return Result.failure(Exception(message ?: "Không nhận được dữ liệu phản hồi"))
    return Result.success(mapper(payload))
}

private inline fun <T, R> ApiResponse<List<T>>.toListResult(mapper: (T) -> R): Result<List<R>> {
    if (!isSuccess()) {
        return Result.failure(Exception(message ?: "Yêu cầu thất bại"))
    }
    return Result.success(data.orEmpty().map(mapper))
}
