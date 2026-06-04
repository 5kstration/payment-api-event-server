package com.project.backend.payment.generator;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Component
@RequiredArgsConstructor
public class PaymentSimulationDataLoader {

    private final ObjectMapper objectMapper;

    @Value("${simulation.data-file}")
    private Resource dataFile;

    private PaymentSimulationData data;

    @PostConstruct
    public void load() {
        try (InputStream inputStream = dataFile.getInputStream()) {
            this.data = objectMapper.readValue(inputStream, PaymentSimulationData.class);
            validate(this.data);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load payment simulation data file.", e);
        }
    }

    private void validate(PaymentSimulationData data) {
        if (data == null) {
            throw new IllegalStateException("Payment simulation data must not be null.");
        }

        if (data.categories() == null || data.categories().isEmpty()) {
            throw new IllegalStateException("categories must not be empty.");
        }

        if (data.merchants() == null || data.merchants().isEmpty()) {
            throw new IllegalStateException("merchants must not be empty.");
        }

        if (data.areas() == null || data.areas().isEmpty()) {
            throw new IllegalStateException("areas must not be empty.");
        }

        if (data.branchSuffixes() == null || data.branchSuffixes().isEmpty()) {
            throw new IllegalStateException("branchSuffixes must not be empty.");
        }

        data.categories().forEach(category -> {
            if (category.code() == null || category.code().isBlank()) {
                throw new IllegalStateException("category code must not be blank.");
            }

            if (category.minAmount() < 0) {
                throw new IllegalStateException(
                        "minAmount must be greater than or equal to 0. category=" + category.code()
                );
            }

            if (category.maxAmount() < category.minAmount()) {
                throw new IllegalStateException(
                        "maxAmount must be greater than or equal to minAmount. category=" + category.code()
                );
            }

            if (category.roundUnit() == null || category.roundUnit() <= 0) {
                throw new IllegalStateException(
                        "roundUnit must be greater than or equal to 1. category=" + category.code()
                );
            }

            if (category.timeWindows() == null || category.timeWindows().isEmpty()) {
                throw new IllegalStateException("timeWindows must not be empty. category=" + category.code());
            }

            category.timeWindows().forEach(timeWindow -> {
                if (timeWindow.startHour() < 0 || timeWindow.startHour() > 23) {
                    throw new IllegalStateException("startHour must be between 0 and 23. category=" + category.code());
                }

                if (timeWindow.endHour() < 1 || timeWindow.endHour() > 24) {
                    throw new IllegalStateException("endHour must be between 1 and 24. category=" + category.code());
                }

                if (timeWindow.startHour() >= timeWindow.endHour()) {
                    throw new IllegalStateException("startHour must be less than endHour. category=" + category.code());
                }

                if (timeWindow.weight() <= 0) {
                    throw new IllegalStateException("weight must be greater than or equal to 1. category=" + category.code());
                }
            });
        });

        Set<String> categoryCodes = data.categories()
                .stream()
                .map(PaymentSimulationData.CategoryRule::code)
                .collect(Collectors.toSet());

        if (categoryCodes.size() != data.categories().size()) {
            throw new IllegalStateException("Duplicate category code exists.");
        }

        data.merchants().forEach(merchant -> {
            if (merchant.name() == null || merchant.name().isBlank()) {
                throw new IllegalStateException("merchant name must not be blank.");
            }

            if (merchant.category() == null || merchant.category().isBlank()) {
                throw new IllegalStateException("merchant category must not be blank. merchant=" + merchant.name());
            }

            if (!categoryCodes.contains(merchant.category())) {
                throw new IllegalStateException(
                        "merchant references an unknown category. merchant="
                                + merchant.name()
                                + ", category="
                                + merchant.category()
                );
            }
        });

        data.areas().forEach(area -> {
            if (area == null || area.isBlank()) {
                throw new IllegalStateException("areas must not contain blank values.");
            }
        });

        data.branchSuffixes().forEach(branchSuffix -> {
            if (branchSuffix == null || branchSuffix.isBlank()) {
                throw new IllegalStateException("branchSuffixes must not contain blank values.");
            }
        });
    }
}
