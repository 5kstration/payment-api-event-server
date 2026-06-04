package com.project.backend.payment.client;

import com.project.backend.payment.dto.PaymentEventPostRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
public class MainProjectPaymentEventClient {

    private final RestClient restClient;

    @Value("${connector-service.payment-event-url}")
    private String paymentEventUrl;

    public void sendPaymentEvent(PaymentEventPostRequest request) {
        try {
            restClient.post()
                    .uri(paymentEventUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException e) {
            throw new IllegalStateException("Failed to send payment event. url=" + paymentEventUrl, e);
        }
    }
}
