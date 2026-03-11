/**
 * 业务异常类
 * <p>
 * 用于封装业务逻辑中的异常，包含错误码和错误信息
 * 可被 {@link GlobalExceptionHandler} 捕获并统一处理
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
package com.kindergarten.system.common.exception;

import com.kindergarten.system.common.result.ResultCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    /** 错误码 */
    private final Integer code;

    /**
     * 使用默认错误码创建异常
     *
     * @param message 错误信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.ERROR.getCode();
    }

    /**
     * 使用错误码枚举创建异常
     *
     * @param resultCode 错误码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 使用自定义错误码创建异常
     *
     * @param code    错误码
     * @param message 错误信息
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

}
