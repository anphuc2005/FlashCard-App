package com.example.flashcardapp.utils

import com.vladsch.flexmark.ext.tables.TablesExtension
import com.vladsch.flexmark.html.HtmlRenderer
import com.vladsch.flexmark.parser.Parser
import com.vladsch.flexmark.util.ast.Node
import com.vladsch.flexmark.util.data.MutableDataSet

/**
 * Utility class để convert markdown sang plain text hoặc HTML
 * Sử dụng thư viện Flexmark
 */
object MarkdownConverter {
    
    private val options = MutableDataSet().set(
        Parser.EXTENSIONS, listOf(TablesExtension.create())
    )
    
    private val parser = Parser.builder(options).build()
    private val htmlRenderer = HtmlRenderer.builder(options).build()
    private val tableSeparatorRegex = Regex("^\\s*\\|?\\s*:?-{3,}:?\\s*(\\|\\s*:?-{3,}:?\\s*)+\\|?\\s*$")
    private val codeFenceRegex = Regex("(?m)^\\s*```")

    /**
     * Convert markdown string sang thẻ HTML (hỗ trợ cả Table)
     */
    fun markdownToHtml(markdownText: String): String {
        return try {
            val normalizedMarkdown = normalizeForChatDisplay(markdownText)
            val document: Node = parser.parse(normalizedMarkdown)
            htmlRenderer.render(document)
        } catch (e: Exception) {
            markdownText
        }
    }

    /**
     * Chuẩn hóa Markdown trước khi render trên Android TextView.
     * - Đóng code fence bị hở
     * - Làm phẳng markdown table thành list để tránh lỗi hiển thị bảng
     * - Chuẩn hóa line break
     */
    fun normalizeForChatDisplay(markdownText: String): String {
        if (markdownText.isBlank()) return markdownText

        var normalized = markdownText
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .trim()

        normalized = closeUnclosedCodeFence(normalized)
        normalized = flattenMarkdownTables(normalized)

        return normalized
            .replace(Regex("\\n{3,}"), "\n\n")
            .trim()
    }

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

    private fun closeUnclosedCodeFence(markdownText: String): String {
        val fenceCount = codeFenceRegex.findAll(markdownText).count()
        return if (fenceCount % 2 == 0) markdownText else "$markdownText\n```"
    }

    private fun flattenMarkdownTables(markdownText: String): String {
        val lines = markdownText.lines()
        if (lines.isEmpty()) return markdownText

        val output = mutableListOf<String>()
        var index = 0
        var inCodeBlock = false

        while (index < lines.size) {
            val currentLine = lines[index]
            if (currentLine.trim().startsWith("```")) {
                inCodeBlock = !inCodeBlock
                output.add(currentLine)
                index++
                continue
            }

            val nextLine = lines.getOrNull(index + 1)

            if (!inCodeBlock &&
                isLikelyTableRow(currentLine) &&
                nextLine != null &&
                tableSeparatorRegex.matches(nextLine.trim())
            ) {
                val tableBlock = mutableListOf<String>()
                tableBlock.add(currentLine)
                tableBlock.add(nextLine)
                index += 2

                while (index < lines.size && isLikelyTableRow(lines[index]) && !lines[index].trim().startsWith("```")) {
                    tableBlock.add(lines[index])
                    index++
                }

                output.add(convertTableToList(tableBlock))
            } else {
                output.add(currentLine)
                index++
            }
        }

        return output.joinToString("\n")
    }

    private fun isLikelyTableRow(line: String): Boolean {
        val trimmed = line.trim()
        if (trimmed.isBlank()) return false
        return trimmed.count { it == '|' } >= 2
    }

    private fun parseTableCells(line: String): List<String> {
        return line.trim()
            .trim('|')
            .split('|')
            .map { it.trim().replace("\\|", "|") }
    }

    private fun convertTableToList(tableLines: List<String>): String {
        if (tableLines.size < 2) return tableLines.joinToString("\n")

        val headers = parseTableCells(tableLines.first())
        val rows = tableLines.drop(2).map(::parseTableCells)

        if (headers.isEmpty() || rows.isEmpty()) {
            return tableLines.joinToString("\n")
        }

        val builder = StringBuilder()
        builder.append("**Bang du lieu:**\n")

        rows.forEach { row ->
            val pairs = headers.indices.mapNotNull { columnIndex ->
                val header = headers[columnIndex].takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val value = row.getOrElse(columnIndex) { "" }.trim()
                if (value.isBlank()) null else "**$header**: $value"
            }
            if (pairs.isNotEmpty()) {
                builder.append("- ")
                builder.append(pairs.joinToString(" | "))
                builder.append("\n")
            }
        }

        return builder.toString().trimEnd()
    }
}
