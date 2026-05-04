package com.example.anvisos.model.repository;

import com.example.anvisos.model.entity.EmailOtpToken;
import com.example.anvisos.model.entity.User;
import com.example.anvisos.model.enums.OtpPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

public interface EmailOtpTokenRepository extends JpaRepository<EmailOtpToken, Long> {

    /** Tìm OTP hợp lệ (chưa dùng, chưa hết hạn) theo user + purpose + mã OTP */
    Optional<EmailOtpToken> findByUserAndOtpAndPurposeAndUsedFalseAndExpiresAtAfter(
            User user, String otp, OtpPurpose purpose, Instant now);

    /** Tìm OTP hợp lệ mới nhất theo user + purpose (để gửi lại) */
    Optional<EmailOtpToken> findTopByUserAndPurposeAndUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
            User user, OtpPurpose purpose, Instant now);

    /** Xóa tất cả OTP cũ của user theo purpose (dọn dẹp khi gửi mới) */
    @Modifying
    @Transactional
    @Query("DELETE FROM EmailOtpToken t WHERE t.user = :user AND t.purpose = :purpose")
    void deleteByUserAndPurpose(User user, OtpPurpose purpose);
}
