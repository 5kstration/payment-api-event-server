package com.project.backend.payment.controller;

import com.project.backend.payment.service.UserPaymentStateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/users/{userId}/budget-sync")
@RequiredArgsConstructor
public class UserPaymentStateController {

    private final UserPaymentStateService userPaymentStateService;

    @PostMapping("/activate")
    public ResponseEntity<Void> activate(@PathVariable String userId) {
        userPaymentStateService.activateBudgetSync(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable String userId) {
        userPaymentStateService.deactivateBudgetSync(userId);
        return ResponseEntity.noContent().build();
    }
}
