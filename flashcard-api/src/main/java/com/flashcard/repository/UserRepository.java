package com.flashcard.repository;

import com.flashcard.model.AuthProvider;
import com.flashcard.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    /**
     * Find a user who has a specific provider linked (for social login lookup).
     * MongoDB query on embedded array: authProviders.providerName + authProviders.providerId
     */
    Optional<User> findByAuthProviders_ProviderNameAndAuthProviders_ProviderId(
            AuthProvider.ProviderName providerName, String providerId);
}
