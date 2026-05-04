package com.example.anvisos.auth.service;

import com.example.anvisos.auth.dto.request.LoginRequest;
import com.example.anvisos.auth.dto.request.RegisterRequest;
import com.example.anvisos.auth.dto.request.RefreshRequest;
import com.example.anvisos.auth.dto.response.TokenResponse;
import com.example.anvisos.model.entity.HealthRecord;
import com.example.anvisos.model.entity.RefreshToken;
import com.example.anvisos.model.entity.User;
import com.example.anvisos.model.enums.UserRole;
import com.example.anvisos.model.repository.HealthRecordRepository;
import com.example.anvisos.model.repository.RefreshTokenRepository;
import com.example.anvisos.model.repository.UserRepository;
import com.example.anvisos.notification.EmailService;
import java.time.Instant;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final HealthRecordRepository healthRecordRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;

    public AuthService(
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository,
            HealthRecordRepository healthRecordRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.healthRecordRepository = healthRecordRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.emailService = emailService;
    }

    @org.springframework.transaction.annotation.Transactional
    public User register(RegisterRequest request) {
        if (userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new IllegalArgumentException("Số điện thoại đã được đăng ký");
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email đã được đăng ký");
        }
        User user = User.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(java.util.Optional.ofNullable(request.getRole()).orElse(UserRole.OWNER))
                .phoneVerified(false)
                .emailVerified(false)
                .createdAt(Instant.now())
                .build();
        User saved = userRepository.save(user);

        // Khởi tạo Hồ sơ y tế cơ bản cho SOS
        HealthRecord healthRecord = HealthRecord.builder()
                .user(saved)
                .bloodType(request.getBloodType())
                .birthYear(request.getBirthYear())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        healthRecordRepository.save(healthRecord);

        // Gửi OTP xác thực email (bất đồng bộ)
        if (saved.getEmail() != null && !saved.getEmail().isBlank()) {
            emailService.sendVerificationOtp(saved);
        }
        return saved;
    }

    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByPhone(request.getPhoneOrEmail())
                .or(() -> userRepository.findByEmail(request.getPhoneOrEmail()))
                .orElseThrow(() -> new IllegalArgumentException("Thông tin đăng nhập không hợp lệ"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Thông tin đăng nhập không hợp lệ");
        }

        // Kiểm tra email đã được xác thực chưa (chỉ bắt buộc nếu user có email)
        if (user.getEmail() != null && !user.getEmail().isBlank() && !user.isEmailVerified()) {
            throw new IllegalArgumentException(
                    "Email chưa được xác thực. Vui lòng kiểm tra hộp thư và nhập mã OTP.");
        }

        RefreshToken refreshToken = refreshTokenService.create(user);
        String accessToken = jwtService.createAccessToken(user);

        return new TokenResponse(
                accessToken,
                refreshToken.getToken(),
                jwtService.getAccessTtlSeconds(),
                refreshTokenService.getRefreshTtlSeconds()
        );
    }

    public TokenResponse refresh(RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenService.validate(request.getRefreshToken());
        User user = refreshToken.getUser();
        String accessToken = jwtService.createAccessToken(user);

        refreshTokenRepository.delete(refreshToken);
        RefreshToken newRefreshToken = refreshTokenService.create(user);

        return new TokenResponse(
                accessToken,
                newRefreshToken.getToken(),
                jwtService.getAccessTtlSeconds(),
                refreshTokenService.getRefreshTtlSeconds()
        );
    }

    public boolean isEmailAvailable(String email) {
        if (email == null || email.isBlank()) return true;
        return userRepository.findByEmail(email).isEmpty();
    }

    public boolean isPhoneAvailable(String phone) {
        if (phone == null || phone.isBlank()) return true;
        return userRepository.findByPhone(phone).isEmpty();
    }
}
