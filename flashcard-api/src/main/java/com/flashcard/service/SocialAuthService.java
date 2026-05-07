package com.flashcard.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;

/**
 * Service to verify social login tokens with Google and Facebook APIs.
 * The server verifies tokens directly — never trusts the client's claims.
 *
 * Avatars from social providers are stored as-is (external URL) in MongoDB avatarUrl field.
 * For user-uploaded images, Cloudinary is used separately.
 */
@Slf4j
@Service
public class SocialAuthService {

    @Value("${app.social.google.client-id}")
    private String googleClientId;

    @Value("${app.social.facebook.app-id}")
    private String facebookAppId;

    @Value("${app.social.facebook.app-secret}")
    private String facebookAppSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Container for verified social user info.
     */
    public record SocialUserInfo(
            String providerId,
            String email,
            String name,
            String pictureUrl
    ) {}

    // ─────────────────────────────────────────────────────
    // GOOGLE — Verify idToken via Google API Client library
    // ─────────────────────────────────────────────────────

    public SocialUserInfo verifyGoogleIdToken(String idToken) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance()
            )
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken token = verifier.verify(idToken);
            if (token == null) {
                throw new IllegalArgumentException("Invalid Google idToken");
            }

            GoogleIdToken.Payload payload = token.getPayload();
            String email = payload.getEmail();
            if (email == null || email.trim().isEmpty()) {
                email = payload.getSubject() + "@google.com";
            }

            return new SocialUserInfo(
                    payload.getSubject(),              // providerId = Google "sub"
                    email,
                    (String) payload.get("name"),
                    (String) payload.get("picture")   // Google profile picture URL
            );
        } catch (Exception e) {
            log.error("Google token verification failed: {}", e.getMessage());
            throw new IllegalArgumentException("Google authentication failed: " + e.getMessage());
        }
    }

    // ─────────────────────────────────────────────────────
    // FACEBOOK — Strict Verification via /debug_token
    // ─────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public SocialUserInfo verifyFacebookAccessToken(String accessToken) {
        try {
            // STEP 1: Strictly verify the token against our App ID/Secret
            String debugUrl = "https://graph.facebook.com/debug_token?input_token=" + accessToken
                    + "&access_token=" + facebookAppId + "|" + facebookAppSecret;

            Map<String, Object> debugResponse = restTemplate.getForObject(debugUrl, Map.class);

            if (debugResponse == null || !debugResponse.containsKey("data")) {
                throw new IllegalArgumentException("Failed to verify Facebook token");
            }

            Map<String, Object> data = (Map<String, Object>) debugResponse.get("data");
            Boolean isValid = (Boolean) data.get("is_valid");
            String extractedAppId = (String) data.get("app_id");

            if (Boolean.FALSE.equals(isValid) || !facebookAppId.equals(extractedAppId)) {
                log.warn("Facebook Token invalid or not issued for our App ID. Valid: {}, ExtractedAppId: {}", isValid, extractedAppId);
                throw new IllegalArgumentException("Invalid Facebook accessToken or not issued for this app");
            }

            // STEP 2: Token is valid and belongs to our App. Now fetch user profile info.
            String url = "https://graph.facebook.com/me?fields=id,name,email,picture.type(large)&access_token=" + accessToken;
            Map<String, Object> fbResponse = restTemplate.getForObject(url, Map.class);

            if (fbResponse == null || fbResponse.containsKey("error")) {
                throw new IllegalArgumentException("Failed to fetch user profile from Facebook");
            }

            String providerId = (String) fbResponse.get("id");
            String email      = (String) fbResponse.get("email");
            if (email == null || email.trim().isEmpty()) {
                email = providerId + "@facebook.com";
            }
            String name       = (String) fbResponse.get("name");

            // Extract nested picture URL: { "picture": { "data": { "url": "..." } } }
            String pictureUrl = null;
            Object pictureObj = fbResponse.get("picture");
            if (pictureObj instanceof Map<?, ?> picMap) {
                Object dataObj = picMap.get("data");
                if (dataObj instanceof Map<?, ?> dataMap) {
                    pictureUrl = (String) dataMap.get("url");
                }
            }

            return new SocialUserInfo(providerId, email, name, pictureUrl);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Facebook token strict verification failed: {}", e.getMessage());
            throw new IllegalArgumentException("Facebook authentication failed: " + e.getMessage());
        }
    }
}
