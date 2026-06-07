package com.project.backend.payment.service;

import com.project.backend.card.entity.Card;
import com.project.backend.card.repository.CardRepository;
import com.project.backend.payment.dto.PaymentEventPostRequest;
import com.project.backend.payment.entity.PaymentEvent;
import com.project.backend.payment.generator.PaymentSimulationData;
import com.project.backend.payment.generator.PaymentSimulationDataLoader;
import com.project.backend.payment.repository.PaymentEventRepository;
import com.project.backend.payment.repository.UserPaymentStateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentSimulationService {

    private static final int MAX_BULK_COUNT = 1000;
    private static final int MAX_DAYS_BACK = 30;
    private static final int FIRST_PAYMENT_HOUR = 6;
    private static final int LAST_PAYMENT_EXCLUSIVE_HOUR = 24;

    private final CardRepository cardRepository;
    private final PaymentEventRepository paymentEventRepository;
    private final UserPaymentStateRepository userPaymentStateRepository;
    private final PaymentSimulationDataLoader dataLoader;
    private final PaymentEventDeliveryService paymentEventDeliveryService;

    @Transactional
    public PaymentEventPostRequest generateOne() {
        Card card = pickRegisteredCard();
        PaymentEvent event = createAndSavePaymentEvent(card);
        return event.toPostRequest();
    }

    @Transactional
    public PaymentEventPostRequest generateOneAndSend() {
        Card card = pickRegisteredCard();
        PaymentEvent event = createAndSavePaymentEvent(card);
        sendToBudgetIfEnabled(event);
        return event.toPostRequest();
    }

    @Transactional
    public List<PaymentEventPostRequest> generateBulk(int count) {
        validateCount(count);
        List<Card> cards = getRegisteredCards();

        return ThreadLocalRandom.current()
                .ints(count, 0, cards.size())
                .mapToObj(index -> createAndSavePaymentEvent(cards.get(index)).toPostRequest())
                .toList();
    }

    @Transactional
    public List<PaymentEventPostRequest> generateBulkAndSend(int count) {
        validateCount(count);
        List<Card> cards = getRegisteredCards();

        return ThreadLocalRandom.current()
                .ints(count, 0, cards.size())
                .mapToObj(index -> {
                    PaymentEvent event = createAndSavePaymentEvent(cards.get(index));
                    sendToBudgetIfEnabled(event);
                    return event.toPostRequest();
                })
                .toList();
    }

    @Transactional
    public List<PaymentEventPostRequest> generateBulkByUserId(String userId, int count) {
        validateUserId(userId);
        validateCount(count);
        Card card = getRegisteredCardByUserId(userId);

        return ThreadLocalRandom.current()
                .ints(count, 0, 1)
                .mapToObj(index -> createAndSavePaymentEvent(card).toPostRequest())
                .toList();
    }

    @Transactional
    public List<PaymentEventPostRequest> generateBulkByUserIdAndSend(String userId, int count) {
        validateUserId(userId);
        validateCount(count);
        Card card = getRegisteredCardByUserId(userId);

        return ThreadLocalRandom.current()
                .ints(count, 0, 1)
                .mapToObj(index -> {
                    PaymentEvent event = createAndSavePaymentEvent(card);
                    sendToBudgetIfEnabled(event);
                    return event.toPostRequest();
                })
                .toList();
    }

    private Card pickRegisteredCard() {
        List<Card> cards = getRegisteredCards();
        return pickOne(cards);
    }

    private List<Card> getRegisteredCards() {
        List<Card> cards = cardRepository.findAll();
        if (cards.isEmpty()) {
            throw new IllegalStateException("No registered cards are available.");
        }
        return cards;
    }

    private Card getRegisteredCardByUserId(String userId) {
        return cardRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No registered card is available. userId=" + userId));
    }

    private PaymentEvent createAndSavePaymentEvent(Card card) {
        PaymentSimulationData data = dataLoader.getData();
        PaymentSimulationData.MerchantBrand merchant = pickOne(data.merchants());
        PaymentSimulationData.CategoryRule categoryRule = findCategoryRule(data, merchant.category());

        PaymentEvent event = PaymentEvent.create(
                card,
                generateMerchantName(merchant, data),
                merchant.category(),
                generateAmount(categoryRule),
                generatePaidAt(categoryRule)
        );
        return paymentEventRepository.save(event);
    }

    private void sendToBudgetIfEnabled(PaymentEvent event) {
        if (!userPaymentStateRepository.existsByUserIdAndBudgetSyncEnabledTrue(event.getUserId())) {
            return;
        }

        try {
            paymentEventDeliveryService.dispatch(event);
        } catch (RuntimeException e) {
            log.warn(
                    "Failed to send generated payment event to budget. userId={}, externalPaymentEventId={}",
                    event.getUserId(),
                    event.getExternalPaymentEventId(),
                    e
            );
        }
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
                        "Category rule is not configured. category=" + categoryCode
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
                    "Invalid amount generation range. category=" + categoryRule.code()
            );
        }

        long selectedUnit = random.nextLong(minUnit, maxUnit + 1);
        return selectedUnit * roundUnit;
    }

    private LocalDateTime generatePaidAt(PaymentSimulationData.CategoryRule categoryRule) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        PaymentSimulationData.TimeWindow timeWindow = pickWeightedTimeWindow(getAwakeTimeWindows(categoryRule));

        int hour = random.nextInt(timeWindow.startHour(), timeWindow.endHour());
        int minute = random.nextInt(0, 60);
        int second = random.nextInt(0, 60);
        int daysBack = random.nextInt(0, MAX_DAYS_BACK + 1);

        return LocalDate.now()
                .minusDays(daysBack)
                .atTime(hour, minute, second);
    }

    private List<PaymentSimulationData.TimeWindow> getAwakeTimeWindows(
            PaymentSimulationData.CategoryRule categoryRule
    ) {
        List<PaymentSimulationData.TimeWindow> awakeTimeWindows = new ArrayList<>();

        for (PaymentSimulationData.TimeWindow timeWindow : categoryRule.timeWindows()) {
            int startHour = Math.max(timeWindow.startHour(), FIRST_PAYMENT_HOUR);
            int endHour = Math.min(timeWindow.endHour(), LAST_PAYMENT_EXCLUSIVE_HOUR);

            if (startHour < endHour) {
                awakeTimeWindows.add(new PaymentSimulationData.TimeWindow(
                        startHour,
                        endHour,
                        timeWindow.weight()
                ));
            }
        }

        if (awakeTimeWindows.isEmpty()) {
            throw new IllegalStateException(
                    "No awake payment time windows are configured. category=" + categoryRule.code()
            );
        }

        return awakeTimeWindows;
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

    private void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId must not be blank.");
        }
    }

    private void validateCount(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count must be greater than or equal to 1.");
        }

        if (count > MAX_BULK_COUNT) {
            throw new IllegalArgumentException("count must be less than or equal to " + MAX_BULK_COUNT + ".");
        }
    }
}
