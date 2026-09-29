package io.github.prjkmo112.cafeapi.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_002", "서버 내부 오류가 발생했습니다."),
    LOCK_TIMEOUT(HttpStatus.SERVICE_UNAVAILABLE, "COMMON_003", "요청이 몰려 처리하지 못했습니다. 잠시 후 다시 시도해주세요."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_001", "회원을 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "MEMBER_002", "이미 존재하는 이메일입니다."),

    // Point
    INSUFFICIENT_POINT(HttpStatus.CONFLICT, "POINT_001", "포인트가 부족합니다."),
    INVALID_POINT_AMOUNT(HttpStatus.BAD_REQUEST, "POINT_002", "포인트 사용 금액이 올바르지 않습니다."),
    DUPLICATE_POINT_CHARGE_REQUEST(HttpStatus.CONFLICT, "POINT_003", "이미 처리된 충전 요청입니다."),

    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_001", "상품을 찾을 수 없습니다."),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "PRODUCT_002", "재고가 부족합니다."),

    // Order
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "ORDER_001", "유효하지 않은 주문 상태 변경입니다."),
    DUPLICATE_ORDER_REQUEST(HttpStatus.CONFLICT, "ORDER_002", "이미 처리 중인 주문 요청입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}

