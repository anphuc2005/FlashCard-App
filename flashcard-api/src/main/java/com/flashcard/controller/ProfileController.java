package com.flashcard.controller;

import com.flashcard.dto.request.UpdateProfileRequest;
import com.flashcard.dto.response.ApiResponse;
import com.flashcard.dto.response.UserProfileResponse;
import com.flashcard.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Current user profile endpoints")
public class ProfileController {

    private final ProfileService profileService;

    @Operation(summary = "Get current user profile")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        UserProfileResponse response = profileService.getMyProfile(email);
        return ResponseEntity.ok(ApiResponse.success("Profile fetched successfully", response));
    }

    @Operation(summary = "Update current user profile")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateMyProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        String email = authentication.getName();
        UserProfileResponse response = profileService.updateMyProfile(email, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }
}
