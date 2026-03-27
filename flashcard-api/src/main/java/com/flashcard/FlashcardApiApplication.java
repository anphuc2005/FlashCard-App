package com.flashcard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class FlashcardApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FlashcardApiApplication.class, args);
    }
}
