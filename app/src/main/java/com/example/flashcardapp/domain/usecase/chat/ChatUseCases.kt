package com.example.flashcardapp.domain.usecase.chat

data class ChatUseCases(
    val observeMessages: ObserveChatMessagesUseCase,
    val saveMessage: SaveChatMessageUseCase,
    val deleteMessage: DeleteChatMessageUseCase,
    val clearMessages: ClearChatMessagesUseCase,
    val sendMessage: SendChatMessageUseCase
)

