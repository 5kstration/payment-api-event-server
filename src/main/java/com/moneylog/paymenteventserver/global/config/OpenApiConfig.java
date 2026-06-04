package com.moneylog.paymenteventserver.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI paymentApiEventServerOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Payment API Event Server")
                        .description("Payment card sync and simulated payment event APIs")
                        .version("v1"));
    }
}
