package com.lottery.pojo.vo;

import com.lottery.common.ResultCode;
import lombok.Data;

@Data
public class R<T> {
    private int code;
    private String message;
    private T data;

    private R() {
    }

    public static <T> R<T> success(T data) {
        R<T> r = new R<>();
        r.code = ResultCode.SUCCESS.getCode();
        r.message = ResultCode.SUCCESS.getMessage();
        r.data = data;
        return r;
    }

    public static <T> R<T> success() {
        return success(null);
    }

    public static <T> R<T> error(ResultCode resultCode) {
        R<T> r = new R<>();
        r.code = resultCode.getCode();
        r.message = resultCode.getMessage();
        return r;
    }

    public static <T> R<T> error(ResultCode resultCode, String message) {
        R<T> r = new R<>();
        r.code = resultCode.getCode();
        r.message = message;
        return r;
    }

    public static <T> R<T> error(int code, String message) {
        R<T> r = new R<>();
        r.code = code;
        r.message = message;
        return r;
    }
}