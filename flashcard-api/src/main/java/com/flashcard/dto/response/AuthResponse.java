package com.flashcard.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String email;
    private String displayName;

    /**
     * Cloudinary URL or social provider picture URL stored in MongoDB.
     */
    private String avatarUrl;

    /**
     * True if this social login just created a brand-new account.
     */
    private Boolean isNewUser;
}
