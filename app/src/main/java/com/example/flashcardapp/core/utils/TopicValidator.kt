package com.example.flashcardapp.utils

/**
 * Utility class để validate câu hỏi/tin nhắn có liên quan đến FlashCard app hay không
 */
object TopicValidator {

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

    /**
     * Kiểm tra câu hỏi/tin nhắn có liên quan đến FlashCard app hay không
     * 
     * @param message: Tin nhắn/câu hỏi từ người dùng
     * @return: true nếu liên quan đến FlashCard, false nếu ngoài lề
     */
    fun isFlashCardRelated(message: String): Boolean {
        val lowerMessage = message.lowercase().trim()
        
        // Nếu rỗng hoặc quá ngắn, coi như không liên quan
        if (lowerMessage.length < 3) return false
        
        // Kiểm tra xem có chứa bất kỳ từ khóa FlashCard nào
        for (keyword in flashcardKeywords) {
            if (lowerMessage.contains(keyword)) {
                return true
            }
        }
        
        // Kiểm tra xem có liên quan đến các chủ đề học tập không
        // (người dùng có thể hỏi về nội dung học tập để tạo thẻ)
        for (topic in educationalTopics) {
            if (lowerMessage.contains(topic)) {
                return true
            }
        }
        
        return false
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
Xin lỗi, tôi chỉ có thể giúp bạn với các câu hỏi liên quan đến ứng dụng FlashCard. 

Tôi có thể hỗ trợ bạn trong:
• Tạo và quản lý các bộ thẻ
• Ôn tập từ vựng, khái niệm, công thức
• Giải thích kiến thức để bạn tạo thẻ học tập
• Gợi ý cách sử dụng ứng dụng hiệu quả

Vui lòng hỏi những câu liên quan đến học tập hoặc sử dụng ứng dụng!
        """.trim()
    }
}

