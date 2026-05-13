package com.moneylog.paymenteventserver.payment.controller;

import com.moneylog.paymenteventserver.payment.dto.PaymentEventPostRequest;
import com.moneylog.paymenteventserver.payment.service.PaymentSimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-simulations")
@RequiredArgsConstructor
public class PaymentSimulationController {

    private final PaymentSimulationService paymentSimulationService;

    // 결제 이벤트 1개 생성만 함
    @PostMapping("/generate")
    public PaymentEventPostRequest generateOne() {
        return paymentSimulationService.generateOne();
    }

    // 결제 이벤트 1개 생성 후 메인 프로젝트로 전송
    @PostMapping("/send")
    public PaymentEventPostRequest generateOneAndSend() {
        return paymentSimulationService.generateOneAndSend();
    }

    // 결제 이벤트 여러 개 생성만 함
    @PostMapping("/generate/bulk")
    public List<PaymentEventPostRequest> generateBulk(
            @RequestParam(defaultValue = "10") int count
    ) {
        return paymentSimulationService.generateBulk(count);
    }

    // 결제 이벤트 여러 개 생성 후 메인 프로젝트로 전송
    @PostMapping("/send/bulk")
    public List<PaymentEventPostRequest> generateBulkAndSend(
            @RequestParam(defaultValue = "10") int count
    ) {
        return paymentSimulationService.generateBulkAndSend(count);
    }

    // 특정 userId 기준 결제 이벤트 여러 개 생성 후 메인 프로젝트로 전송
    @PostMapping("/users/{userId}/send/bulk")
    public List<PaymentEventPostRequest> generateBulkByUserIdAndSend(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "10") int count
    ) {
        return paymentSimulationService.generateBulkByUserIdAndSend(userId, count);
    }
}