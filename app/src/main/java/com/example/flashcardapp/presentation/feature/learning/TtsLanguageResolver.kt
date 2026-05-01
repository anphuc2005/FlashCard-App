package com.example.flashcardapp.presentation.feature.learning

import android.speech.tts.TextToSpeech
import java.util.Locale

object TtsLanguageResolver {

    private val VIETNAMESE_LOCALE = Locale.forLanguageTag("vi-VN")

    private val VIETNAMESE_DIACRITIC_REGEX = Regex(
        "[\\u0102\\u0103\\u00C2\\u00E2\\u0110\\u0111\\u00CA\\u00EA\\u00D4\\u00F4\\u01A0\\u01A1\\u01AF\\u01B0\\u1EA0-\\u1EF9]"
    )
    private val ASCII_WORD_REGEX = Regex("[A-Za-z']+")

    private val ENGLISH_HINT_WORDS = setOf(
        "the", "is", "are", "was", "were", "be", "been", "being",
        "a", "an", "and", "or", "of", "to", "in", "on", "at",
        "for", "from", "with", "without", "what", "when", "where",
        "why", "how", "which", "who", "whom", "this", "that", "these",
        "those", "it", "its", "their", "there", "have", "has", "had",
        "do", "does", "did", "not", "can", "could", "will", "would",
        "should", "if", "then", "than", "into", "about"
    )

    private val VIETNAMESE_HINT_WORDS = setOf(
        "la", "va", "cua", "cho", "voi", "khong", "co", "toi", "ban",
        "mot", "nhieu", "nhung", "duoc", "trong", "tren", "duoi",
        "tai", "khi", "sao", "nhu", "the", "nao", "gi", "day", "do"
    )

    private val JAPANESE_BLOCKS = setOf(
        Character.UnicodeBlock.HIRAGANA,
        Character.UnicodeBlock.KATAKANA,
        Character.UnicodeBlock.KATAKANA_PHONETIC_EXTENSIONS
    )

    private val KOREAN_BLOCKS = setOf(
        Character.UnicodeBlock.HANGUL_JAMO,
        Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO,
        Character.UnicodeBlock.HANGUL_SYLLABLES
    )

    private val HAN_BLOCKS = setOf(
        Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS,
        Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A,
        Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B,
        Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
    )

    fun resolveBestSupportedLocale(
        tts: TextToSpeech,
        text: String,
        defaultLocale: Locale = Locale.getDefault()
    ): Locale {
        val preferred = detectPreferredLocale(text, defaultLocale)
        val candidates = linkedSetOf<Locale>()
        candidates.add(preferred)
        candidates.add(defaultLocale)
        candidates.add(Locale.US)
        candidates.add(VIETNAMESE_LOCALE)

        return candidates.firstOrNull { isLocaleSupported(tts, it) } ?: defaultLocale
    }

    fun isLanguageResultSupported(result: Int): Boolean {
        return result >= TextToSpeech.LANG_AVAILABLE
    }

    private fun isLocaleSupported(tts: TextToSpeech, locale: Locale): Boolean {
        val availability = tts.isLanguageAvailable(locale)
        return isLanguageResultSupported(availability)
    }

    private fun detectPreferredLocale(text: String, defaultLocale: Locale): Locale {
        val normalized = text.trim()
        if (normalized.isEmpty()) return defaultLocale

        if (containsAnyBlock(normalized, JAPANESE_BLOCKS)) return Locale.JAPAN
        if (containsAnyBlock(normalized, KOREAN_BLOCKS)) return Locale.KOREA
        if (containsAnyBlock(normalized, HAN_BLOCKS)) return Locale.SIMPLIFIED_CHINESE
        if (VIETNAMESE_DIACRITIC_REGEX.containsMatchIn(normalized)) return VIETNAMESE_LOCALE

        val tokens = ASCII_WORD_REGEX.findAll(normalized.lowercase(Locale.ROOT))
            .map { it.value }
            .toList()
        val englishHits = tokens.count { it in ENGLISH_HINT_WORDS }
        val vietnameseHits = tokens.count { it in VIETNAMESE_HINT_WORDS }

        if (vietnameseHits > englishHits) return VIETNAMESE_LOCALE
        if (englishHits > vietnameseHits) return Locale.US

        return if (tokens.isNotEmpty()) Locale.US else defaultLocale
    }

    private fun containsAnyBlock(text: String, blocks: Set<Character.UnicodeBlock>): Boolean {
        val codePoints = text.codePoints().iterator()
        while (codePoints.hasNext()) {
            val block = Character.UnicodeBlock.of(codePoints.nextInt()) ?: continue
            if (block in blocks) return true
        }
        return false
    }
}
