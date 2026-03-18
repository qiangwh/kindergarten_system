package com.kindergarten.system.dto;

import lombok.Data;

/**
 * 登录响应
 */
@Data
public class LoginResponse {

    private String token;

    private Long id;

    private String username;

    private String realName;

    private String role;

}