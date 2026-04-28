package com.example.flashcardapp.presentation.feature.aiChat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flashcardapp.domain.model.ChatMessage
import com.example.flashcardapp.domain.usecase.category.GetAllCategoriesUseCase
import com.example.flashcardapp.domain.usecase.chat.ChatUseCases
import com.example.flashcardapp.domain.usecase.deck.AddDeckUseCase
import com.example.flashcardapp.domain.usecase.deck.ExploreDecksUseCase
import com.example.flashcardapp.domain.usecase.flashcard.AddFlashCardUseCase
import com.example.flashcardapp.utils.DeckMatchingHelper
import com.example.flashcardapp.utils.MarkdownConverter
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.Normalizer
import java.util.UUID

enum class MessageStatus {
    SENDING, SUCCESS, ERROR
}

private data class DeckCardDraft(
    val question: String,
    val answer: String
)

private data class DeckDraft(
    val name: String,
    val description: String,
    val cards: List<DeckCardDraft>,
    val language: String? = null,
    val difficulty: String? = null,
    val suggestedCategoryId: String? = null,
    val requestedCardCount: Int? = null
)

private data class PendingDeckCreation(
    val draft: DeckDraft,
    val sourcePrompt: String,
    val createdAt: Long = System.currentTimeMillis()
)

class ChatAIViewModel(
    private val useCases: ChatUseCases,
    private val exploreDecksUseCase: ExploreDecksUseCase,
    private val addDeckUseCase: AddDeckUseCase,
    private val addFlashCardUseCase: AddFlashCardUseCase,
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase
) : ViewModel() {

    companion object {
        private const val MAX_HISTORY_MESSAGES_FOR_REQUEST = 12
        private const val MIN_DRAFT_CARDS = 3
        private const val MAX_DRAFT_CARDS = 50
        private const val DEFAULT_DRAFT_CARDS = 10

        private val JSON_CODE_BLOCK_REGEX = Regex(
            pattern = "```(?:json)?\\s*(\\{[\\s\\S]*\\})\\s*```",
            option = RegexOption.IGNORE_CASE
        )
    }

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _pendingDeckDraftMessageId = MutableStateFlow<String?>(null)
    val pendingDeckDraftMessageId: StateFlow<String?> = _pendingDeckDraftMessageId.asStateFlow()

    private var pendingDeckCreation: PendingDeckCreation? = null
    private var cachedDefaultCategoryId: String? = null

    init {
        observeMessages()
    }

    private fun observeMessages() {
        viewModelScope.launch {
            useCases.observeMessages().collect { messages ->
                _messages.value = messages.sortedBy { it.timestamp }
            }
        }
    }

    /**
     * Gửi tin nhắn
     */
    fun sendMessage(userText: String) {
        if (userText.isBlank()) return

        val historyForRequest = _messages.value
            .filter { it.sender == "user" || it.sender == "bot" }
            .takeLast(MAX_HISTORY_MESSAGES_FOR_REQUEST)

        val userMessage = ChatMessage(
            id = UUID.randomUUID().toString(),
            message = userText,
            sender = "user",
            timestamp = System.currentTimeMillis()
        )
        _messages.value = _messages.value + userMessage
        _error.value = null

        viewModelScope.launch {
            useCases.saveMessage(userMessage)
        }

        viewModelScope.launch {
            _isLoading.value = true

            val aiMessageId = UUID.randomUUID().toString()
            val sendingMessage = ChatMessage(
                id = aiMessageId,
                message = "Đang suy nghĩ...",
                sender = "bot",
                timestamp = System.currentTimeMillis()
            )
            _messages.value = _messages.value + sendingMessage
            useCases.saveMessage(sendingMessage)

            val result: Result<String> = try {
                when {
                    pendingDeckCreation != null && isDeckCreationConfirmation(userText) -> {
                        Result.success(confirmPendingDeckCreation())
                    }

                    pendingDeckCreation != null && isDeckCreationCancellation(userText) -> {
                        Result.success(cancelPendingDeckCreation())
                    }

                    shouldCreateDeckDraft(userText) -> {
                        Result.success(prepareDeckDraft(userText, historyForRequest))
                    }

                    else -> {
                        sendStandardAiMessage(userText, historyForRequest)
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }

            result.onSuccess { aiResponse ->
                val normalizedMarkdown = MarkdownConverter.normalizeForChatDisplay(aiResponse)
                val successMessage = ChatMessage(
                    id = aiMessageId,
                    message = normalizedMarkdown,
                    sender = "bot",
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = _messages.value.map { message ->
                    if (message.id == aiMessageId) successMessage else message
                }
                viewModelScope.launch {
                    useCases.saveMessage(successMessage)
                }

                if (pendingDeckCreation != null && isDeckDraftPreviewMessage(normalizedMarkdown)) {
                    _pendingDeckDraftMessageId.value = aiMessageId
                } else if (pendingDeckCreation == null) {
                    _pendingDeckDraftMessageId.value = null
                }
            }

            result.onFailure { exception ->
                val errorMessage = ChatMessage(
                    id = aiMessageId,
                    message = "Xin lỗi, có lỗi xảy ra: ${exception.message}",
                    sender = "bot",
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = _messages.value.map { message ->
                    if (message.id == aiMessageId) errorMessage else message
                }
                viewModelScope.launch {
                    useCases.saveMessage(errorMessage)
                }
                _error.value = exception.message ?: "Unknown error"
            }

            _isLoading.value = false
        }
    }

    fun confirmPendingDeckCreationFromUi() {
        if (_isLoading.value || pendingDeckCreation == null) return
        executeQuickDeckAction(confirm = true)
    }

    fun cancelPendingDeckCreationFromUi() {
        if (_isLoading.value || pendingDeckCreation == null) return
        executeQuickDeckAction(confirm = false)
    }

    private fun executeQuickDeckAction(confirm: Boolean) {
        viewModelScope.launch {
            _isLoading.value = true

            val aiMessageId = UUID.randomUUID().toString()
            val sendingMessage = ChatMessage(
                id = aiMessageId,
                message = "Đang xử lý yêu cầu...",
                sender = "bot",
                timestamp = System.currentTimeMillis()
            )
            _messages.value = _messages.value + sendingMessage
            useCases.saveMessage(sendingMessage)

            val result = runCatching {
                if (confirm) confirmPendingDeckCreation() else cancelPendingDeckCreation()
            }.fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(it) }
            )

            result.onSuccess { response ->
                val normalizedMarkdown = MarkdownConverter.normalizeForChatDisplay(response)
                val successMessage = ChatMessage(
                    id = aiMessageId,
                    message = normalizedMarkdown,
                    sender = "bot",
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = _messages.value.map { message ->
                    if (message.id == aiMessageId) successMessage else message
                }
                viewModelScope.launch {
                    useCases.saveMessage(successMessage)
                }
            }

            result.onFailure { exception ->
                val errorMessage = ChatMessage(
                    id = aiMessageId,
                    message = "Xin lỗi, có lỗi xảy ra: ${exception.message}",
                    sender = "bot",
                    timestamp = System.currentTimeMillis()
                )
                _messages.value = _messages.value.map { message ->
                    if (message.id == aiMessageId) errorMessage else message
                }
                viewModelScope.launch {
                    useCases.saveMessage(errorMessage)
                }
                _error.value = exception.message ?: "Unknown error"
            }

            _isLoading.value = false
        }
    }

    private suspend fun sendStandardAiMessage(
        userText: String,
        historyForRequest: List<ChatMessage>
    ): Result<String> {
        val contextMessage = buildDeckContext(userText)
        return useCases.sendMessage(
            userMessage = userText,
            history = historyForRequest,
            contextMessage = contextMessage
        )
    }

    private suspend fun prepareDeckDraft(
        userText: String,
        historyForRequest: List<ChatMessage>
    ): String {
        val draftContext = buildDeckDraftContext(userText)

        val firstAttempt = useCases.sendMessage(
            userMessage = userText,
            history = historyForRequest,
            contextMessage = draftContext
        )

        var draft = parseDeckDraft(firstAttempt.getOrElse { throw it })

        if (draft == null) {
            val retryContext = "$draftContext\n\nBẮT BUỘC: Chỉ trả về duy nhất JSON hợp lệ, không markdown, không giải thích."
            val retryAttempt = useCases.sendMessage(
                userMessage = userText,
                history = historyForRequest,
                contextMessage = retryContext
            )
            draft = parseDeckDraft(retryAttempt.getOrElse { throw it })
        }

        val sanitizedDraft = sanitizeDeckDraft(draft)
            ?: return buildDraftFallbackHelpMessage()

        pendingDeckCreation = PendingDeckCreation(
            draft = sanitizedDraft,
            sourcePrompt = userText
        )

        return buildDeckDraftPreviewMessage(sanitizedDraft)
    }

    private fun sanitizeDeckDraft(rawDraft: DeckDraft?): DeckDraft? {
        if (rawDraft == null) return null

        val sanitizedCards = rawDraft.cards
            .map {
                DeckCardDraft(
                    question = it.question.trim(),
                    answer = it.answer.trim()
                )
            }
            .filter { it.question.isNotBlank() && it.answer.isNotBlank() }
            .distinctBy { "${it.question.lowercase()}::${it.answer.lowercase()}" }
            .take(MAX_DRAFT_CARDS)

        if (sanitizedCards.size < MIN_DRAFT_CARDS) return null

        val targetSize = when {
            rawDraft.requestedCardCount == null -> minOf(DEFAULT_DRAFT_CARDS, sanitizedCards.size)
            rawDraft.requestedCardCount < MIN_DRAFT_CARDS -> MIN_DRAFT_CARDS
            rawDraft.requestedCardCount > MAX_DRAFT_CARDS -> MAX_DRAFT_CARDS
            else -> rawDraft.requestedCardCount
        }

        val finalizedCards = sanitizedCards.take(targetSize)
        if (finalizedCards.size < MIN_DRAFT_CARDS) return null

        val deckName = rawDraft.name.trim().ifBlank { "Bộ thẻ mới" }
        val deckDescription = rawDraft.description.trim().ifBlank {
            "Bộ thẻ được tạo từ yêu cầu trong AI Chat"
        }

        return DeckDraft(
            name = deckName,
            description = deckDescription,
            cards = finalizedCards,
            language = rawDraft.language?.trim()?.ifBlank { null },
            difficulty = rawDraft.difficulty?.trim()?.ifBlank { null },
            suggestedCategoryId = rawDraft.suggestedCategoryId?.trim()?.ifBlank { null },
            requestedCardCount = targetSize
        )
    }

    private fun buildDeckDraftPreviewMessage(draft: DeckDraft): String {
        val previewCards = draft.cards.take(5)
            .mapIndexed { index, card ->
                "${index + 1}. **Q:** ${card.question}\n   **A:** ${card.answer}"
            }
            .joinToString("\n")

        val moreCardsLine = if (draft.cards.size > 5) {
            "\n... và ${draft.cards.size - 5} thẻ khác"
        } else {
            ""
        }

        val languageLine = draft.language?.let { "\n**Ngôn ngữ:** $it" } ?: ""
        val difficultyLine = draft.difficulty?.let { "\n**Mức độ:** $it" } ?: ""

        return """
Mình đã chuẩn bị xong bản nháp bộ thẻ:

**Tên bộ thẻ:** ${draft.name}
**Mô tả:** ${draft.description}
**Số thẻ:** ${draft.cards.size}$languageLine$difficultyLine

**Xem nhanh một vài thẻ:**
$previewCards$moreCardsLine

Nếu bạn đồng ý tạo bộ thẻ này, hãy nhắn: `xac nhan tao deck`
Nếu muốn hủy, hãy nhắn: `huy tao deck`
Bạn cũng có thể bấm trực tiếp nút **Xác nhận tạo** hoặc **Hủy** ngay bên dưới tin nhắn này.
Bạn cũng có thể yêu cầu chỉnh sửa lại nội dung trước khi xác nhận.
        """.trim()
    }

    private fun buildDraftFallbackHelpMessage(): String {
        return """
Mình có thể tạo bộ thẻ cho bạn, nhưng chưa dựng được bản nháp hợp lệ.

Bạn thử gửi theo mẫu này để mình tạo chính xác hơn:
- Chủ đề
- Số lượng thẻ (5-50)
- Mức độ (cơ bản/trung bình/nâng cao)
- Ngôn ngữ

Ví dụ: `Tạo bộ thẻ 15 thẻ tiếng Anh chủ đề du lịch, mức độ A2`.
        """.trim()
    }

    private suspend fun confirmPendingDeckCreation(): String {
        val pending = pendingDeckCreation
            ?: return "Hiện chưa có bản nháp nào để tạo. Bạn hãy yêu cầu mình tạo bộ thẻ trước nhé."

        val draft = pending.draft
        val categoryId = resolveCategoryId(draft.suggestedCategoryId)

        val deckResult = addDeckUseCase(
            name = draft.name,
            description = draft.description,
            isPublic = true,
            categoryId = categoryId
        )

        val createdDeck = deckResult.getOrElse { error ->
            return "Không thể tạo bộ thẻ lúc này: ${error.message ?: "Unknown error"}. Bạn có thể thử lại bằng `xac nhan tao deck`."
        }

        var createdCards = 0
        val failedCards = mutableListOf<String>()

        draft.cards.forEachIndexed { index, card ->
            val addResult = addFlashCardUseCase(
                question = card.question,
                answer = card.answer,
                deckId = createdDeck.id
            )
            if (addResult.isSuccess) {
                createdCards += 1
            } else {
                val reason = addResult.exceptionOrNull()?.message ?: "Unknown error"
                failedCards.add("Thẻ ${index + 1}: $reason")
            }
        }

        pendingDeckCreation = null
        _pendingDeckDraftMessageId.value = null

        return if (failedCards.isEmpty()) {
            """
Đã tạo thành công bộ thẻ **${createdDeck.name}**.

- Deck ID: `${createdDeck.id}`
- Số thẻ đã tạo: **$createdCards/${draft.cards.size}**

Bạn có thể vào danh sách Deck để học ngay.
            """.trim()
        } else {
            val failPreview = failedCards.take(3).joinToString("\n") { "- $it" }
            val moreFail = if (failedCards.size > 3) "\n- ... và ${failedCards.size - 3} lỗi khác" else ""

            """
Đã tạo bộ thẻ **${createdDeck.name}** nhưng có lỗi một phần.

- Deck ID: `${createdDeck.id}`
- Số thẻ tạo thành công: **$createdCards/${draft.cards.size}**

Chi tiết lỗi:
$failPreview$moreFail
            """.trim()
        }
    }

    private fun cancelPendingDeckCreation(): String {
        val pending = pendingDeckCreation
        pendingDeckCreation = null
        _pendingDeckDraftMessageId.value = null

        return if (pending == null) {
            "Hiện không có bản nháp nào đang chờ xác nhận."
        } else {
            "Đã hủy bản nháp tạo bộ thẻ **${pending.draft.name}**. Khi cần, bạn chỉ cần yêu cầu tạo lại."
        }
    }

    private fun isDeckDraftPreviewMessage(text: String): Boolean {
        val normalized = normalizeCommandText(text)
        return normalized.contains("xac nhan tao deck") && normalized.contains("huy tao deck")
    }

    private suspend fun resolveCategoryId(preferredCategoryId: String?): String {
        if (!preferredCategoryId.isNullOrBlank()) {
            cachedDefaultCategoryId = preferredCategoryId
            return preferredCategoryId
        }

        if (!cachedDefaultCategoryId.isNullOrBlank()) {
            return cachedDefaultCategoryId!!
        }

        val categories = getAllCategoriesUseCase.invoke().getOrNull().orEmpty()
        val categoryIdFromApi = categories.firstOrNull()?.id
        if (!categoryIdFromApi.isNullOrBlank()) {
            cachedDefaultCategoryId = categoryIdFromApi
            return categoryIdFromApi
        }

        val categoryIdFromDeck = exploreDecksUseCase.invoke()
            .getOrNull()
            .orEmpty()
            .mapNotNull { it.categoryId.takeIf { id -> id.isNotBlank() } }
            .firstOrNull()

        if (!categoryIdFromDeck.isNullOrBlank()) {
            cachedDefaultCategoryId = categoryIdFromDeck
            return categoryIdFromDeck
        }

        return "general"
    }

    private fun shouldCreateDeckDraft(userText: String): Boolean {
        val normalized = normalizeCommandText(userText)

        val createKeywords = listOf(
            "tao", "create", "make", "generate", "soan"
        )
        val deckKeywords = listOf(
            "bo the", "deck", "flashcard", "the hoc"
        )

        return createKeywords.any { normalized.contains(it) } &&
            deckKeywords.any { normalized.contains(it) }
    }

    private fun isDeckCreationConfirmation(userText: String): Boolean {
        val normalized = normalizeCommandText(userText)
        val confirmations = listOf(
            "xac nhan", "dong y", "yes", "ok", "oke", "confirm", "tao ngay", "tao di"
        )
        return confirmations.any { normalized == it || normalized.contains(it) }
    }

    private fun isDeckCreationCancellation(userText: String): Boolean {
        val normalized = normalizeCommandText(userText)
        val cancellations = listOf(
            "huy", "khong tao", "cancel", "dung tao", "thoi"
        )
        return cancellations.any { normalized == it || normalized.contains(it) }
    }

    private fun normalizeCommandText(text: String): String {
        val lowered = text.lowercase().trim()
        val withoutDiacritics = Normalizer.normalize(lowered, Normalizer.Form.NFD)
            .replace(Regex("\\p{Mn}+"), "")
            .replace("đ", "d")

        return withoutDiacritics
            .replace(Regex("[^a-z0-9\\s]"), " ")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private suspend fun buildDeckDraftContext(userText: String): String {
        val categoryHints = getAllCategoriesUseCase.invoke()
            .getOrNull()
            .orEmpty()
            .take(10)
            .joinToString(" | ") { "${it.id}:${it.name}" }

        val relatedDeckContext = buildDeckContext(userText)

        val categoryHintText = if (categoryHints.isBlank()) {
            "(không có category hint)"
        } else {
            categoryHints
        }

        val relatedDeckHint = relatedDeckContext
            ?.take(1000)
            ?.let { "\nNgữ cảnh deck liên quan:\n$it" }
            ?: ""

        return """
Bạn là trợ lý tạo JSON cho tính năng tạo bộ thẻ trong app FlashCard.
Yêu cầu: Phân tích câu hỏi người dùng và trả về DUY NHẤT một JSON object hợp lệ theo schema:
{
  "name": "string",
  "description": "string",
  "language": "string",
  "difficulty": "string",
  "cardCount": number,
  "suggestedCategoryId": "string",
  "cards": [
    { "question": "string", "answer": "string" }
  ]
}

Quy tắc bắt buộc:
- Chỉ trả về JSON object, không markdown, không giải thích.
- Tạo từ 5 đến 20 thẻ, mặc định 10 nếu user không chỉ định.
- Nội dung thẻ ngắn gọn, đúng chủ đề, dễ học.
- Nếu user không nêu rõ category, chọn category phù hợp nhất từ danh sách.
- Danh sách category hợp lệ: $categoryHintText
$relatedDeckHint
        """.trim()
    }

    private fun parseDeckDraft(raw: String): DeckDraft? {
        val jsonText = extractJsonObject(raw) ?: return null

        return try {
            val jsonElement = JsonParser.parseString(jsonText)
            if (!jsonElement.isJsonObject) return null

            val root = jsonElement.asJsonObject
            val cardsArray = root.getAsJsonArrayOrNull("cards") ?: JsonArray()

            val cards = cardsArray
                .mapNotNull { element ->
                    if (!element.isJsonObject) return@mapNotNull null
                    val obj = element.asJsonObject
                    val question = obj.readString("question", "front", "term", "q")
                    val answer = obj.readString("answer", "back", "definition", "a")
                    if (question.isNullOrBlank() || answer.isNullOrBlank()) return@mapNotNull null
                    DeckCardDraft(question = question, answer = answer)
                }

            if (cards.isEmpty()) return null

            DeckDraft(
                name = root.readString("name", "deckName", "title").orEmpty(),
                description = root.readString("description", "deckDescription", "summary").orEmpty(),
                cards = cards,
                language = root.readString("language", "lang"),
                difficulty = root.readString("difficulty", "level"),
                suggestedCategoryId = root.readString("suggestedCategoryId", "categoryId"),
                requestedCardCount = root.readInt("cardCount", "count", "totalCards")
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun extractJsonObject(text: String): String? {
        val fromCodeBlock = JSON_CODE_BLOCK_REGEX.find(text)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()

        if (!fromCodeBlock.isNullOrBlank()) {
            return fromCodeBlock
        }

        val firstBrace = text.indexOf('{')
        val lastBrace = text.lastIndexOf('}')
        if (firstBrace >= 0 && lastBrace > firstBrace) {
            return text.substring(firstBrace, lastBrace + 1)
        }

        return null
    }

    private fun JsonObject.getAsJsonArrayOrNull(key: String): JsonArray? {
        if (!has(key)) return null
        val element = get(key)
        if (!element.isJsonArray) return null
        return element.asJsonArray
    }

    private fun JsonObject.readString(vararg keys: String): String? {
        for (key in keys) {
            if (!has(key)) continue
            val element = get(key)
            if (element.isJsonNull) continue
            if (!element.isJsonPrimitive) continue
            return element.asString?.trim()?.takeIf { it.isNotBlank() }
        }
        return null
    }

    private fun JsonObject.readInt(vararg keys: String): Int? {
        for (key in keys) {
            if (!has(key)) continue
            val element = get(key)
            if (element.isJsonNull) continue

            val value = when {
                element.isJsonPrimitive && element.asJsonPrimitive.isNumber -> element.asInt
                element.isJsonPrimitive && element.asJsonPrimitive.isString -> {
                    element.asString.toIntOrNull()
                }
                else -> null
            }

            if (value != null) return value
        }
        return null
    }

    private suspend fun buildDeckContext(userText: String): String? {
        val decksResult = exploreDecksUseCase.invoke()
        if (decksResult.isFailure) return null

        val decks = decksResult.getOrNull().orEmpty()
        if (decks.isEmpty()) return null

        val matchingDecks = DeckMatchingHelper.findMatchingDecks(
            query = userText,
            availableDecks = decks
        )

        return DeckMatchingHelper.buildAiDeckContext(
            query = userText,
            allDecks = decks,
            matchingDecks = matchingDecks
        )
    }

    /**
     * Xóa tất cả tin nhắn
     */
    fun clearChat() {
        pendingDeckCreation = null
        _pendingDeckDraftMessageId.value = null
        _messages.value = emptyList()
        _error.value = null
        viewModelScope.launch {
            useCases.clearMessages()
        }
    }

    /**
     * Xóa tin nhắn cụ thể
     */
    fun deleteMessage(messageId: String) {
        val messageToDelete = _messages.value.find { it.id == messageId }
        _messages.value = _messages.value.filter { it.id != messageId }

        if (messageToDelete != null) {
            viewModelScope.launch {
                useCases.deleteMessage(messageToDelete.id)
            }
        }
    }

    /**
     * Thử lại tin nhắn cuối cùng
     */
    fun retryLastMessage() {
        val lastUserMessage = _messages.value.findLast { it.sender == "user" }
        if (lastUserMessage != null) {
            sendMessage(lastUserMessage.message)
        }
    }
}


class ChatAIViewModelFactory(
    private val useCases: ChatUseCases,
    private val exploreDecksUseCase: ExploreDecksUseCase,
    private val addDeckUseCase: AddDeckUseCase,
    private val addFlashCardUseCase: AddFlashCardUseCase,
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ChatAIViewModel(
            useCases = useCases,
            exploreDecksUseCase = exploreDecksUseCase,
            addDeckUseCase = addDeckUseCase,
            addFlashCardUseCase = addFlashCardUseCase,
            getAllCategoriesUseCase = getAllCategoriesUseCase
        ) as T
    }
}
