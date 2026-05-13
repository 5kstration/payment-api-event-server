package com.moneylog.paymenteventserver.card.repository;
import com.moneylog.paymenteventserver.card.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {

    /* Card Id 와 User를 찾는 Repository JPA
    / 예외를 고려한 Optional
     */
    Optional<Card> findByUserIdAndUserCardId(Long userId, Long userCardId);

    // 결제 이벤트 생성 시 활성된 카드만 가져오는 Repository JPA
    List<Card> findByActiveTrue();

    List<Card> findByUserIdAndActiveTrue(Long userId);
}
