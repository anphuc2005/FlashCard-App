package com.flashcard.repository;

import com.flashcard.model.Deck;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeckRepository extends MongoRepository<Deck, String> {
    List<Deck> findByUserId(String userId);
}
