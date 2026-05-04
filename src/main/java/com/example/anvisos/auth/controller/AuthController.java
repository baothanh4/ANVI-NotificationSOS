package com.example.anvisos.auth.controller;

import com.example.anvisos.auth.dto.request.*;
import com.example.anvisos.auth.dto.response.RegisterResponse;
import com.example.anvisos.auth.dto.response.TokenResponse;
import com.example.anvisos.auth.service.AuthService;
import com.example.anvisos.auth.service.EmailVerificationService;
import com.example.anvisos.model.entity.User;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;

    public AuthController(AuthService authService,
                          EmailVerificationService emailVerificationService) {
        this.authService = authService;
        this.emailVerificationService = emailVerificationService;
    }

    // ─────────────────────────────────────────────
    //  Đăng ký / Đăng nhập / Refresh
    // ─────────────────────────────────────────────

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return new RegisterResponse(
                user.getId(),
                user.getFullName(),
                user.getPhone(),
                user.getEmail(),
                user.getRole()
        );
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request);
    }

    // ─────────────────────────────────────────────
    //  A. Xác thực Email
    // ─────────────────────────────────────────────

    /**
     * POST /api/auth/verify-email
     * Body: { "email": "...", "otp": "123456" }
     */
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, String>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {
        emailVerificationService.verifyEmail(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(Map.of("message", "Email đã được xác thực thành công! Bạn có thể đăng nhập ngay."));
    }

    /**
     * POST /api/auth/resend-verification
     * Body: { "email": "..." }
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, String>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest request) {
        emailVerificationService.resendVerificationOtp(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "Mã OTP mới đã được gửi đến email của bạn."));
    }

    // ─────────────────────────────────────────────
    //  C. Quên / Đặt lại mật khẩu
    // ─────────────────────────────────────────────

    /**
     * POST /api/auth/forgot-password
     * Body: { "email": "..." }
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        emailVerificationService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "Nếu email tồn tại trong hệ thống, mã OTP đã được gửi."));
    }

    /**
     * POST /api/auth/reset-password
     * Body: { "email": "...", "otp": "123456", "newPassword": "..." }
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        emailVerificationService.resetPassword(
                request.getEmail(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Mật khẩu đã được đặt lại thành công! Vui lòng đăng nhập lại."));
    }

    @GetMapping("/check-availability")
    public ResponseEntity<Map<String, Boolean>> checkAvailability(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone) {
        
        if (email != null) {
            return ResponseEntity.ok(Map.of("available", authService.isEmailAvailable(email)));
        }
        if (phone != null) {
            return ResponseEntity.ok(Map.of("available", authService.isPhoneAvailable(phone)));
        }
        return ResponseEntity.badRequest().build();
    }
}
