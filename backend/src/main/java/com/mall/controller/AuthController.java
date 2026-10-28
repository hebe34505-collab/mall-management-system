package com.mall.controller;

import com.mall.common.ApiResponse;
import com.mall.dto.LoginRequest;
import com.mall.dto.PasswordChangeRequest;
import com.mall.dto.ProfileUpdateRequest;
import com.mall.dto.RegisterRequest;
import com.mall.service.AuthService;
import com.mall.vo.LoginVO;
import com.mall.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证与个人资料")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "注册普通用户")
    @PostMapping("/auth/register")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiResponse.ok();
    }

    @Operation(summary = "用户名或邮箱登录")
    @PostMapping("/auth/login")
    public ApiResponse<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @Operation(summary = "退出登录并使当前令牌失效")
    @PostMapping("/auth/logout")
    public ApiResponse<Void> logout() {
        authService.logout();
        return ApiResponse.ok();
    }

    @Operation(summary = "当前登录用户")
    @GetMapping("/auth/me")
    public ApiResponse<UserVO> me() {
        return ApiResponse.ok(authService.me());
    }

    @Operation(summary = "修改个人资料")
    @PutMapping("/users/me")
    public ApiResponse<UserVO> updateProfile(@Valid @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.ok(authService.updateProfile(request));
    }

    @Operation(summary = "修改密码")
    @PutMapping("/users/me/password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        authService.changePassword(request);
        return ApiResponse.ok();
    }
}
