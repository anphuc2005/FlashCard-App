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

import jakarta.validation.constraints.Email;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MongoDB document representing an application user.
 * Supports both local credentials and social login via embedded AuthProvider list.
 * Images (avatarUrl) are stored as Cloudinary URLs in MongoDB.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @Email(message = "Invalid email format")
    @Indexed(unique = true)
    private String email;

    /**
     * Nullable — social-only users have no local password.
     */
    private String password;

    private String displayName;

    /**
     * Cloudinary URL (or social provider picture URL).
     * For user uploads: upload to Cloudinary first, store returned URL here.
     */
    private String avatarUrl;

    /**
     * Embedded list of linked authentication providers.
     * A user can have multiple providers linked (LOCAL + GOOGLE, etc.).
     */
    @Builder.Default
    private List<AuthProvider> authProviders = new ArrayList<>();

    @Builder.Default
    private Set<String> roles = new HashSet<>(Set.of("ROLE_USER"));

    @Builder.Default
    private boolean enabled = true;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;
}
