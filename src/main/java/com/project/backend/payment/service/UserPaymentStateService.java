package com.project.backend.payment.service;

import com.project.backend.card.repository.CardRepository;
import com.project.backend.payment.client.MainProjectPaymentEventClient;
import com.project.backend.payment.entity.PaymentEvent;
import com.project.backend.payment.entity.UserPaymentState;
import com.project.backend.payment.repository.PaymentEventRepository;
import com.project.backend.payment.repository.UserPaymentStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPaymentStateService {

    private final CardRepository cardRepository;
    private final UserPaymentStateRepository userPaymentStateRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final MainProjectPaymentEventClient mainProjectPaymentEventClient;

    @Transactional
    public void activateBudgetSync(String userId) {
        if (cardRepository.findByUserId(userId).isEmpty()) {
            log.info("Ignoring budget sync activation because card is not registered. userId={}", userId);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        UserPaymentState state = userPaymentStateRepository.findById(userId)
                .orElseGet(() -> UserPaymentState.create(userId));
        state.activate(now);
        userPaymentStateRepository.save(state);

        flushPendingEvents(userId, state);
    }

    @Transactional
    public void deactivateBudgetSync(String userId) {
        if (cardRepository.findByUserId(userId).isEmpty()) {
            log.info("Ignoring budget sync deactivation because card is not registered. userId={}", userId);
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        UserPaymentState state = userPaymentStateRepository.findById(userId)
                .orElseGet(() -> UserPaymentState.create(userId));
        state.deactivate(now);
        userPaymentStateRepository.save(state);
    }

    private void flushPendingEvents(String userId, UserPaymentState state) {
        List<PaymentEvent> pendingEvents = paymentEventRepository
                .findByUserIdAndSentToBudgetFalseOrderByPaidAtAsc(userId);
        LocalDateTime flushedAt = LocalDateTime.now();

        for (PaymentEvent event : pendingEvents) {
            try {
                mainProjectPaymentEventClient.sendPaymentEvent(event.toPostRequest());
                event.markSentToBudget(LocalDateTime.now());
            } catch (RuntimeException e) {
                log.warn(
                        "Failed to send pending payment event to budget. userId={}, externalPaymentEventId={}",
                        event.getUserId(),
                        event.getExternalPaymentEventId(),
                        e
                );
            }
        }

        state.markFlushed(flushedAt);
    }
}
