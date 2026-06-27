package com.teasmart.controller;

import com.teasmart.common.Result;
import com.teasmart.dto.ChangePasswordRequest;
import com.teasmart.dto.LoginRequest;
import com.teasmart.dto.RegisterRequest;
import com.teasmart.dto.UpdateProfileRequest;
import com.teasmart.service.AuthService;
import com.teasmart.vo.LoginVO;
import com.teasmart.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Result<UserVO> register(@Valid @RequestBody RegisterRequest req) {
        return Result.ok(authService.register(req));
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginRequest req) {
        return Result.ok(authService.login(req));
    }

    @GetMapping("/me")
    public Result<UserVO> me(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(authService.getUserById(userId));
    }

    @PutMapping("/profile")
    public Result<UserVO> updateProfile(Authentication authentication,
                                        @Valid @RequestBody UpdateProfileRequest req) {
        Long userId = (Long) authentication.getPrincipal();
        return Result.ok(authService.updateProfile(userId, req));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(Authentication authentication,
                                         @Valid @RequestBody ChangePasswordRequest req) {
        Long userId = (Long) authentication.getPrincipal();
        authService.changePassword(userId, req);
        return Result.ok(null);
    }
}
