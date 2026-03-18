package com.kindergarten.system.dto;

import com.kindergarten.system.security.LoginUserDetails;
import lombok.Data;

/**
 * 当前登录用户信息
 */
@Data
public class CurrentUserInfo {

    private Long id;

    private String username;

    private String realName;

    private String role;

    public static CurrentUserInfo from(LoginUserDetails userDetails) {
        CurrentUserInfo info = new CurrentUserInfo();
        info.setId(userDetails.getId());
        info.setUsername(userDetails.getUsername());
        info.setRealName(userDetails.getRealName());
        info.setRole(userDetails.getRole());
        return info;
    }

}