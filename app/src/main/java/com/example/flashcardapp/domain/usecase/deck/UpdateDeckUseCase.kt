package com.example.flashcardapp.domain.usecase.deck

import com.example.flashcardapp.data.repository.DeckRepository
import com.example.flashcardapp.domain.model.Deck

class UpdateDeckUseCase(
    private val deckRepository: DeckRepository
) {
    suspend operator fun invoke(id: String, name: String, description: String, isPublic: Boolean, existingDeck: Deck): Result<Deck> {
        val updatedDeck = existingDeck.copy(
            name = name,
            description = description,
            isPublic = isPublic,
            updatedAt = System.currentTimeMillis().toString()
        )
        
        // Nếu chuyển từ public -> private (tắt công khai)
        if (existingDeck.isPublic && !isPublic) {
            // Delete from server but keep locally
            deckRepository.deleteDeckFromServerOnly(id)
            // Cập nhật local
            deckRepository.updateDeckLocal(updatedDeck)
            return Result.success(updatedDeck)
        }
        
        // Nếu đang private -> public (bật công khai) thì chưa có trên server, gọi createDeck? 
        // Thay vì logic phức tạp, ta có thể dùng updateDeck trực tiếp
        if (!existingDeck.isPublic && isPublic) {
            return deckRepository.createDeck(updatedDeck, true)
        }

        // Cập nhật bình thường nếu mode không đổi
        return if (isPublic) {
            deckRepository.updateDeck(id, updatedDeck)
        } else {
            deckRepository.updateDeckLocal(updatedDeck)
            Result.success(updatedDeck)
        }
    }
}
