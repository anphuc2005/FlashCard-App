package com.flashcard.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.mapping.event.ValidatingMongoEventListener;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * MongoDB-specific configurations.
 * Enables JSR-303 validation on MongoDB documents before persistence.
 */
@Configuration
public class MongoConfig {

    @Bean
    public ValidatingMongoEventListener validatingMongoEventListener(
            LocalValidatorFactoryBean factory) {
        return new ValidatingMongoEventListener(factory);
    }
}
