package com.flashcard.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    @NotBlank(message = "Display name is required")
    private String displayName;

    // New request key is imageUrl; avatarUrl is kept as backward-compatible alias.
    @JsonAlias("avatarUrl")
    private String imageUrl;
}
