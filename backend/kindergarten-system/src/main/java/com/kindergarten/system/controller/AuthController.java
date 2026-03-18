package com.kindergarten.system.controller;

import com.kindergarten.system.common.result.Result;
import com.kindergarten.system.dto.CurrentUserInfo;
import com.kindergarten.system.dto.LoginRequest;
import com.kindergarten.system.dto.LoginResponse;
import com.kindergarten.system.security.JwtUtil;
import com.kindergarten.system.security.LoginUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();

        LoginResponse response = new LoginResponse();
        response.setToken(jwtUtil.generateToken(userDetails));
        response.setId(userDetails.getId());
        response.setUsername(userDetails.getUsername());
        response.setRealName(userDetails.getRealName());
        response.setRole(userDetails.getRole());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return Result.success(response);
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        SecurityContextHolder.clearContext();
        return Result.success();
    }

    @GetMapping("/info")
    public Result<CurrentUserInfo> info(@AuthenticationPrincipal LoginUserDetails userDetails) {
        if (userDetails == null) {
            return Result.error("未登录或Token无效");
        }
        return Result.success(CurrentUserInfo.from(userDetails));
    }

}