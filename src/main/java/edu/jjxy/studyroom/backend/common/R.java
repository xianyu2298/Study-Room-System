package edu.jjxy.studyroom.backend.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一响应结果封装
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 状态码
     * 0=成功，其他=失败
     */
    private int code;

    /**
     * 消息
     */
    private String msg;

    /**
     * 数据
     */
    private T data;

    /**
     * 时间戳
     */
    private long timestamp;

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return this.code == 0;
    }

    // ==================== 成功响应 ====================

    /**
     * 成功响应（无数据）
     */
    public static <T> R<T> ok() {
        return ok(null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> R<T> ok(T data) {
        return ok("操作成功", data);
    }

    /**
     * 成功响应（带消息）
     */
    public static <T> R<T> ok(String msg, T data) {
        return new R<>(0, msg, data, System.currentTimeMillis());
    }

    // ==================== 失败响应 ====================

    /**
     * 失败响应（使用默认错误码）
     */
    public static <T> R<T> error() {
        return error(ResultCode.INTERNAL_SERVER_ERROR);
    }

    /**
     * 失败响应（使用指定错误码）
     */
    public static <T> R<T> error(ResultCode resultCode) {
        return new R<>(resultCode.getCode(), resultCode.getMessage(), null, System.currentTimeMillis());
    }

    /**
     * 失败响应（使用指定消息）
     */
    public static <T> R<T> error(String message) {
        return new R<>(ResultCode.INTERNAL_SERVER_ERROR.getCode(), message, null, System.currentTimeMillis());
    }

    /**
     * 失败响应（使用指定错误码和消息）
     */
    public static <T> R<T> error(ResultCode resultCode, String message) {
        return new R<>(resultCode.getCode(), message, null, System.currentTimeMillis());
    }

    /**
     * 失败响应（使用指定错误码和消息）
     */
    public static <T> R<T> error(int code, String message) {
        return new R<>(code, message, null, System.currentTimeMillis());
    }

    // ==================== 链式调用 ====================

    /**
     * 设置消息
     */
    public R<T> message(String message) {
        this.msg = message;
        return this;
    }

    /**
     * 设置数据
     */
    public R<T> data(T data) {
        this.data = data;
        return this;
    }

    /**
     * 设置错误码
     */
    public R<T> code(int code) {
        this.code = code;
        return this;
    }
}
