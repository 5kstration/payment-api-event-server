package com.moneylog.paymenteventserver.payment.generator;

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

    /*
    1. application.yml 에 payment-simulation-data.json 파일을 읽음
    2. Json 내용을 PaymentSimulationData record로 변환
    3. Validate() 데이터를 평가
    4. 평가 후 data 필드에 보관
     */
    private final ObjectMapper objectMapper;

    // application.yml 에 경로 변수
    @Value("${simulation.data-file}")
    private Resource dataFile;

    private PaymentSimulationData data;

    // json file loader 메소드
    @PostConstruct
    public void load() {
        try (InputStream inputStream = dataFile.getInputStream()) {
            this.data = objectMapper.readValue(inputStream, PaymentSimulationData.class);
            validate(this.data);
        } catch (Exception e) {
            throw new IllegalStateException("결제 시뮬레이션 데이터 파일을 읽지 못했습니다.", e);
        }
    }
    // validate (): json file vaild 확인 함수
    private void validate(PaymentSimulationData data) {
        if (data == null) {
            throw new IllegalStateException("결제 시뮬레이션 데이터가 null입니다.");
        }

        if (data.categories() == null || data.categories().isEmpty()) {
            throw new IllegalStateException("categories 데이터가 비어 있습니다.");
        }

        if (data.merchants() == null || data.merchants().isEmpty()) {
            throw new IllegalStateException("merchants 데이터가 비어 있습니다.");
        }

        if (data.areas() == null || data.areas().isEmpty()) {
            throw new IllegalStateException("areas 데이터가 비어 있습니다.");
        }

        if (data.branchSuffixes() == null || data.branchSuffixes().isEmpty()) {
            throw new IllegalStateException("branchSuffixes 데이터가 비어 있습니다.");
        }

        data.categories().forEach(category -> {
            if (category.code() == null || category.code().isBlank()) {
                throw new IllegalStateException("category code가 비어 있습니다.");
            }

            if (category.minAmount() < 0) {
                throw new IllegalStateException(
                        "minAmount는 0 이상이어야 합니다. category=" + category.code()
                );
            }

            if (category.maxAmount() < category.minAmount()) {
                throw new IllegalStateException(
                        "maxAmount는 minAmount보다 크거나 같아야 합니다. category=" + category.code()
                );
            }

            if (category.roundUnit() == null || category.roundUnit() <= 0) {
                throw new IllegalStateException(
                        "roundUnit은 1 이상이어야 합니다. category=" + category.code()
                );
            }

            if (category.timeWindows() == null || category.timeWindows().isEmpty()) {
                throw new IllegalStateException(
                        "timeWindows가 비어 있습니다. category=" + category.code()
                );
            }

            category.timeWindows().forEach(timeWindow -> {
                if (timeWindow.startHour() < 0 || timeWindow.startHour() > 23) {
                    throw new IllegalStateException(
                            "startHour는 0~23 사이여야 합니다. category=" + category.code()
                    );
                }

                if (timeWindow.endHour() < 1 || timeWindow.endHour() > 24) {
                    throw new IllegalStateException(
                            "endHour는 1~24 사이여야 합니다. category=" + category.code()
                    );
                }

                if (timeWindow.startHour() >= timeWindow.endHour()) {
                    throw new IllegalStateException(
                            "startHour는 endHour보다 작아야 합니다. category=" + category.code()
                    );
                }

                if (timeWindow.weight() <= 0) {
                    throw new IllegalStateException(
                            "weight는 1 이상이어야 합니다. category=" + category.code()
                    );
                }
            });
        });

        Set<String> categoryCodes = data.categories()
                .stream()
                .map(PaymentSimulationData.CategoryRule::code)
                .collect(Collectors.toSet());

        if (categoryCodes.size() != data.categories().size()) {
            throw new IllegalStateException("중복된 category code가 존재합니다.");
        }

        data.merchants().forEach(merchant -> {
            if (merchant.name() == null || merchant.name().isBlank()) {
                throw new IllegalStateException("merchant name이 비어 있습니다.");
            }

            if (merchant.category() == null || merchant.category().isBlank()) {
                throw new IllegalStateException(
                        "merchant category가 비어 있습니다. merchant=" + merchant.name()
                );
            }

            if (!categoryCodes.contains(merchant.category())) {
                throw new IllegalStateException(
                        "존재하지 않는 category를 사용하는 merchant가 있습니다. merchant="
                                + merchant.name()
                                + ", category="
                                + merchant.category()
                );
            }
        });

        data.areas().forEach(area -> {
            if (area == null || area.isBlank()) {
                throw new IllegalStateException("areas에 빈 값이 있습니다.");
            }
        });

        data.branchSuffixes().forEach(branchSuffix -> {
            if (branchSuffix == null || branchSuffix.isBlank()) {
                throw new IllegalStateException("branchSuffixes에 빈 값이 있습니다.");
            }
        });
    }
}

