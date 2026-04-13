package com.flashcard.repository;

import com.flashcard.model.OtpToken;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface OtpTokenRepository extends MongoRepository<OtpToken, String> {
    Optional<OtpToken> findByEmailAndOtpCode(String email, String otpCode);
}
