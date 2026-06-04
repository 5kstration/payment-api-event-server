package com.project.backend.payment.generator;

import java.util.List;

public record PaymentSimulationData(
        List<CategoryRule> categories,
        List<MerchantBrand> merchants,
        List<String> areas,
        List<String> branchSuffixes
) {
    public record CategoryRule(
            String code,
            long minAmount,
            long maxAmount,
            Integer roundUnit,
            List<TimeWindow> timeWindows
    ) {
    }

    public record TimeWindow(
            int startHour,
            int endHour,
            int weight
    ) {
    }

    public record MerchantBrand(
            String name,
            String category
    ) {
    }
}
