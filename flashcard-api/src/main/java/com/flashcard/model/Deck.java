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
 * MongoDB document representing a Flashcard Deck (a collection of cards).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "decks")
public class Deck {

    @Id
    private String id;

    @NotBlank(message = "Deck name is required")
    private String name;

    private String description;

    /**
     * Owner's user ID. Indexed for fast lookup of decks by user.
     */
    @Indexed
    private String userId;

    /**
     * Number of cards in this deck (denormalized for performance).
     */
    @Builder.Default
    private int cardCount = 0;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
