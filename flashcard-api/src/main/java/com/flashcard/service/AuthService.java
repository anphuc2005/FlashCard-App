package com.flashcard.service;

import com.flashcard.dto.request.LoginRequest;
import com.flashcard.dto.request.RegisterRequest;
import com.flashcard.dto.request.ForgotPasswordRequest;
import com.flashcard.dto.request.VerifyOtpRequest;
import com.flashcard.dto.request.ResetPasswordRequest;
import com.flashcard.dto.response.AuthResponse;
import com.flashcard.exception.DuplicateResourceException;
import com.flashcard.exception.ResourceNotFoundException;
import com.flashcard.exception.InvalidOtpException;
import com.flashcard.model.AuthProvider;
import com.flashcard.model.OtpToken;
import com.flashcard.model.User;
import com.flashcard.repository.OtpTokenRepository;
import com.flashcard.repository.UserRepository;
import com.flashcard.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

/**
 * Authentication service: Local register/login + Social login (Google/Facebook).
 *
 * Social login follows 5-step account linking:
 *   1. Verify token via SocialAuthService
 *   2. Find by providerId → JWT if exists
 *   3. Find by email → link account → JWT
 *   4. Create new user → JWT
 *
 * avatarUrl stores Cloudinary URLs (for user uploads) or social provider picture URLs.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final SocialAuthService socialAuthService;
    private final OtpTokenRepository otpTokenRepository;
    private final EmailService emailService;

    // ─────────────────────────
    // LOCAL AUTH
    // ─────────────────────────

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        AuthProvider localProvider = AuthProvider.builder()
                .providerName(AuthProvider.ProviderName.LOCAL)
                .providerId(request.getEmail())
                .build();

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .displayName(request.getDisplayName())
                .authProviders(new ArrayList<>())
                .build();
        user.getAuthProviders().add(localProvider);

        userRepository.save(user);
        return buildAuthResponse(user, false);
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        return buildAuthResponse(user, false);
    }

    // ─────────────────────────
    // FORGOT PASSWORD / OTP
    // ─────────────────────────

    public void processForgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        // Validate that this user has a LOCAL provider (users who only logged in via Social don't have passwords to reset)
        boolean hasLocalProvider = user.getAuthProviders().stream()
                .anyMatch(p -> p.getProviderName() == AuthProvider.ProviderName.LOCAL);
        
        if (!hasLocalProvider) {
            throw new InvalidOtpException("Account linked to social login. Please login with Google or Facebook.");
        }

        // Generate 6-digit OTP
        String otpCode = String.format("%06d", new Random().nextInt(999999));

        // Save OTP token (5 minutes expiration to allow password typing)
        OtpToken otpToken = OtpToken.builder()
                .email(user.getEmail())
                .otpCode(otpCode)
                .expirationTime(Instant.now().plus(5, ChronoUnit.MINUTES))
                .isUsed(false)
                .build();
        
        // Remove previous active OTPs for this email to prevent spam (optional but good practice)
        // For simplicity we just save the new one, but one could delete old unused ones first.
        otpTokenRepository.save(otpToken);

        // Send email
        emailService.sendOtpEmail(user.getEmail(), otpCode);
        log.info("[Forgot Password] OTP sent to email: {}", user.getEmail());
    }

    public void verifyOtp(VerifyOtpRequest request) {
        OtpToken otpToken = otpTokenRepository.findByEmailAndOtpCode(request.getEmail(), request.getOtpCode())
                .orElseThrow(() -> new InvalidOtpException("Invalid OTP code"));

        if (otpToken.isUsed()) {
            throw new InvalidOtpException("OTP has already been used");
        }

        if (Instant.now().isAfter(otpToken.getExpirationTime())) {
            throw new InvalidOtpException("OTP has expired");
        }
        
        // OTP is valid - do not mark as used yet, wait for resetPassword call
    }

    public void resetPassword(ResetPasswordRequest request) {
        // Re-verify the OTP first
        OtpToken otpToken = otpTokenRepository.findByEmailAndOtpCode(request.getEmail(), request.getOtpCode())
                .orElseThrow(() -> new InvalidOtpException("Invalid OTP code"));

        if (otpToken.isUsed()) {
            throw new InvalidOtpException("OTP has already been used");
        }

        if (Instant.now().isAfter(otpToken.getExpirationTime())) {
            throw new InvalidOtpException("OTP has expired");
        }

        // Proceed to update password
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getEmail()));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark OTP as used
        otpToken.setUsed(true);
        otpTokenRepository.save(otpToken);
        
        log.info("[Forgot Password] Password reset successfully for email: {}", user.getEmail());
    }

    // ─────────────────────────
    // SOCIAL AUTH — Google
    // ─────────────────────────

    public AuthResponse loginWithGoogle(String idToken) {
        SocialAuthService.SocialUserInfo info = socialAuthService.verifyGoogleIdToken(idToken);
        return processSocialLogin(info, AuthProvider.ProviderName.GOOGLE);
    }

    // ─────────────────────────
    // SOCIAL AUTH — Facebook
    // ─────────────────────────

    public AuthResponse loginWithFacebook(String accessToken) {
        SocialAuthService.SocialUserInfo info = socialAuthService.verifyFacebookAccessToken(accessToken);
        return processSocialLogin(info, AuthProvider.ProviderName.FACEBOOK);
    }

    // ─────────────────────────────────────────────────────────────────────
    // CORE: 5-STEP ACCOUNT LINKING LOGIC
    // ─────────────────────────────────────────────────────────────────────

    private AuthResponse processSocialLogin(SocialAuthService.SocialUserInfo info,
                                            AuthProvider.ProviderName providerName) {
        // Step 2: Find by providerId (already linked account)
        Optional<User> byProvider = userRepository
                .findByAuthProviders_ProviderNameAndAuthProviders_ProviderId(
                        providerName, info.providerId());
        if (byProvider.isPresent()) {
            log.info("[Social Login] Found existing linked user: {}", byProvider.get().getEmail());
            return buildAuthResponse(byProvider.get(), false);
        }

        // Step 3: Find by email
        Optional<User> byEmail = userRepository.findByEmail(info.email());
        if (byEmail.isPresent()) {
            // Step 4: Link new provider to existing account
            User existingUser = byEmail.get();
            AuthProvider newProvider = AuthProvider.builder()
                    .providerName(providerName)
                    .providerId(info.providerId())
                    .build();
            existingUser.getAuthProviders().add(newProvider);

            // Update avatar if not set (store social picture URL in MongoDB)
            if (existingUser.getAvatarUrl() == null && info.pictureUrl() != null) {
                existingUser.setAvatarUrl(info.pictureUrl());
            }
            userRepository.save(existingUser);
            log.info("[Social Login] Linked {} provider to existing user: {}", providerName, existingUser.getEmail());
            return buildAuthResponse(existingUser, false);
        }

        // Step 5: Create brand-new user
        AuthProvider newProvider = AuthProvider.builder()
                .providerName(providerName)
                .providerId(info.providerId())
                .build();

        User newUser = User.builder()
                .email(info.email())
                .displayName(info.name())
                .avatarUrl(info.pictureUrl())   // Social picture URL → stored in MongoDB
                .authProviders(new ArrayList<>())
                .build();
        newUser.getAuthProviders().add(newProvider);

        userRepository.save(newUser);
        log.info("[Social Login] Created new user via {}: {}", providerName, newUser.getEmail());
        return buildAuthResponse(newUser, true);
    }

    // ─────────────────────────
    // HELPER
    // ─────────────────────────

    private AuthResponse buildAuthResponse(User user, boolean isNewUser) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken  = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .isNewUser(isNewUser)
                .build();
    }
}
