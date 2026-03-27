package com.flashcard.repository;

import com.flashcard.model.Card;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends MongoRepository<Card, String> {
    List<Card> findByDeckId(String deckId);
    long countByDeckId(String deckId);
    void deleteByDeckId(String deckId);
}
