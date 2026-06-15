package com.project.backend.payment.scheduler;

import com.project.backend.payment.service.PaymentSimulationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentSimulationScheduler {

    private static final LocalTime SLEEP_START_TIME = LocalTime.MIDNIGHT;
    private static final LocalTime SLEEP_END_TIME = LocalTime.of(6, 0);

    private final PaymentSimulationService paymentSimulationService;

    @Scheduled(fixedDelayString = "${simulation.fixed-delay-ms}")
    public void generateAndSendPaymentEvent() {
        if (isSleepTime(LocalTime.now())) {
            log.info("Payment event generation skipped during sleep time.");
            return;
        }

        try {
            var requests = paymentSimulationService.generateForEachRegisteredCardAndSend();

            log.info(
                    "Payment events generated for all registered cards. count={}",
                    requests.size()
            );
        } catch (IllegalStateException e) {
            log.warn("Payment event generation skipped. reason={}", e.getMessage());
        } catch (Exception e) {
            log.error("Payment event generation failed", e);
        }
    }

    private boolean isSleepTime(LocalTime now) {
        return !now.isBefore(SLEEP_START_TIME) && now.isBefore(SLEEP_END_TIME);
    }
}
