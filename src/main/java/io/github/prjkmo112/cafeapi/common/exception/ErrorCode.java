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
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "COMMON_003", "접근 권한이 없습니다."),

    // Member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBER_001", "회원을 찾을 수 없습니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "MEMBER_002", "이미 존재하는 이메일입니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "MEMBER_003", "이메일 또는 비밀번호가 올바르지 않습니다."),

    // Point
    INSUFFICIENT_POINT(HttpStatus.CONFLICT, "POINT_001", "포인트가 부족합니다."),
    INVALID_POINT_AMOUNT(HttpStatus.BAD_REQUEST, "POINT_002", "포인트 사용 금액이 올바르지 않습니다."),
    DUPLICATE_POINT_CHARGE_REQUEST(HttpStatus.CONFLICT, "POINT_003", "이미 처리된 충전 요청입니다."),

    // Product
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRODUCT_001", "상품을 찾을 수 없습니다."),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "PRODUCT_002", "재고가 부족합니다."),
    INVALID_PRICE(HttpStatus.BAD_REQUEST, "PRODUCT_003", "가격은 0 이상이어야 합니다."),

    // Order
    ORDER_ITEMS_EMPTY(HttpStatus.BAD_REQUEST, "ORDER_001", "주문할 상품이 없습니다."),
    INVALID_ORDER_ITEM_SELECTION(HttpStatus.BAD_REQUEST, "ORDER_002", "주문할 수 없는 상품이 포함되어 있습니다."),
    DUPLICATE_ORDER_ITEM_SELECTION(HttpStatus.BAD_REQUEST, "ORDER_003", "주문 대상 상품이 중복 선택되었습니다."),
    ORDER_STOCK_INSUFFICIENT(HttpStatus.CONFLICT, "ORDER_004", "주문 상품의 재고가 부족합니다."),
    ORDER_MEMBER_ID_REQUIRED(HttpStatus.BAD_REQUEST, "ORDER_005", "회원 ID는 필수입니다."),
    ORDER_NUMBER_REQUIRED(HttpStatus.BAD_REQUEST, "ORDER_006", "주문번호는 필수입니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_007", "주문을 찾을 수 없습니다."),
    ALREADY_ORDER_CANCELED(HttpStatus.CONFLICT, "ORDER_008", "이미 취소된 주문입니다."),
    INVALID_POINT_USAGE(HttpStatus.BAD_REQUEST, "ORDER_009", "사용할 포인트 금액이 올바르지 않습니다."),
    INVALID_ORDER_STATUS(HttpStatus.BAD_REQUEST, "ORDER_010", "유효하지 않은 주문 상태 변경입니다."),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증이 필요합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}

