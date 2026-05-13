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
                    "결제 이벤트 전송 완료. externalPaymentEventId={}, userId={}, userCardId={}, amount={}",
                    request.externalPaymentEventId(),
                    request.userId(),
                    request.userCardId(),
                    request.amount()
            );
        } catch (IllegalStateException e) {
            log.warn("결제 이벤트 생성/전송 스킵. reason={}", e.getMessage());
        } catch (Exception e) {
            log.error("결제 이벤트 생성/전송 실패", e);
        }
    }
}
