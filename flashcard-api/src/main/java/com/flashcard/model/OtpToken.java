package com.flashcard.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "otp_tokens")
public class OtpToken {
    @Id
    private String id;
    private String email;
    private String otpCode;
    private Instant expirationTime;
    private boolean isUsed;
}
