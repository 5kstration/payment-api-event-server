# Payment API Event Server

Spring Boot 기반 결제 이벤트 시뮬레이션 마이크로서비스입니다. 현재 PostgreSQL, NATS JetStream, Spring Scheduler를 사용하여 카드 등록 상태를 기준으로 결제 이벤트를 생성하고, 예산/가계부 서비스로 결제 이벤트를 전달합니다.

## 기술 스택

- Java 17
- Spring Boot 3.5.14
- Spring Data JPA
- PostgreSQL
- NATS JetStream
- Spring Scheduler
- Spring Web `RestClient`
- Springdoc OpenAPI / Swagger UI
- Micrometer / Prometheus
- JUnit 5
- SonarQube

## 현재 구현 상태

### 카드 관리 (Card)

- 온보딩/내부 서비스에서 전달받은 카드 정보를 등록합니다.
- 사용자별 활성 카드 정보를 관리합니다.
- 동일 사용자에게 새 카드가 등록되면 기존 카드 및 결제 연동 상태를 정리한 뒤 새 카드로 교체합니다.
- 카드 삭제 시 사용자 결제 동기화 상태와 결제 관련 데이터를 초기화합니다.
- `cardLast4`는 4자리 숫자 형식으로 검증합니다.

### 결제 이벤트 시뮬레이션 (Payment Event)

- 등록된 카드 정보를 기반으로 결제 이벤트를 생성합니다.
- 시뮬레이션 데이터 파일의 가맹점, 카테고리, 금액 범위, 시간대 가중치를 사용하여 결제 데이터를 생성합니다.
- 단건/벌크 결제 이벤트 생성을 지원합니다.
- 특정 사용자 기준의 벌크 결제 이벤트 생성을 지원합니다.
- 새 카드 등록 시 과거 결제 내역 백필 이벤트를 생성하여 예산 서비스로 전달합니다.
- 매일 00:00-06:00 사이에는 스케줄러 기반 자동 생성이 중단됩니다.

### 예산 서비스 연동

- 사용자별 예산 동기화 활성화/비활성화 상태를 관리합니다.
- 예산 동기화가 활성화된 사용자에 대해서만 결제 이벤트를 예산 서비스로 전달합니다.
- NATS JetStream이 비활성화된 경우 `connector-service.payment-event-url`로 HTTP POST 요청을 보냅니다.
- NATS JetStream이 활성화된 경우 Outbox 테이블에 이벤트를 저장하고, 별도 스케줄러가 JetStream subject로 발행합니다.

### Outbox 기반 이벤트 발행

- 결제 이벤트 발행 요청을 `payment_event_outbox` 테이블에 먼저 저장합니다.
- `PENDING` 상태 이벤트를 주기적으로 조회하여 NATS JetStream으로 발행합니다.
- 발행 성공 시 Outbox 상태를 `PUBLISHED`로 변경하고 결제 이벤트의 전송 상태를 갱신합니다.
- 발행 실패 시 재시도 횟수와 마지막 에러 메시지를 저장합니다.

## 주요 API

| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/internal/v1/cards` | 카드 등록/동기화 |
| GET | `/internal/v1/cards` | 등록된 카드 목록 조회 |
| DELETE | `/internal/v1/cards/{cardId}` | 카드 삭제 및 사용자 결제 상태 초기화 |
| POST | `/internal/v1/users/{userId}/budget-sync/activate` | 사용자 예산 동기화 활성화 |
| POST | `/internal/v1/users/{userId}/budget-sync/deactivate` | 사용자 예산 동기화 비활성화 |
| POST | `/api/v1/payment-simulations/generate` | 결제 이벤트 단건 생성 |
| POST | `/api/v1/payment-simulations/send` | 결제 이벤트 단건 생성 후 전송 |
| POST | `/api/v1/payment-simulations/generate/bulk?count=10` | 결제 이벤트 벌크 생성 |
| POST | `/api/v1/payment-simulations/send/bulk?count=10` | 결제 이벤트 벌크 생성 후 전송 |
| POST | `/api/v1/payment-simulations/users/{userId}/send/bulk?count=10` | 특정 사용자 결제 이벤트 벌크 생성 후 전송 |

## 로컬 실행

### 필수 인프라

- PostgreSQL
- NATS Server (JetStream 사용 시)
- 예산/가계부 서비스 또는 결제 이벤트 수신용 테스트 서버

`src/main/resources/application.yml`에는 실제 운영 비밀값을 넣지 않습니다. 로컬/운영 실행 시 환경변수, Secret/ConfigMap 또는 Config Server를 통해 주입합니다.

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:5432/payment_event_db"
$env:SPRING_DATASOURCE_USERNAME="postgres"
$env:SPRING_DATASOURCE_PASSWORD="postgres"
$env:SPRING_JPA_HIBERNATE_DDL_AUTO="update"

$env:SIMULATION_FIXED_DELAY_MS="60000"
$env:SIMULATION_DATA_FILE="classpath:simulation/payment-simulation-data.json"

$env:CONNECTOR_SERVICE_PAYMENT_EVENT_URL="http://localhost:18080/internal/v1/payment-events"

$env:PAYMENT_EVENT_MESSAGING_NATS_ENABLED="false"
$env:PAYMENT_EVENT_MESSAGING_NATS_URL="nats://localhost:4222"
$env:PAYMENT_EVENT_MESSAGING_NATS_STREAM="PAYMENT_EVENTS"
$env:PAYMENT_EVENT_MESSAGING_NATS_SUBJECT="payment.events.created"
```

실행:

```powershell
.\gradlew.bat bootRun
```

## DB 적재 방식

### cards

카드 등록 API를 통해 저장되는 사용자 카드 정보입니다. 현재 사용자당 하나의 카드만 활성 카드로 관리합니다.

```text
card_id       = 카드 ID
user_id       = 사용자 ID
card_name     = 카드명
card_company  = 카드사
card_last4    = 카드 번호 마지막 4자리
active        = 활성 여부
registered_at = 등록 일시
```

### user_payment_states

사용자별 예산 동기화 상태를 저장합니다.

```text
user_id              = 사용자 ID
budget_sync_enabled  = 예산 동기화 활성화 여부
activated_at         = 동기화 활성화 일시
deactivated_at       = 동기화 비활성화 일시
last_flushed_at      = 마지막 결제 이벤트 flush 일시
backfilled_at        = 과거 결제 내역 백필 완료 일시
created_at           = 생성 일시
updated_at           = 수정 일시
```

### payment_events

시뮬레이션으로 생성된 결제 이벤트가 저장됩니다.

```text
payment_event_id          = 내부 결제 이벤트 ID
external_payment_event_id = 외부 전달용 결제 이벤트 ID
user_id                   = 결제 사용자 ID
card_id                   = 카드 ID
card_name                 = 카드명
card_company              = 카드사
card_last4                = 카드 번호 마지막 4자리
merchant_name             = 가맹점명
category                  = 결제 카테고리
amount                    = 결제 금액
paid_at                   = 결제 일시
generation_type           = 생성 타입 (SCHEDULED, BACKFILL)
sent_to_budget            = 예산 서비스 전송 여부
sent_to_budget_at         = 예산 서비스 전송 일시
created_at                = 생성 일시
```

### payment_event_outbox

NATS JetStream 발행을 위한 Outbox 이벤트가 저장됩니다.

```text
outbox_id                 = Outbox ID
payment_event_id          = 내부 결제 이벤트 ID
external_payment_event_id = 외부 전달용 결제 이벤트 ID
subject                   = NATS subject
payload                   = 발행 payload
status                    = 발행 상태 (PENDING, PUBLISHED)
publish_attempts          = 발행 시도 횟수
last_error                = 마지막 발행 실패 메시지
created_at                = 생성 일시
updated_at                = 수정 일시
published_at              = 발행 완료 일시
```

## Swagger

서버 실행 후 아래 경로에서 API 명세를 확인할 수 있습니다.

```text
http://<payment-api-event-server-host>/swagger-ui.html
```

## Actuator / Prometheus

Spring Boot Actuator와 Prometheus registry가 포함되어 있습니다. 운영 환경에서는 필요한 actuator endpoint만 노출하도록 설정합니다.

```text
http://<payment-api-event-server-host>/actuator
http://<payment-api-event-server-host>/actuator/prometheus
```

## 테스트

```powershell
.\gradlew.bat test
```
