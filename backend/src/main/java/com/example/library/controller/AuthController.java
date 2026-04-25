package com.example.library.controller;

import com.example.library.common.ApiResponse;
import com.example.library.dto.LoginRequest;
import com.example.library.dto.PasswordChangeRequest;
import com.example.library.dto.ProfileUpdateRequest;
import com.example.library.security.CurrentUser;
import com.example.library.service.AuthService;
import com.example.library.vo.LoginResponse;
import com.example.library.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ApiResponse<UserVO> me(@AuthenticationPrincipal CurrentUser user) {
        return ApiResponse.ok(authService.me(user));
    }

    @PutMapping("/profile")
    public ApiResponse<UserVO> updateProfile(@AuthenticationPrincipal CurrentUser user, @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.ok(authService.updateProfile(user, request));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal CurrentUser user, @Valid @RequestBody PasswordChangeRequest request) {
        authService.changePassword(user, request);
        return ApiResponse.ok();
    }
}

