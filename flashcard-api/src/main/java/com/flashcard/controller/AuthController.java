package com.flashcard.controller;

import com.flashcard.dto.request.LoginRequest;
import com.flashcard.dto.request.RegisterRequest;
import com.flashcard.dto.request.SocialLoginRequest;
import com.flashcard.dto.response.ApiResponse;
import com.flashcard.dto.response.AuthResponse;
import com.flashcard.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login endpoints (Local + Google + Facebook)")
public class AuthController {

    private final AuthService authService;

    // ─────────────────────────
    // LOCAL AUTH
    // ─────────────────────────

    @Operation(summary = "Register a new user with email & password")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @Operation(summary = "Login with email and password")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    // ─────────────────────────
    // SOCIAL AUTH
    // ─────────────────────────

    @Operation(
        summary = "Login / Register with Google",
        description = "Client sends the Google idToken. Server verifies it with Google APIs, then returns JWT."
    )
    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithGoogle(
            @Valid @RequestBody SocialLoginRequest request) {
        AuthResponse response = authService.loginWithGoogle(request.getToken());
        return ResponseEntity.ok(ApiResponse.success("Google login successful", response));
    }

    @Operation(
        summary = "Login / Register with Facebook",
        description = "Client sends the Facebook accessToken. Server verifies it with Graph API, then returns JWT."
    )
    @PostMapping("/facebook")
    public ResponseEntity<ApiResponse<AuthResponse>> loginWithFacebook(
            @Valid @RequestBody SocialLoginRequest request) {
        AuthResponse response = authService.loginWithFacebook(request.getToken());
        return ResponseEntity.ok(ApiResponse.success("Facebook login successful", response));
    }
}
