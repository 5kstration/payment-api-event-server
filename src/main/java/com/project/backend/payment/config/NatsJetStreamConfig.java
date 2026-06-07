package com.project.backend.payment.config;

import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.Nats;
import io.nats.client.Options;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@EnableConfigurationProperties(NatsJetStreamProperties.class)
public class NatsJetStreamConfig {

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(
            prefix = "payment-event.messaging.nats",
            name = "enabled",
            havingValue = "true"
    )
    public Connection natsConnection(NatsJetStreamProperties properties)
            throws IOException, InterruptedException {
        Options options = new Options.Builder()
                .server(properties.url())
                .connectionName(properties.connectionName())
                .connectionTimeout(properties.connectionTimeout())
                .reconnectWait(properties.reconnectWait())
                .maxReconnects(properties.maxReconnects())
                .build();

        return Nats.connect(options);
    }

    @Bean
    @ConditionalOnProperty(
            prefix = "payment-event.messaging.nats",
            name = "enabled",
            havingValue = "true"
    )
    public JetStream jetStream(Connection natsConnection) throws IOException {
        return natsConnection.jetStream();
    }
}
