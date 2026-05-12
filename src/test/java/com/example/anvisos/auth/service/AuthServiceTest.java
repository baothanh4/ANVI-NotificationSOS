package com.example.anvisos.auth.service;

import com.example.anvisos.auth.dto.request.LoginRequest;
import com.example.anvisos.auth.dto.request.RegisterRequest;
import com.example.anvisos.auth.dto.request.RefreshRequest;
import com.example.anvisos.auth.dto.response.TokenResponse;
import com.example.anvisos.model.entity.RefreshToken;
import com.example.anvisos.model.entity.User;
import com.example.anvisos.model.enums.UserRole;
import com.example.anvisos.model.repository.HealthRecordRepository;
import com.example.anvisos.model.repository.RefreshTokenRepository;
import com.example.anvisos.model.repository.UserRepository;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Mock
    private HealthRecordRepository healthRecordRepository;

    @Test
    void registerCreatesUser() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Anvi User");
        request.setPhone("0901");
        request.setEmail("user@anvi.vn");
        request.setPassword("secret");

        Mockito.when(userRepository.findByPhone("0901")).thenReturn(Optional.empty());
        Mockito.when(userRepository.findByEmail("user@anvi.vn")).thenReturn(Optional.empty());
        Mockito.when(passwordEncoder.encode("secret")).thenReturn("hashed");
        Mockito.when(userRepository.save(ArgumentMatchers.any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Mockito.when(healthRecordRepository.save(Mockito.any())) .thenAnswer(invocation -> invocation.getArgument(0));

        User user = authService.register(request);

        Assertions.assertEquals(UserRole.OWNER, user.getRole());
        Assertions.assertEquals("hashed", user.getPasswordHash());
    }

    @Test
    void loginReturnsTokens() {
        User user = User.builder()
                .id(1L)
                .fullName("Anvi")
                .phone("0901")
                .passwordHash("hashed")
                .role(UserRole.OWNER)
                .phoneVerified(true)
                .createdAt(Instant.now())
                .build();

        LoginRequest request = new LoginRequest();
        request.setPhoneOrEmail("0901");
        request.setPassword("secret");

        RefreshToken refreshToken = RefreshToken.builder()
                .token("refresh")
                .expiresAt(Instant.now().plusSeconds(1000))
                .createdAt(Instant.now())
                .user(user)
                .build();

        Mockito.when(userRepository.findByPhone("0901")).thenReturn(Optional.of(user));
        Mockito.when(passwordEncoder.matches("secret", "hashed")).thenReturn(true);
        Mockito.when(refreshTokenService.create(user)).thenReturn(refreshToken);
        Mockito.when(jwtService.createAccessToken(user)).thenReturn("access");
        Mockito.when(jwtService.getAccessTtlSeconds()).thenReturn(900L);
        Mockito.when(refreshTokenService.getRefreshTtlSeconds()).thenReturn(1000L);

        TokenResponse response = authService.login(request);

        Assertions.assertEquals("access", response.getAccessToken());
        Assertions.assertEquals("refresh", response.getRefreshToken());
    }

    @Test
    void refreshCreatesNewRefreshToken() {
        User user = User.builder().id(2L).build();
        RefreshToken oldToken = RefreshToken.builder()
                .token("old")
                .user(user)
                .expiresAt(Instant.now().plusSeconds(1000))
                .createdAt(Instant.now())
                .build();
        RefreshToken newToken = RefreshToken.builder()
                .token("new")
                .user(user)
                .expiresAt(Instant.now().plusSeconds(1000))
                .createdAt(Instant.now())
                .build();

        RefreshRequest request = new RefreshRequest();
        request.setRefreshToken("old");

        Mockito.when(refreshTokenService.validate("old")).thenReturn(oldToken);
        Mockito.when(jwtService.createAccessToken(user)).thenReturn("access");
        Mockito.when(refreshTokenService.create(user)).thenReturn(newToken);
        Mockito.when(jwtService.getAccessTtlSeconds()).thenReturn(900L);
        Mockito.when(refreshTokenService.getRefreshTtlSeconds()).thenReturn(1000L);

        TokenResponse response = authService.refresh(request);

        Assertions.assertEquals("access", response.getAccessToken());
        Assertions.assertEquals("new", response.getRefreshToken());
    }
}
