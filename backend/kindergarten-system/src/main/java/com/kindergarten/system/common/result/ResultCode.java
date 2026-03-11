/**
 * 返回状态码枚举
 * <p>
 * 定义系统中使用的所有返回状态码：
 * - 0：成功
 * - 10000-19999：业务错误
 * - 20000-29999：认证授权错误
 * - 30000-39999：文件相关错误
 * - 90000+：系统错误
 * </p>
 *
 * @author Kindergarten System
 * @since 1.0.0
 */
package com.kindergarten.system.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {

    /** 成功 */
    SUCCESS(0, "ok"),

    /** 通用错误 */
    ERROR(10000, "操作失败"),

    /** 参数错误 */
    PARAM_ERROR(10001, "参数错误"),

    /** 数据不存在 */
    DATA_NOT_FOUND(10002, "数据不存在"),

    /** 状态不允许 */
    STATUS_NOT_ALLOWED(10003, "状态不允许"),

    /** 未登录或Token无效 */
    UNAUTHORIZED(20001, "未登录或Token无效"),

    /** 无权限访问 */
    FORBIDDEN(20002, "无权限访问"),

    /** 文件上传失败 */
    UPLOAD_ERROR(30001, "文件上传失败"),

    /** 系统异常 */
    SYSTEM_ERROR(90000, "系统异常");

    /** 状态码 */
    private final Integer code;

    /** 提示信息 */
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
