package com.example.flashcardapp.utils

import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node

/**
 * Utility class để convert markdown sang plain text hoặc HTML
 * Sử dụng thư viện Flexmark
 */
object MarkdownConverter {
    
    private val parser = Parser.builder().build()
    private val htmlRenderer = HtmlRenderer.builder().build()

    /**
     * Convert markdown string sang plain text (loại bỏ tất cả formatting markdown)
     * 
     * @param markdownText: Chuỗi markdown cần convert
     * @return: Plain text không có markdown formatting
     * 
     * Ví dụ:
     * Input: "# Hello\n**Bold** *italic* `code`"
     * Output: "Hello\nBold italic code"
     */
    fun markdownToPlainText(markdownText: String): String {
        return try {
            val document: Node = parser.parse(markdownText)
            val html = htmlRenderer.render(document)
            
            // Loại bỏ HTML tags để lấy plain text
            val plainText = html
                .replace(Regex("<[^>]*>"), "") // Loại bỏ HTML tags
                .replace(Regex("&nbsp;"), " ") // Thay thế HTML entity
                .replace(Regex("&lt;"), "<")
                .replace(Regex("&gt;"), ">")
                .replace(Regex("&amp;"), "&")
                .replace(Regex("&quot;"), "\"") // Loại bỏ &quot; (dấu ngoặc kép)
                .replace(Regex("&#\\d+;"), " ") // Loại bỏ numeric HTML entities
                .trim()
            
            plainText
        } catch (e: Exception) {
            // Nếu parse markdown thất bại, trả về text gốc
            markdownText
        }
    }

    /**
     * Loại bỏ tất cả markdown formatting nhưng giữ lại cấu trúc văn bản
     * (line breaks, spacing, v.v.)
     * 
     * @param markdownText: Chuỗi markdown cần clean
     * @return: Text đã được clean
     */
    fun stripMarkdownFormatting(markdownText: String): String {
        return markdownText
            .replace(Regex("#{1,6}\\s+"), "") // Loại bỏ headings (# ## ### etc)
            .replace(Regex("\\*{2}([^*]+)\\*{2}"), "$1") // Loại bỏ **bold**
            .replace(Regex("_{2}([^_]+)_{2}"), "$1") // Loại bỏ __bold__
            .replace(Regex("\\*([^*]+)\\*"), "$1") // Loại bỏ *italic*
            .replace(Regex("_([^_]+)_"), "$1") // Loại bỏ _italic_
            .replace(Regex("`([^`]+)`"), "$1") // Loại bỏ `code`
            .replace(Regex("```[\\s\\S]*?```"), "") // Loại bỏ code blocks
            .replace(Regex("\\[([^\\]]+)\\]\\([^)]+\\)"), "$1") // Loại bỏ links [text](url) -> text
            .replace(Regex("!\\[([^\\]]*)\\]\\([^)]+\\)"), "$1") // Loại bỏ images
            .replace(Regex("^\\s*[-*+]\\s+", RegexOption.MULTILINE), "") // Loại bỏ list markers
            .replace(Regex("^\\s*\\d+\\.\\s+", RegexOption.MULTILINE), "") // Loại bỏ numbered lists
            .replace(Regex("^>\\s+", RegexOption.MULTILINE), "") // Loại bỏ blockquotes
            .replace(Regex("\\n\\n+"), "\n\n") // Normalize multiple newlines
            .trim()
    }
}



