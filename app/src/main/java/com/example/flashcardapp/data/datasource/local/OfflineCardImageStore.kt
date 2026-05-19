package com.example.flashcardapp.data.datasource.local

import android.content.Context
import android.webkit.MimeTypeMap
import com.example.flashcardapp.domain.model.FlashCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

class OfflineCardImageStore(
    context: Context,
    private val accountKeyProvider: () -> String?
) {
    private val rootDir = File(context.applicationContext.filesDir, ROOT_DIR)

    suspend fun cacheImage(card: FlashCard): String? = withContext(Dispatchers.IO) {
        val imageUrl = card.imageUrl?.trim()?.takeIf { it.startsWith("http") } ?: return@withContext card.localImagePath
        val accountDir = accountImageDir() ?: return@withContext card.localImagePath
        accountDir.mkdirs()

        val extension = resolveExtension(imageUrl)
        val target = File(accountDir, "${sanitize(card.id)}.$extension")
        if (target.exists() && target.length() > 0L) {
            return@withContext target.absolutePath
        }

        val temp = File(accountDir, "${sanitize(card.id)}.tmp")
        runCatching {
            val connection = (URL(imageUrl).openConnection() as HttpURLConnection).apply {
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                instanceFollowRedirects = true
            }
            try {
                connection.inputStream.use { input ->
                    temp.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                if (temp.length() <= 0L) return@runCatching null
                if (target.exists()) target.delete()
                temp.renameTo(target)
                target.absolutePath
            } finally {
                connection.disconnect()
            }
        }.getOrElse {
            temp.delete()
            card.localImagePath
        }
    }

    suspend fun cacheImages(cards: List<FlashCard>): List<FlashCard> = withContext(Dispatchers.IO) {
        cards.map { card ->
            val localPath = cacheImage(card)
            if (localPath.isNullOrBlank()) card else card.copy(localImagePath = localPath)
        }
    }

    suspend fun clearAllImages() = withContext(Dispatchers.IO) {
        rootDir.deleteRecursively()
    }

    private fun accountImageDir(): File? {
        val accountKey = accountKeyProvider()?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return File(rootDir, sha256(accountKey))
    }

    private fun resolveExtension(imageUrl: String): String {
        val cleanUrl = imageUrl.substringBefore('?').substringBefore('#')
        val extension = MimeTypeMap.getFileExtensionFromUrl(cleanUrl).orEmpty().lowercase()
        return extension.takeIf { it in SUPPORTED_EXTENSIONS } ?: DEFAULT_EXTENSION
    }

    private fun sanitize(raw: String): String {
        return raw.replace(Regex("[^A-Za-z0-9._-]"), "_").ifBlank { "card" }
    }

    private fun sha256(raw: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    private companion object {
        const val ROOT_DIR = "offline_card_images"
        const val DEFAULT_EXTENSION = "jpg"
        const val TIMEOUT_MS = 15_000
        val SUPPORTED_EXTENSIONS = setOf("jpg", "jpeg", "png", "webp", "gif")
    }
}
