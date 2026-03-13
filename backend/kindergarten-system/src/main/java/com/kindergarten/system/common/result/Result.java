/**
 * 统一返回结果封装类
 * <p>
 * 用于封装所有 API 接口的返回结果，统一格式：
 * {
 *   "code": 0,
 *   "message": "ok",
 *   "data": {}
 * }
 * </p>
 *
 * @param <T> 返回数据类型
 * @author Kindergarten System
 * @since 1.0.0
 */
package com.kindergarten.system.common.result;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 状态码：0-成功，其他-失败 */
    private Integer code;

    /** 提示信息 */
    private String message;

    /** 返回数据 */
    private T data;

    public Result() {}

    public Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), null);
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败响应（默认错误）
     */
    public static <T> Result<T> error() {
        return new Result<>(ResultCode.ERROR.getCode(), ResultCode.ERROR.getMessage(), null);
    }

    /**
     * 失败响应（自定义消息）
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCode.ERROR.getCode(), message, null);
    }

    /**
     * 失败响应（使用错误码枚举）
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    /**
     * 失败响应（使用错误码枚举 + 自定义消息）
     */
    public static <T> Result<T> failure(ResultCode resultCode, String message) {
        String finalMessage = message != null ? message : resultCode.getMessage();
        return new Result<>(resultCode.getCode(), finalMessage, null);
    }

    /**
     * 失败响应（自定义状态码和消息）
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message, null);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }
}
