package io.hhplus.tdd;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INVALID_USER_ID("USER01", "유효하지 않은 유저ID"),
    USER_NOT_FOUND("USER02", "존재하지 않는 유저"),
    INPUT_POINT_EXCEEDED("POINT01", "충전 가능한 포인트를 초과하였습니다."),
    INVALID_POINT_INPUT("POINT02", "유효하지 않은 포인트 요청"),
    POINT_INSUFFICIENT("POINT03", "잔액이 부족하여 포인트를 사용할 수 없습니다.");

    private String code;
    private String message;
}
