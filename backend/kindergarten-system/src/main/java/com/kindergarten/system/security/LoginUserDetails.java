package com.kindergarten.system.security;

import com.kindergarten.system.entity.SysUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 登录用户信息
 */
@Getter
public class LoginUserDetails implements UserDetails {

    private final Long id;

    private final String username;

    private final String password;

    private final String realName;

    private final String role;

    private final Integer status;

    private final List<GrantedAuthority> authorities;

    public LoginUserDetails(SysUser user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.realName = user.getRealName();
        this.role = user.getRole();
        this.status = user.getStatus();
        String finalRole = (role == null || role.isBlank()) ? "ADMIN" : role.trim().toUpperCase();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + finalRole));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == null || status == 1;
    }

}