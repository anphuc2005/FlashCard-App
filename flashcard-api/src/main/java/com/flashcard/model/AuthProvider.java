package com.flashcard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embedded document representing a linked authentication provider for a User.
 * Supports LOCAL (email/password), GOOGLE, and FACEBOOK.
 * Stored as an array inside the User document (MongoDB embedded approach).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthProvider {

    public enum ProviderName {
        LOCAL, GOOGLE, FACEBOOK
    }

    /**
     * The authentication provider type.
     */
    private ProviderName providerName;

    /**
     * Unique ID from the provider (e.g. Google sub, Facebook ID).
     * For LOCAL auth, this equals the user's MongoDB _id.
     */
    private String providerId;
}
