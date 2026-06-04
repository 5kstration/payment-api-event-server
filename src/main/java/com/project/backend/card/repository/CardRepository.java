package com.project.backend.card.repository;

import com.project.backend.card.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, String> {

    Optional<Card> findByUserId(String userId);

    List<Card> findByActiveTrue();

    List<Card> findByUserIdAndActiveTrue(String userId);
}
