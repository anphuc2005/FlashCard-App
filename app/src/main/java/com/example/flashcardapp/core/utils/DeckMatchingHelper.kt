package com.example.flashcardapp.utils

import com.example.flashcardapp.domain.model.Deck

/**
 * Utility class để tìm các bộ thẻ phù hợp từ danh sách có sẵn
 * Giúp AI đưa ra gợi ý bộ thẻ nếu người dùng hỏi về các chủ đề
 */
object DeckMatchingHelper {

    data class DeckRecommendation(
        val deck: Deck,
        val score: Int,
        val reasons: List<String>
    )

    private val tokenRegex = Regex("[\\p{L}\\p{N}]{2,}")
    private val stopWords = setOf(
        "và", "là", "của", "cho", "với", "các", "những", "để", "trong",
        "the", "and", "for", "with", "that", "this"
    )

    /**
     * Tìm các bộ thẻ có liên quan dựa trên từ khóa trong câu hỏi
     * 
     * @param query: Câu hỏi hoặc từ khóa tìm kiếm
     * @param availableDecks: Danh sách bộ thẻ có sẵn trong app
     * @return: Danh sách các bộ thẻ phù hợp (sắp xếp theo mức độ liên quan)
     */
    fun findMatchingDecks(
        query: String,
        availableDecks: List<Deck>,
        maxResults: Int = 5
    ): List<DeckRecommendation> {
        if (query.isBlank() || availableDecks.isEmpty()) {
            return emptyList()
        }

        val lowerQuery = query.lowercase()
        val queryTokens = tokenize(lowerQuery)
        if (queryTokens.isEmpty()) return emptyList()

        val recommendations = mutableListOf<DeckRecommendation>()
        availableDecks.forEach { deck ->
            val deckName = deck.name.lowercase()
            val deckDesc = deck.description?.lowercase() ?: ""
            var score = 0
            val reasons = mutableListOf<String>()

            if (deckName.contains(lowerQuery) && lowerQuery.length >= 4) {
                score += 18
                reasons.add("Tên deck khớp gần như toàn bộ yêu cầu")
            }

            queryTokens.forEach { token ->
                if (deckName.contains(token)) {
                    score += 8
                    reasons.add("Tên deck chứa từ '$token'")
                }
                if (deckDesc.contains(token)) {
                    score += 4
                    reasons.add("Mô tả deck có '$token'")
                }
            }

            val matchedTokenCount = queryTokens.count { token ->
                deckName.contains(token) || deckDesc.contains(token)
            }
            if (matchedTokenCount >= 2) {
                score += 6
            }

            if (score > 0) {
                recommendations.add(
                    DeckRecommendation(
                        deck = deck,
                        score = score,
                        reasons = reasons.distinct().take(3)
                    )
                )
            }
        }

        return recommendations
            .sortedByDescending { it.score }
            .take(maxResults)
    }

    /**
     * Tạo tin nhắn gợi ý dựa trên các bộ thẻ tìm được
     * 
     * @param matchingDecks: Danh sách bộ thẻ phù hợp
     * @param maxSuggestions: Số lượng gợi ý tối đa (mặc định 3)
     * @return: Tin nhắn gợi ý dạng text
     */
    fun buildSuggestionMessage(
        matchingDecks: List<DeckRecommendation>,
        maxSuggestions: Int = 3
    ): String {
        if (matchingDecks.isEmpty()) {
            return ""
        }

        val suggestions = matchingDecks.take(maxSuggestions)
        val messageBuilder = StringBuilder()

        messageBuilder.append("\n\n**Bộ thẻ có sẵn để bạn tham khảo:**\n")

        suggestions.forEachIndexed { index, recommendation ->
            val deck = recommendation.deck
            messageBuilder.append("${index + 1}. **${deck.name}**")
            if (!deck.description.isNullOrBlank()) {
                messageBuilder.append(" - ${deck.description}")
            }
            val topReason = recommendation.reasons.firstOrNull()
            if (!topReason.isNullOrBlank()) messageBuilder.append(" _(Lý do: $topReason)_")
            messageBuilder.append("\n")
        }

        messageBuilder.append("\nBạn có thể mở bộ thẻ này để ôn tập hoặc tạo bộ thẻ mới tương tự!")

        return messageBuilder.toString()
    }

    /**
     * Tạo context message để bơm vào system prompt, giúp AI ưu tiên các deck liên quan.
     */
    fun buildAiDeckContext(
        query: String,
        allDecks: List<Deck>,
        matchingDecks: List<DeckRecommendation>,
        maxSuggestions: Int = 5
    ): String? {
        if (allDecks.isEmpty()) return null

        val recommendations = if (matchingDecks.isNotEmpty()) {
            matchingDecks.take(maxSuggestions)
        } else if (isTopicSearchQuery(query)) {
            allDecks.take(maxSuggestions).map {
                DeckRecommendation(
                    deck = it,
                    score = 1,
                    reasons = listOf("Deck phổ biến trong hệ thống")
                )
            }
        } else {
            emptyList()
        }

        if (recommendations.isEmpty()) return null

        val deckLines = recommendations.joinToString("\n") { recommendation ->
            val deck = recommendation.deck
            val summary = buildString {
                append("- [${deck.id}] ${deck.name}")
                if (!deck.description.isNullOrBlank()) {
                    append(": ${truncate(deck.description, 160)}")
                }
                if (recommendation.reasons.isNotEmpty()) {
                    val reasonsText = recommendation.reasons.joinToString("; ")
                    append(" | Lý do khớp: ${truncate(reasonsText, 180)}")
                }
            }
            summary
        }

        return """
Bạn đang hỗ trợ trong app FlashCard và có thể tham khảo các Deck có sẵn trên server.
Khi câu hỏi phù hợp, hãy ưu tiên đề xuất 1-3 deck liên quan nhất, nêu lý do ngắn gọn và nhắc người dùng có thể mở deck để học ngay.
Nếu chưa đủ chắc chắn, hãy hỏi 1 câu làm rõ trước khi đề xuất sâu hơn.

Deck gợi ý:
$deckLines
        """.trim()
    }

    /**
     * Kiểm tra xem người dùng có đang hỏi về một chủ đề cụ thể không
     * (để có thể gợi ý bộ thẻ liên quan)
     * 
     * @param query: Câu hỏi từ người dùng
     * @return: true nếu câu hỏi có vẻ như là tìm kiếm chủ đề
     */
    fun isTopicSearchQuery(query: String): Boolean {
        val lowerQuery = query.lowercase()
        
        val searchKeywords = listOf(
            "bộ thẻ", "deck", "có", "giống", "liên quan", "tương tự",
            "về", "về môn", "chủ đề", "topic", "học",
            "tìm", "search", "suggest", "gợi ý"
        )

        return searchKeywords.any { lowerQuery.contains(it) }
    }

    private fun tokenize(text: String): List<String> {
        return tokenRegex.findAll(text)
            .map { it.value.lowercase() }
            .filter { token -> token !in stopWords }
            .toList()
    }

    private fun truncate(text: String, maxLength: Int): String {
        val clean = text.trim()
        if (clean.length <= maxLength) return clean
        return clean.take(maxLength).trimEnd() + "..."
    }
}
