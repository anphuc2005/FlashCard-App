package com.flashcard.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Binds JWT-related properties from application.yml under the "app.jwt" prefix.
 */
@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /**
     * Base64-encoded secret key used to sign JWT tokens.
     */
    private String secretKey;

    /**
     * Access token validity duration in milliseconds. Default: 1 hour.
     */
    private long accessTokenExpiration = 3600000;

    /**
     * Refresh token validity duration in milliseconds. Default: 7 days.
     */
    private long refreshTokenExpiration = 604800000;
}
