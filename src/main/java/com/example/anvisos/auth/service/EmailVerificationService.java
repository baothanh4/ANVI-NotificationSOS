package com.example.anvisos.auth.service;

import com.example.anvisos.model.entity.EmailOtpToken;
import com.example.anvisos.model.entity.User;
import com.example.anvisos.model.enums.OtpPurpose;
import com.example.anvisos.model.repository.EmailOtpTokenRepository;
import com.example.anvisos.model.repository.UserRepository;
import com.example.anvisos.notification.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class EmailVerificationService {

    private final UserRepository userRepository;
    private final EmailOtpTokenRepository otpRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public EmailVerificationService(UserRepository userRepository,
                                    EmailOtpTokenRepository otpRepository,
                                    EmailService emailService,
                                    PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    // ─────────────────────────────────────────────
    //  A. Xác thực Email
    // ─────────────────────────────────────────────

    /**
     * Xác thực OTP đăng ký email.
     * Set emailVerified = true nếu OTP hợp lệ.
     */
    @Transactional
    public void verifyEmail(String email, String otp) {
        User user = findUserByEmailOrThrow(email);

        if (user.isEmailVerified()) {
            throw new IllegalArgumentException("Email này đã được xác thực rồi");
        }

        EmailOtpToken token = otpRepository
                .findByUserAndOtpAndPurposeAndUsedFalseAndExpiresAtAfter(
                        user, otp, OtpPurpose.VERIFY_EMAIL, Instant.now())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mã OTP không hợp lệ hoặc đã hết hạn"));

        // Đánh dấu đã dùng
        token.setUsed(true);
        otpRepository.save(token);

        // Xác thực email
        user.setEmailVerified(true);
        userRepository.save(user);
    }

    /**
     * Gửi lại OTP xác thực email.
     */
    public void resendVerificationOtp(String email) {
        User user = findUserByEmailOrThrow(email);

        if (user.isEmailVerified()) {
            throw new IllegalArgumentException("Email này đã được xác thực rồi");
        }

        emailService.sendVerificationOtp(user);
    }

    // ─────────────────────────────────────────────
    //  C. Quên / Đặt lại mật khẩu
    // ─────────────────────────────────────────────

    /**
     * Gửi OTP đặt lại mật khẩu.
     * Không tiết lộ email có tồn tại hay không để tránh user enumeration attack.
     */
    public void forgotPassword(String email) {
        userRepository.findByEmail(email).ifPresent(emailService::sendPasswordResetOtp);
        // Luôn trả về thành công dù email có tồn tại hay không
    }

    /**
     * Đặt lại mật khẩu bằng OTP.
     */
    @Transactional
    public void resetPassword(String email, String otp, String newPassword) {
        User user = findUserByEmailOrThrow(email);

        EmailOtpToken token = otpRepository
                .findByUserAndOtpAndPurposeAndUsedFalseAndExpiresAtAfter(
                        user, otp, OtpPurpose.RESET_PASSWORD, Instant.now())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Mã OTP không hợp lệ hoặc đã hết hạn"));

        // Đánh dấu đã dùng
        token.setUsed(true);
        otpRepository.save(token);

        // Cập nhật mật khẩu mới
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ─────────────────────────────────────────────
    //  Helper
    // ─────────────────────────────────────────────

    private User findUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy tài khoản với email: " + email));
    }
}
