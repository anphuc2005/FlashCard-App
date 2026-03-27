package com.example.flashcardapp.utils

import com.example.flashcardapp.data.entity.DeckEntity

/**
 * Utility class để tìm các bộ thẻ phù hợp từ danh sách có sẵn
 * Giúp AI đưa ra gợi ý bộ thẻ nếu người dùng hỏi về các chủ đề
 */
object DeckMatchingHelper {

    /**
     * Tìm các bộ thẻ có liên quan dựa trên từ khóa trong câu hỏi
     * 
     * @param query: Câu hỏi hoặc từ khóa tìm kiếm
     * @param availableDecks: Danh sách bộ thẻ có sẵn trong app
     * @return: Danh sách các bộ thẻ phù hợp (sắp xếp theo mức độ liên quan)
     */
    fun findMatchingDecks(query: String, availableDecks: List<DeckEntity>): List<DeckEntity> {
        if (query.isBlank() || availableDecks.isEmpty()) {
            return emptyList()
        }

        val lowerQuery = query.lowercase()
        
        // Tạo bản đồ điểm số cho mỗi bộ thẻ
        val deckScores = mutableMapOf<String, Int>()

        for (deck in availableDecks) {
            val deckName = deck.name.lowercase()
            val deckDesc = deck.description?.lowercase() ?: ""
            var score = 0

            // Kiểm tra từ khóa trong tên bộ thẻ
            val queryWords = lowerQuery.split(Regex("[\\s\\p{P}]+"))
            
            for (word in queryWords) {
                if (word.length > 2) { // Chỉ xem xét từ có ít nhất 3 ký tự
                    // Nếu từ khóa chính xác xuất hiện trong tên
                    if (deckName.contains(word)) {
                        score += 10
                    }
                    // Nếu từ khóa xuất hiện trong mô tả
                    if (deckDesc.contains(word)) {
                        score += 5
                    }
                }
            }

            // Nếu có điểm, thêm vào bản đồ
            if (score > 0) {
                deckScores[deck.id] = score
            }
        }

        // Sắp xếp bộ thẻ theo điểm số giảm dần
        return deckScores.entries
            .sortedByDescending { it.value }
            .mapNotNull { entry -> availableDecks.find { it.id == entry.key } }
    }

    /**
     * Tạo tin nhắn gợi ý dựa trên các bộ thẻ tìm được
     * 
     * @param matchingDecks: Danh sách bộ thẻ phù hợp
     * @param maxSuggestions: Số lượng gợi ý tối đa (mặc định 3)
     * @return: Tin nhắn gợi ý dạng text
     */
    fun buildSuggestionMessage(
        matchingDecks: List<DeckEntity>,
        maxSuggestions: Int = 3
    ): String {
        if (matchingDecks.isEmpty()) {
            return ""
        }

        val suggestions = matchingDecks.take(maxSuggestions)
        val messageBuilder = StringBuilder()

        messageBuilder.append("\n\n📚 **Bộ thẻ có sẵn để bạn tham khảo:**\n")

        suggestions.forEachIndexed { index, deck ->
            messageBuilder.append("${index + 1}. **${deck.name}**")
            if (!deck.description.isNullOrBlank()) {
                messageBuilder.append(" - ${deck.description}")
            }
            messageBuilder.append("\n")
        }

        messageBuilder.append("\nBạn có thể mở bộ thẻ này để ôn tập hoặc tạo bộ thẻ mới tương tự!")

        return messageBuilder.toString()
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
}

