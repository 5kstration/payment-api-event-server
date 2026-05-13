package com.moneylog.paymenteventserver.payment.generator;

import java.util.List;

// json file 구조를 java Record로 매핑하는 클래스
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