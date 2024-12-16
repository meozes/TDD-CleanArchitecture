package io.hhplus.tdd;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND("USER01", "user_not_found"),
    POINT_EXCEEDED("POINT01", "point_exceeded"),
    INVALID_POINT_INPUT("INPUT01", "invalid_point_input");

    private String code;
    private String message;
}
