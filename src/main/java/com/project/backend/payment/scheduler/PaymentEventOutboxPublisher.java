package com.project.backend.payment.scheduler;

import com.project.backend.payment.entity.PaymentEventOutbox;
import com.project.backend.payment.entity.PaymentEventOutboxStatus;
import com.project.backend.payment.repository.PaymentEventOutboxRepository;
import com.project.backend.payment.repository.PaymentEventRepository;
import io.nats.client.JetStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "payment-event.messaging.nats",
        name = "enabled",
        havingValue = "true"
)
public class PaymentEventOutboxPublisher {

    private final PaymentEventOutboxRepository paymentEventOutboxRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final JetStream jetStream;

    @Scheduled(fixedDelayString = "${payment-event.messaging.nats.publisher-fixed-delay-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        paymentEventOutboxRepository.findTop50ByStatusOrderByCreatedAtAsc(PaymentEventOutboxStatus.PENDING)
                .forEach(this::publish);
    }

    private void publish(PaymentEventOutbox outbox) {
        try {
            jetStream.publish(outbox.getSubject(), outbox.getPayload().getBytes(StandardCharsets.UTF_8));

            LocalDateTime now = LocalDateTime.now();
            outbox.markPublished(now);
            paymentEventRepository.findById(outbox.getPaymentEventId())
                    .ifPresent(event -> event.markSentToBudget(now));

            log.info(
                    "Payment event outbox published. externalPaymentEventId={}, subject={}",
                    outbox.getExternalPaymentEventId(),
                    outbox.getSubject()
            );
        } catch (Exception e) {
            outbox.recordFailure(e.getMessage(), LocalDateTime.now());
            log.warn(
                    "Failed to publish payment event outbox. externalPaymentEventId={}, subject={}",
                    outbox.getExternalPaymentEventId(),
                    outbox.getSubject(),
                    e
            );
        }
    }
}
