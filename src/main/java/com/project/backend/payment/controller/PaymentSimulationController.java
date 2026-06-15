package com.project.backend.payment.controller;

import com.project.backend.payment.dto.PaymentEventPostRequest;
import com.project.backend.payment.service.PaymentSimulationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-simulations")
@RequiredArgsConstructor
public class PaymentSimulationController {

    private final PaymentSimulationService paymentSimulationService;

    @PostMapping("/generate")
    public PaymentEventPostRequest generateOne() {
        return paymentSimulationService.generateOne();
    }

    @PostMapping("/send")
    public PaymentEventPostRequest generateOneAndSend() {
        return paymentSimulationService.generateOneAndSend();
    }

    @PostMapping("/send/all")
    public List<PaymentEventPostRequest> generateForEachRegisteredCardAndSend() {
        return paymentSimulationService.generateForEachRegisteredCardAndSend();
    }

    @PostMapping("/generate/bulk")
    public List<PaymentEventPostRequest> generateBulk(
            @RequestParam(defaultValue = "10") int count
    ) {
        return paymentSimulationService.generateBulk(count);
    }

    @PostMapping("/send/bulk")
    public List<PaymentEventPostRequest> generateBulkAndSend(
            @RequestParam(defaultValue = "10") int count
    ) {
        return paymentSimulationService.generateBulkAndSend(count);
    }

    @PostMapping("/users/{userId}/send/bulk")
    public List<PaymentEventPostRequest> generateBulkByUserIdAndSend(
            @PathVariable String userId,
            @RequestParam(defaultValue = "10") int count
    ) {
        return paymentSimulationService.generateBulkByUserIdAndSend(userId, count);
    }
}
