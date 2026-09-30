# 📘 API 명세서

| 기능 | Method | URI | 구현 여부 |
|---|---|---|---|
| 메뉴 목록 조회 (검색, 필터, 페이징) | `GET` | `/api/menus` | ✅ 구현됨 |
| 인기 메뉴 목록 조회 | `GET` | `/api/menus/popular` | ✅ 구현됨 |
| 포인트 충전 | `POST` | `/api/points/charge` | ✅ 구현됨 |
| 커피 주문/결제 | `POST` | `/api/orders` | ✅ 구현됨 |
| 사용자 등록 | `POST` | `/api/users/register` | ✅ 구현됨 (과제 필수 항목은 아니며, 테스트용 사용자 생성을 위해 추가) |

## 공통 규칙

- 요청/응답 본문은 `application/json`이며, 금액은 원 단위 정수입니다.
- 모든 응답은 아래와 같은 공통 래퍼(`ApiResponse`)로 감싸집니다. `data`는 값이 없으면(`null`) 응답 본문에서 생략됩니다.

성공 응답 형식:

```json
{
  "code": "SUCCESS",
  "data": { }
}
```

에러 응답 형식 (`GlobalExceptionHandler` / `BusinessException` 기준):

```json
{
  "code": "에러 코드",
  "message": "입력값이 올바르지 않습니다."
}
```

| 상황 | HTTP | code |
|---|---|---|
| Bean Validation 실패(`@Valid`) | 400 | `INVALID_INPUT` |
| 요청 바디 파싱 실패 | 400 | `INVALID_INPUT` |
| 쿼리 파라미터 타입 불일치 | 400 | `INVALID_INPUT` |
| 비즈니스 예외(`BusinessException`) | 예외별 `ErrorCode.status` | 예외별 `ErrorCode.code` |
| 비관적 락 대기 시간 초과 | 503 | `LOCK_TIMEOUT` |
| 그 외 처리되지 않은 예외 | 500 | `INTERNAL_ERROR` |

전체 에러 코드는 [`ErrorCode.java`](../src/main/java/io/github/prjkmo112/cafeapi/common/exception/ErrorCode.java)에 정의되어 있습니다.

---

## 1. 메뉴 목록 조회

`GET /api/menus`

검색어, 가격 범위, 상태, 등록일 범위로 메뉴를 필터링하여 페이지 단위로 조회합니다. 모든 쿼리 파라미터는 선택 값이며, 아무 조건도 주지 않으면 전체 메뉴를 페이징하여 반환합니다.

**Request Query Parameter**

| 이름 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `keyword` | String | X | 메뉴 이름에 포함된 문자열 검색 (대소문자 무시) |
| `priceStart` | Long | X | 최소 가격 (원) |
| `priceEnd` | Long | X | 최대 가격 (원) |
| `status` | String | X | 메뉴 상태 (`SALE`, `SOLDOUT`) |
| `createdAtStart` | String(LocalDateTime) | X | 등록일 검색 시작. 형식은 `yyyy-MM-dd HH:mm:ss` (예: `2026-09-01 00:00:00`, 소수 초는 선택) |
| `createdAtEnd` | String(LocalDateTime) | X | 등록일 검색 종료. 형식은 시작과 동일 |
| `page` | Integer | X | 페이지 번호 (0부터, 기본값 0) |
| `size` | Integer | X | 페이지 크기 (기본값 10) |
| `sort` | String | X | 정렬 기준 `필드,방향` (예: `sort=price,desc`). 여러 개 지정 가능 |

- 정렬 가능한 필드는 `name`, `price`, `status`, `createdAt`이며, 그 외 값은 400(`INVALID_INPUT`)입니다.
- 정렬 값이 같은 메뉴끼리도 순서가 흔들리지 않도록 항상 마지막에 `id` 오름차순이 추가됩니다. `sort`를 주지 않으면 `id` 오름차순입니다.
- 응답의 `createdAt`은 ISO 형식(`2026-09-22T14:00:00`)으로 내려가며, 요청 파라미터 형식과 다릅니다.

**Response** `200 OK`

```json
{
  "code": "SUCCESS",
  "data": {
    "content": [
      { "id": 1, "name": "아메리카노", "price": 4000, "status": "SALE", "createdAt": "2026-09-22T14:00:00" },
      { "id": 2, "name": "카페라떼", "price": 4500, "status": "SALE", "createdAt": "2026-09-22T14:00:00" }
    ],
    "page": 0,
    "size": 10,
    "totalElements": 2,
    "totalPages": 1,
    "hasNext": false
  }
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `data.content[].id` | Long | 메뉴 ID |
| `data.content[].name` | String | 메뉴 이름 |
| `data.content[].price` | Long | 가격 (원) |
| `data.content[].status` | String | 판매 상태 (`SALE`: 판매중, `SOLDOUT`: 품절) |
| `data.content[].createdAt` | String | 메뉴 등록 일시 |
| `data.page` / `size` / `totalElements` / `totalPages` / `hasNext` | - | 페이지 정보 |

**Error**

| HTTP | code | 상황 |
|---|---|---|
| 400 | `INVALID_INPUT` | 쿼리 파라미터 형식 오류 (예: `status`에 존재하지 않는 값, 날짜 형식 불일치, 정렬이 허용되지 않는 필드) |

---

## 2. 인기 메뉴 목록 조회

`GET /api/menus/popular`

최근 7일간(`status`가 `CANCELED`가 아닌 주문 기준) 주문 횟수 상위 3개 메뉴를 반환합니다. 기간, 개수는 요구사항에 고정된 값이라 클라이언트가 바꿀 수 없습니다.

**Response** `200 OK`

```json
{
  "code": "SUCCESS",
  "data": [
    { "menuId": 1, "name": "아메리카노", "price": 4000, "count": 26 },
    { "menuId": 3, "name": "카푸치노", "price": 4500, "count": 5 },
    { "menuId": 2, "name": "카페라떼", "price": 4500, "count": 2 }
  ]
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `data[].menuId` | Long | 메뉴 ID |
| `data[].name` | String | 메뉴 이름 |
| `data[].price` | Long | 가격 (원) |
| `data[].count` | Long | 최근 7일간 주문 횟수 |

**정확성과 성능**: 원본은 항상 `orders` 테이블 직접 집계이며, Redis에는 계산 결과를 최대 10분(TTL) 동안만 캐시합니다. 또한 주문이 성공적으로 커밋될 때마다 캐시를 즉시 비워서, 다음 조회는 최신 데이터로 다시 계산됩니다. 이 무효화는 별도 스레드가 아니라 요청 스레드에서 동기로 실행되어 주문 응답이 나가기 전에 끝납니다(Redis 연결/명령 타임아웃 각 2초). Redis가 죽어 있어도(직접 재현해 확인) 이 API는 DB로 폴백되어 정상 응답합니다.

---

## 3. 포인트 충전

`POST /api/points/charge`

결제는 포인트로만 가능하며, 사용자 식별값과 충전금액을 입력받아 포인트를 충전합니다(1원 = 1P). 같은 `idempotencyKey`로 재요청하면 중복 충전되지 않습니다.

**Request Body**

```json
{
  "userId": 1,
  "point": 5000,
  "idempotencyKey": "client-generated-uuid"
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `userId` | Long | O | 사용자 ID |
| `point` | Long | O | 충전 금액 (1 이상) |
| `idempotencyKey` | String | O | 클라이언트가 생성하는 충전 요청 고유 키. 재시도 시 동일한 값을 그대로 보내야 함 |

**Response** `200 OK`

```json
{
  "code": "SUCCESS",
  "data": { "userId": 1, "point": 5000 }
}
```

| 필드 | 타입 | 설명 |
|---|---|---|
| `data.userId` | Long | 사용자 ID |
| `data.point` | Long | 충전 후 잔액 |

**Error**

| HTTP | code | 상황 |
|---|---|---|
| 404 | `MEMBER_NOT_FOUND` | 존재하지 않는 사용자 |
| 400 | `INVALID_INPUT` | 입력값 검증 실패(`userId`/`point`가 1 미만, `idempotencyKey`가 비어 있음 등). 충전 금액 0 이하는 검증 단계에서 먼저 걸러지며, `INVALID_POINT_AMOUNT`는 도메인 로직의 방어선이라 정상 경로에서는 노출되지 않음 |
| 409 | `DUPLICATE_POINT_CHARGE_REQUEST` | 이미 처리된 충전 요청(같은 `idempotencyKey`로 재요청, 동시 요청 포함) |

---

## 4. 커피 주문/결제

`POST /api/orders`

사용자 식별값과 메뉴 ID를 입력받아 주문하고 결제합니다. 포인트에서 메뉴 가격만큼 차감하며, 주문 저장과 포인트 차감은 하나의 트랜잭션입니다. 결제가 완료되면 사용자 식별값, 메뉴 ID, 결제금액을 Kafka(`order-paid` 토픽)로 발행해 데이터 수집 플랫폼(Mock 소비자로 대체) 쪽에 실시간으로 전달합니다.

같은 `idempotencyKey`로 재요청하면 새로 주문하지 않고 **기존 주문을 그대로 반환**하며 포인트도 다시 차감하지 않습니다. 키는 사용자 단위로 관리됩니다.

**Request Body**

```json
{
  "userId": 1,
  "menuId": 1,
  "idempotencyKey": "order-20260929-0001"
}
```

| 필드 | 타입 | 필수 | 설명 |
|---|---|---|---|
| `userId` | Long | O | 사용자 ID |
| `menuId` | Long | O | 메뉴 ID |
| `idempotencyKey` | String | O | 요청 단위 고유 키(최대 64자). 재시도 시 같은 값을 보내야 함 |

**Response** `200 OK`

```json
{
  "code": "SUCCESS",
  "data": {
    "userId": 1,
    "menuId": 1,
    "orderId": "ORD-20260929075403-B4DDDAF5",
    "amount": 4000,
    "status": "PAID"
  }
}
```

**Error**

| HTTP | code | 상황 |
|---|---|---|
| 404 | `MEMBER_NOT_FOUND` | 존재하지 않는 사용자 |
| 404 | `PRODUCT_NOT_FOUND` | 존재하지 않는 메뉴 |
| 409 | `INSUFFICIENT_STOCK` | 품절된 메뉴(`SOLDOUT`). 메시지는 "재고가 부족합니다." |
| 409 | `INSUFFICIENT_POINT` | 포인트 부족 |
| 400 | `INVALID_INPUT` | 입력값 검증 실패(`idempotencyKey` 누락, 빈 값, 64자 초과 등) |
| 409 | `DUPLICATE_ORDER_REQUEST` | 같은 `idempotencyKey`의 동시 요청이 먼저 처리 중. 잠시 후 같은 키로 재요청하면 기존 주문이 반환됨 |
| 503 | `LOCK_TIMEOUT` | 같은 사용자의 요청이 몰려 락 대기 시간(3초)을 초과. 같은 키로 재시도 가능 |

**실시간 전송 구조**: 결제 트랜잭션 안에서 Kafka로 직접 호출하지 않습니다. `OrderService`가 주문 저장 직후 `ApplicationEventPublisher`로 `OrderPaidEvent`를 발행하고, `OrderProducer`가 트랜잭션이 **커밋된 후에만**(`@TransactionalEventListener(AFTER_COMMIT)`) 그 이벤트를 받아 Kafka로 전송합니다. `@Async`(전용 스레드 풀 `kafkaPublishExecutor`, 스레드 이름 접두사 `kafka-pub-`)로 별도 스레드에서 처리되어, Kafka가 느려지거나 죽어 있어도 주문 API 응답에는 영향이 없습니다. 풀의 큐(100건)까지 가득 차면 새 이벤트는 로그를 남기고 버려집니다(`DiscardPolicy`).

---

## 5. 사용자 등록 (참고)

`POST /api/users/register`

과제 필수 API는 아니지만, 주문/충전 테스트를 위해 사용자를 만드는 용도로 구현했습니다.

```json
{ "name": "홍길동", "email": "hong@example.com", "password": "password1234" }
```

가입과 동시에 포인트 잔액 0원인 `user_point` 행이 함께 생성됩니다.

**Response** `200 OK`

```json
{
  "code": "SUCCESS",
  "data": { "name": "홍길동", "email": "hong@example.com" }
}
```

| HTTP | code | 상황 |
|---|---|---|
| 400 | `INVALID_INPUT` | 필수값 누락, 이메일 형식 오류 |
| 409 | `DUPLICATE_EMAIL` | 이미 존재하는 이메일(동시 가입 요청 포함) |
