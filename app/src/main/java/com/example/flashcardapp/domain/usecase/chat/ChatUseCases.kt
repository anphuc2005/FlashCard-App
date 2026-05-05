package com.example.flashcardapp.domain.usecase.chat

data class ChatUseCases(
    val createSession: CreateChatSessionUseCase,
    val getSessions: GetChatSessionsUseCase,
    val getMessages: GetChatMessagesUseCase,
    val sendMessage: SendChatMessageUseCase,
    val deleteSession: DeleteChatSessionUseCase
)
