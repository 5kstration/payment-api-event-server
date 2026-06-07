package com.project.backend.payment.service;

import com.project.backend.payment.client.MainProjectPaymentEventClient;
import com.project.backend.payment.config.NatsJetStreamProperties;
import com.project.backend.payment.entity.PaymentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentEventDeliveryService {

    private final MainProjectPaymentEventClient mainProjectPaymentEventClient;
    private final PaymentEventOutboxService paymentEventOutboxService;
    private final NatsJetStreamProperties natsJetStreamProperties;

    public void dispatch(PaymentEvent event) {
        if (natsJetStreamProperties.enabled()) {
            paymentEventOutboxService.enqueue(event);
            return;
        }

        mainProjectPaymentEventClient.sendPaymentEvent(event.toPostRequest());
        event.markSentToBudget(LocalDateTime.now());
    }
}
