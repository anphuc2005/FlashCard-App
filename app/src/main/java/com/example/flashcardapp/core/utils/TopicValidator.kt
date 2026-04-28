package com.example.flashcardapp.utils

/**
 * Utility class để validate câu hỏi/tin nhắn có liên quan đến FlashCard app hay không
 */
object TopicValidator {

    enum class TopicDecision {
        RELATED,
        NEEDS_CLARIFICATION,
        OUT_OF_SCOPE
    }

    data class TopicRelevanceResult(
        val score: Int,
        val decision: TopicDecision,
        val detectedKeywords: List<String>
    )

    // Các từ khóa liên quan đến FlashCard
    private val flashcardKeywords = setOf(
        // Tính năng chính
        "flashcard", "flash card", "thẻ", "bộ thẻ", "deck",
        "ôn tập", "học tập", "ghi nhớ", "kiến thức",
        
        // Chức năng cụ thể
        "tạo thẻ", "thêm thẻ", "xóa thẻ", "sửa thẻ", "chỉnh sửa",
        "mặt trước", "mặt sau", "câu hỏi", "đáp án",
        "vuốt", "lật thẻ", "chạm", "ôn lại",
        
        // Nội dung học tập
        "từ vựng", "khái niệm", "công thức", "sự kiện",
        "định nghĩa", "giải thích", "ý nghĩa", "ngôn ngữ",
        
        // Quản lý bộ thẻ
        "bộ thẻ", "bộ học", "chủ đề", "môn học", "danh sách",
        "thống kê", "tiến độ", "đã thuộc", "cần ôn",
        
        // Ứng dụng
        "app", "ứng dụng", "android", "điện thoại", "màn hình",
        "giao diện", "cài đặt", "tính năng",
        
        // Chế độ học
        "spaced repetition", "lặp lại ngắt quãng", "ngẫu nhiên",
        "theo thứ tự", "đánh dấu",
        
        // Tiếng Anh
        "card", "cards", "study", "learning", "vocabulary",
        "question", "answer", "memorize", "practice",
        "revision", "exam", "quiz", "term", "definition"
    )

    // Các từ khóa chủ đề liên quan đến học tập
    private val educationalTopics = setOf(
        "toán", "math", "vật lý", "physics", "hóa học", "chemistry",
        "lịch sử", "history", "địa lý", "geography", "sinh học", "biology",
        "tiếng anh", "english", "tiếng việt", "vietnamese", "văn học",
        "khoa học", "science", "kinh tế", "economics", "quản lý",
        "lập trình", "programming", "code", "java", "kotlin", "python",
        "công nghệ", "technology", "kỹ năng", "skill"
    )

    // Các từ/ý định mang tính yêu cầu trợ giúp học tập
    private val learningIntentKeywords = setOf(
        "giúp", "hướng dẫn", "gợi ý", "đề xuất", "recommend", "suggest",
        "học gì", "ôn gì", "how to study", "how can i", "làm sao", "nên học",
        "tạo", "soạn", "review", "plan", "lộ trình", "practice"
    )

    // Dấu hiệu người dùng đang tiếp tục ngữ cảnh trước đó
    private val continuationSignals = setOf(
        "nó", "cái đó", "cái này", "tiếp", "tiếp theo", "chi tiết hơn", "mở rộng",
        "thêm", "ví dụ", "còn lại", "that", "it", "continue", "more details"
    )

    // Các chủ đề ngoài phạm vi thường gặp
    private val offTopicKeywords = setOf(
        "bóng đá", "football", "nba", "kpop", "drama", "gossip", "tử vi",
        "xổ số", "chứng khoán", "crypto", "bitcoin", "thời tiết", "weather",
        "chính trị", "politics", "cá cược", "bet", "game online"
    )

    private val stopWords = setOf(
        "và", "là", "của", "cho", "với", "các", "những", "để", "trong", "khi",
        "the", "and", "for", "with", "that", "this", "from", "into"
    )

    private val tokenRegex = Regex("[\\p{L}\\p{N}]{2,}")

    /**
     * Kiểm tra câu hỏi/tin nhắn có liên quan đến FlashCard app hay không
     * 
     * @param message: Tin nhắn/câu hỏi từ người dùng
     * @return: true nếu liên quan đến FlashCard, false nếu ngoài lề
     */
    fun isFlashCardRelated(message: String): Boolean {
        return evaluateRelevance(message).decision != TopicDecision.OUT_OF_SCOPE
    }

    /**
     * Đánh giá mức độ liên quan của câu hỏi dựa trên nội dung + ngữ cảnh trước đó.
     * Giúp tránh việc từ chối quá cứng khi người dùng đang nói tiếp một chủ đề học tập.
     */
    fun evaluateRelevance(
        message: String,
        conversationHistory: List<String> = emptyList()
    ): TopicRelevanceResult {
        val lowerMessage = message.lowercase().trim()
        if (lowerMessage.length < 2) {
            return TopicRelevanceResult(
                score = 0,
                decision = TopicDecision.OUT_OF_SCOPE,
                detectedKeywords = emptyList()
            )
        }

        val tokens = tokenize(lowerMessage)
        var score = 0
        val detected = linkedSetOf<String>()

        flashcardKeywords.forEach { keyword ->
            if (lowerMessage.contains(keyword)) {
                score += if (keyword.contains(" ")) 4 else 3
                detected.add(keyword)
            }
        }

        educationalTopics.forEach { topic ->
            if (lowerMessage.contains(topic)) {
                score += if (topic.contains(" ")) 3 else 2
                detected.add(topic)
            }
        }

        learningIntentKeywords.forEach { intent ->
            if (lowerMessage.contains(intent)) {
                score += 2
                detected.add(intent)
            }
        }

        if (tokens.size <= 2) {
            score -= 1
        }

        val hasOffTopicKeyword = offTopicKeywords.any { lowerMessage.contains(it) }
        if (hasOffTopicKeyword) {
            score -= 4
        }

        val recentContext = conversationHistory
            .takeLast(6)
            .joinToString(" ")
            .lowercase()

        val contextLooksRelevant = flashcardKeywords.any { recentContext.contains(it) } ||
            educationalTopics.any { recentContext.contains(it) }

        val isContinuation = continuationSignals.any { lowerMessage.contains(it) }
        if (contextLooksRelevant && isContinuation) {
            score += 3
        }

        // Nếu nội dung ngắn nhưng có ngữ cảnh liên quan trước đó, ưu tiên hỏi làm rõ thay vì từ chối.
        val decision = when {
            score >= 5 -> TopicDecision.RELATED
            score >= 2 -> TopicDecision.NEEDS_CLARIFICATION
            contextLooksRelevant && tokens.size <= 4 -> TopicDecision.NEEDS_CLARIFICATION
            else -> TopicDecision.OUT_OF_SCOPE
        }

        return TopicRelevanceResult(
            score = score.coerceIn(-10, 50),
            decision = decision,
            detectedKeywords = detected.toList()
        )
    }

    /**
     * Lấy danh sách các từ khóa FlashCard được tìm thấy trong message
     */
    fun getDetectedKeywords(message: String): List<String> {
        val lowerMessage = message.lowercase()
        return (flashcardKeywords + educationalTopics)
            .filter { lowerMessage.contains(it) }
    }

    /**
     * Tạo tin nhắn từ chối phù hợp khi câu hỏi ngoài lề
     */
    fun getOutOfTopicMessage(): String {
        return """
Xin lỗi, mình chỉ hỗ trợ tốt các chủ đề liên quan đến học tập và FlashCard.

Bạn có thể hỏi mình về:
- Tạo và quản lý bộ thẻ
- Gợi ý deck phù hợp mục tiêu học

Bạn thử mô tả mục tiêu học của bạn (môn học, trình độ, thời gian ôn) để mình hỗ trợ chính xác hơn nhé.
        """.trim()
    }

    fun getClarificationSystemHint(): String {
        return """
Nếu câu hỏi chưa đủ rõ, đừng từ chối ngay.
Hãy:
1) Nêu giả định bạn đang hiểu.
2) Hỏi đúng 1 câu làm rõ ngắn gọn.
3) Đưa luôn gợi ý tạm thời để người dùng vẫn tiếp tục được.
        """.trim()
    }

    private fun tokenize(text: String): List<String> {
        return tokenRegex.findAll(text)
            .map { it.value.lowercase() }
            .filter { token -> token !in stopWords }
            .toList()
    }
}
