package com.project.backend.payment.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.backend.payment.config.NatsJetStreamProperties;
import com.project.backend.payment.entity.PaymentEvent;
import com.project.backend.payment.entity.PaymentEventOutbox;
import com.project.backend.payment.repository.PaymentEventOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentEventOutboxService {

    private final PaymentEventOutboxRepository paymentEventOutboxRepository;
    private final NatsJetStreamProperties natsJetStreamProperties;
    private final ObjectMapper objectMapper;

    @Transactional
    public void enqueue(PaymentEvent event) {
        if (paymentEventOutboxRepository.existsByExternalPaymentEventId(event.getExternalPaymentEventId())) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        PaymentEventOutbox outbox = PaymentEventOutbox.create(
                event,
                natsJetStreamProperties.subject(),
                serialize(event),
                now
        );
        paymentEventOutboxRepository.save(outbox);
    }

    private String serialize(PaymentEvent event) {
        try {
            return objectMapper.writeValueAsString(event.toPostRequest());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(
                    "Failed to serialize payment event. externalPaymentEventId="
                            + event.getExternalPaymentEventId(),
                    e
            );
        }
    }
}
