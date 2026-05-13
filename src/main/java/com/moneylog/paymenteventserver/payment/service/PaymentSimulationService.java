package com.moneylog.paymenteventserver.payment.service;


import com.moneylog.paymenteventserver.card.entity.Card;
import com.moneylog.paymenteventserver.card.repository.CardRepository;
import com.moneylog.paymenteventserver.payment.client.MainProjectPaymentEventClient;
import com.moneylog.paymenteventserver.payment.dto.PaymentEventPostRequest;
import com.moneylog.paymenteventserver.payment.generator.PaymentSimulationData;
import com.moneylog.paymenteventserver.payment.generator.PaymentSimulationDataLoader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentSimulationService {

    private static final int MAX_BULK_COUNT = 1000;
    private static final int MAX_DAYS_BACK = 30;

    private final CardRepository cardRepository;
    private final PaymentSimulationDataLoader dataLoader;
    private final MainProjectPaymentEventClient mainProjectPaymentEventClient;

    public PaymentEventPostRequest generateOne() {
        List<Card> cards = cardRepository.findByActiveTrue();

        if (cards.isEmpty()) {
            throw new IllegalStateException("등록된 활성 카드가 없습니다.");
        }

        Card card = pickOne(cards);

        return createPaymentEvent(card);
    }

    public PaymentEventPostRequest generateOneAndSend() {
        PaymentEventPostRequest request = generateOne();

        mainProjectPaymentEventClient.sendPaymentEvent(request);

        return request;
    }

    public List<PaymentEventPostRequest> generateBulk(int count) {
        validateCount(count);

        List<Card> cards = cardRepository.findByActiveTrue();

        if (cards.isEmpty()) {
            throw new IllegalStateException("등록된 활성 카드가 없습니다.");
        }

        return ThreadLocalRandom.current()
                .ints(count, 0, cards.size())
                .mapToObj(index -> createPaymentEvent(cards.get(index)))
                .toList();
    }

    public List<PaymentEventPostRequest> generateBulkAndSend(int count) {
        List<PaymentEventPostRequest> requests = generateBulk(count);

        requests.forEach(mainProjectPaymentEventClient::sendPaymentEvent);

        return requests;
    }

    public List<PaymentEventPostRequest> generateBulkByUserId(Long userId, int count) {
        if (userId == null) {
            throw new IllegalArgumentException("userId는 null일 수 없습니다.");
        }

        validateCount(count);

        List<Card> cards = cardRepository.findByUserIdAndActiveTrue(userId);

        if (cards.isEmpty()) {
            throw new IllegalStateException("해당 userId에 등록된 활성 카드가 없습니다. userId=" + userId);
        }

        return ThreadLocalRandom.current()
                .ints(count, 0, cards.size())
                .mapToObj(index -> createPaymentEvent(cards.get(index)))
                .toList();
    }

    public List<PaymentEventPostRequest> generateBulkByUserIdAndSend(Long userId, int count) {
        List<PaymentEventPostRequest> requests = generateBulkByUserId(userId, count);

        requests.forEach(mainProjectPaymentEventClient::sendPaymentEvent);

        return requests;
    }

    private PaymentEventPostRequest createPaymentEvent(Card card) {
        PaymentSimulationData data = dataLoader.getData();

        PaymentSimulationData.MerchantBrand merchant = pickOne(data.merchants());

        PaymentSimulationData.CategoryRule categoryRule = findCategoryRule(
                data,
                merchant.category()
        );

        Long amount = generateAmount(categoryRule);
        LocalDateTime paidAt = generatePaidAt(categoryRule);
        String merchantName = generateMerchantName(merchant, data);

        return new PaymentEventPostRequest(
                UUID.randomUUID().toString(),
                card.getUserId(),
                card.getUserCardId(),
                card.getCardCompany(),
                card.getCardNumberLast4(),
                merchantName,
                merchant.category(),
                amount,
                paidAt
        );
    }

    private PaymentSimulationData.CategoryRule findCategoryRule(
            PaymentSimulationData data,
            String categoryCode
    ) {
        return data.categories()
                .stream()
                .filter(category -> category.code().equals(categoryCode))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "category rule을 찾을 수 없습니다. category=" + categoryCode
                ));
    }

    private Long generateAmount(PaymentSimulationData.CategoryRule categoryRule) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        long minAmount = categoryRule.minAmount();
        long maxAmount = categoryRule.maxAmount();
        int roundUnit = categoryRule.roundUnit();

        long minUnit = (minAmount + roundUnit - 1) / roundUnit;
        long maxUnit = maxAmount / roundUnit;

        if (maxUnit < minUnit) {
            throw new IllegalStateException(
                    "금액 생성 범위가 올바르지 않습니다. category=" + categoryRule.code()
            );
        }

        long selectedUnit = random.nextLong(minUnit, maxUnit + 1);

        return selectedUnit * roundUnit;
    }

    private LocalDateTime generatePaidAt(PaymentSimulationData.CategoryRule categoryRule) {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        PaymentSimulationData.TimeWindow timeWindow = pickWeightedTimeWindow(
                categoryRule.timeWindows()
        );

        int hour = random.nextInt(timeWindow.startHour(), timeWindow.endHour());
        int minute = random.nextInt(0, 60);
        int second = random.nextInt(0, 60);

        int daysBack = random.nextInt(0, MAX_DAYS_BACK + 1);

        return LocalDate.now()
                .minusDays(daysBack)
                .atTime(hour, minute, second);
    }

    private PaymentSimulationData.TimeWindow pickWeightedTimeWindow(
            List<PaymentSimulationData.TimeWindow> timeWindows
    ) {
        int totalWeight = timeWindows.stream()
                .mapToInt(PaymentSimulationData.TimeWindow::weight)
                .sum();

        int pick = ThreadLocalRandom.current().nextInt(totalWeight);

        for (PaymentSimulationData.TimeWindow timeWindow : timeWindows) {
            pick -= timeWindow.weight();

            if (pick < 0) {
                return timeWindow;
            }
        }

        return timeWindows.get(timeWindows.size() - 1);
    }

    private String generateMerchantName(
            PaymentSimulationData.MerchantBrand merchant,
            PaymentSimulationData data
    ) {
        String area = pickOne(data.areas());
        String branchSuffix = pickOne(data.branchSuffixes());

        return merchant.name() + " " + area + branchSuffix;
    }

    private <T> T pickOne(List<T> values) {
        int index = ThreadLocalRandom.current().nextInt(values.size());
        return values.get(index);
    }

    private void validateCount(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("생성 개수는 1개 이상이어야 합니다.");
        }

        if (count > MAX_BULK_COUNT) {
            throw new IllegalArgumentException(
                    "한 번에 생성할 수 있는 최대 개수는 " + MAX_BULK_COUNT + "개입니다."
            );
        }
    }
}