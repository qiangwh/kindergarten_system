package com.kindergarten.system.common.result;

import lombok.Getter;

@Getter
public enum ResultCode {

    SUCCESS(0, "ok"),
    ERROR(10000, "操作失败"),
    
    PARAM_ERROR(10001, "参数错误"),
    DATA_NOT_FOUND(10002, "数据不存在"),
    STATUS_NOT_ALLOWED(10003, "状态不允许"),
    
    UNAUTHORIZED(20001, "未登录或Token无效"),
    FORBIDDEN(20002, "无权限访问"),
    
    UPLOAD_ERROR(30001, "文件上传失败"),
    
    SYSTEM_ERROR(90000, "系统异常");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

}
