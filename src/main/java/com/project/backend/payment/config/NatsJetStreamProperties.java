package com.project.backend.payment.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "payment-event.messaging.nats")
public record NatsJetStreamProperties(
        boolean enabled,
        String url,
        String connectionName,
        Duration connectionTimeout,
        Duration reconnectWait,
        int maxReconnects,
        String stream,
        String subject
) {
    public NatsJetStreamProperties {
        if (enabled && (url == null || url.isBlank())) {
            throw new IllegalStateException("NATS URL must be configured when NATS messaging is enabled.");
        }
        if (connectionName == null || connectionName.isBlank()) {
            connectionName = "payment-api-event-server";
        }
        if (connectionTimeout == null) {
            connectionTimeout = Duration.ofSeconds(2);
        }
        if (reconnectWait == null) {
            reconnectWait = Duration.ofSeconds(2);
        }
        if (maxReconnects == 0) {
            maxReconnects = 10;
        }
        if (stream == null || stream.isBlank()) {
            stream = "PAYMENT_EVENTS";
        }
        if (subject == null || subject.isBlank()) {
            subject = "payment.events.created";
        }
    }
}
