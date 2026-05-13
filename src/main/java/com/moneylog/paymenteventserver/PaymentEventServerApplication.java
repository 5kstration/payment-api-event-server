package com.moneylog.paymenteventserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling       // Spring 에서 스케줄러 기능 어노테이션
@SpringBootApplication
public class PaymentEventServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentEventServerApplication.class, args);
    }

}
