package com.flashcard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

/**
 * MongoDB document representing a single Flashcard within a Deck.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "cards")
public class Card {

    @Id
    private String id;

    /**
     * Reference to the parent Deck. Indexed for fast card retrieval by deck.
     */
    @NotBlank(message = "Deck ID is required")
    @Indexed
    private String deckId;

    @NotBlank(message = "Front content is required")
    private String front;

    @NotBlank(message = "Back content is required")
    private String back;

    /**
     * Spaced Repetition fields for offline-first sync.
     */
    @Builder.Default
    private int reviewCount = 0;

    @Builder.Default
    private int easeFactor = 250; // Stored as integer (2.50 * 100)

    private Instant nextReviewDate;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
