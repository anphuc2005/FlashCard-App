package com.example.flashcardapp.data.sync

import com.example.flashcardapp.AppSessionManager
import com.example.flashcardapp.core.network.NetworkMonitor
import com.example.flashcardapp.data.repository.DeckRepository
import com.example.flashcardapp.data.repository.FlashCardRepository
import com.example.flashcardapp.data.repository.StudyRepository
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class OfflineSyncManager(
    private val networkMonitor: NetworkMonitor,
    private val sessionManager: AppSessionManager,
    private val deckRepository: DeckRepository,
    private val flashCardRepository: FlashCardRepository,
    private val studyRepository: StudyRepository
) {

    private val syncMutex = Mutex()

    suspend fun observeAndSync() {
        networkMonitor.isOnline
            .filter { it }
            .collectLatest {
                syncPending()
            }
    }

    suspend fun syncPending(): Result<Unit> {
        if (!networkMonitor.isCurrentlyOnline()) return Result.success(Unit)
        if (sessionManager.accessToken.isNullOrBlank() || sessionManager.isAuthExpired) {
            return Result.success(Unit)
        }

        return syncMutex.withLock {
            runCatching {
                deckRepository.syncPendingDecks().getOrThrow()
                flashCardRepository.syncPendingCards().getOrThrow()
                studyRepository.syncReviews().getOrThrow()
                Unit
            }
        }
    }

    suspend fun getPendingChangeCount(): Int {
        return deckRepository.getPendingDeckCount() +
            flashCardRepository.getPendingCardCount() +
            studyRepository.getPendingReviewCount()
    }
}
