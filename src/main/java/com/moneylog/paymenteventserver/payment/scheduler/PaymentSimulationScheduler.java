package com.moneylog.paymenteventserver.payment.scheduler;

import com.moneylog.paymenteventserver.payment.service.PaymentSimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSimulationScheduler {

    private final PaymentSimulationService paymentSimulationService;

    @Scheduled(fixedDelayString = "${simulation.fixed-delay-ms}")
    public void generateAndSendPaymentEvent() {
        try {
            var request = paymentSimulationService.generateOneAndSend();

            log.info(
                    "Payment event generated. It is sent to budget only when budget sync is enabled. externalPaymentEventId={}, userId={}, cardId={}, amount={}",
                    request.externalPaymentEventId(),
                    request.userId(),
                    request.cardId(),
                    request.amount()
            );
        } catch (IllegalStateException e) {
            log.warn("Payment event generation skipped. reason={}", e.getMessage());
        } catch (Exception e) {
            log.error("Payment event generation failed", e);
        }
    }
}
