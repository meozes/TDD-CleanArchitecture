package io.hhplus.tdd;

public record ErrorResponse(
        String code,
        String message
) {
    public static ErrorResponse error(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getCode(), errorCode.getMessage());
    }
}
