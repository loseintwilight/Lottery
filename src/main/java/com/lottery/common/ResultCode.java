package com.lottery.common;

public enum ResultCode {
    SUCCESS(200, "success"),
    ERROR(500, "server error"),
    INVALID_PARAM(400, "invalid parameter"),
    USER_EXISTS(1001, "username already exists"),
    USER_NOT_FOUND(1002, "user not found"),
    PASSWORD_ERROR(1003, "password error"),
    BALANCE_NOT_ENOUGH(1004, "balance not enough"),
    DRAW_NOT_FOUND(2001, "draw not found"),
    DRAW_ALREADY_EXISTS(2002, "draw already exists"),
    TICKET_NOT_FOUND(3001, "ticket not found");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}