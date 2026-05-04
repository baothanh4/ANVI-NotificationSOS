package com.example.anvisos.notification;

import com.example.anvisos.model.entity.EmailOtpToken;
import com.example.anvisos.model.entity.User;
import com.example.anvisos.model.enums.OtpPurpose;
import com.example.anvisos.model.repository.EmailOtpTokenRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter VN_FORMATTER = DateTimeFormatter
            .ofPattern("HH:mm:ss dd/MM/yyyy")
            .withZone(ZoneId.of("Asia/Ho_Chi_Minh"));

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final EmailOtpTokenRepository otpRepository;

    @Value("${anvi.email.from}")
    private String fromAddress;

    @Value("${anvi.email.otp-ttl-minutes:10}")
    private int otpTtlMinutes;

    public EmailService(JavaMailSender mailSender,
                        TemplateEngine templateEngine,
                        EmailOtpTokenRepository otpRepository) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.otpRepository = otpRepository;
    }

    // ─────────────────────────────────────────────
    //  OTP Generation & Storage
    // ─────────────────────────────────────────────

    /**
     * Tạo và lưu OTP 6 số vào DB, xóa OTP cũ trước.
     */
    public EmailOtpToken createOtp(User user, OtpPurpose purpose) {
        // Xóa OTP cũ của user theo purpose này
        otpRepository.deleteByUserAndPurpose(user, purpose);

        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        EmailOtpToken token = EmailOtpToken.builder()
                .user(user)
                .otp(otp)
                .purpose(purpose)
                .expiresAt(Instant.now().plusSeconds(otpTtlMinutes * 60L))
                .build();
        return otpRepository.save(token);
    }

    // ─────────────────────────────────────────────
    //  Email: Verify Email OTP
    // ─────────────────────────────────────────────

    /**
     * Gửi OTP xác thực email sau khi đăng ký.
     * Chạy bất đồng bộ (@Async) để không block request.
     */
    @Async
    public void sendVerificationOtp(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("[EMAIL] User {} has no email, skip verification", user.getId());
            return;
        }
        try {
            EmailOtpToken token = createOtp(user, OtpPurpose.VERIFY_EMAIL);

            Context ctx = new Context();
            ctx.setVariable("fullName", user.getFullName());
            ctx.setVariable("otp", token.getOtp());
            ctx.setVariable("ttlMinutes", otpTtlMinutes);

            String html = templateEngine.process("email/verify-email", ctx);
            sendHtmlEmail(user.getEmail(), "✅ Xác thực email ANVI-SOS — Mã OTP của bạn", html);
            log.info("[EMAIL] Verification OTP sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("[EMAIL] Failed to send verification OTP to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    //  Email: Reset Password OTP
    // ─────────────────────────────────────────────

    /**
     * Gửi OTP đặt lại mật khẩu.
     */
    @Async
    public void sendPasswordResetOtp(User user) {
        try {
            EmailOtpToken token = createOtp(user, OtpPurpose.RESET_PASSWORD);

            Context ctx = new Context();
            ctx.setVariable("fullName", user.getFullName());
            ctx.setVariable("otp", token.getOtp());
            ctx.setVariable("ttlMinutes", otpTtlMinutes);

            String html = templateEngine.process("email/reset-password", ctx);
            sendHtmlEmail(user.getEmail(), "🔑 Đặt lại mật khẩu ANVI-SOS — Mã OTP của bạn", html);
            log.info("[EMAIL] Password reset OTP sent to {}", user.getEmail());
        } catch (Exception e) {
            log.error("[EMAIL] Failed to send password reset OTP to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    //  Email: SOS Alert
    // ─────────────────────────────────────────────

    /**
     * Gửi cảnh báo SOS khẩn cấp đến địa chỉ email.
     *
     * @param toEmail     Email người nhận (emergency contact)
     * @param victimName  Tên nạn nhân
     * @param victimPhone SĐT nạn nhân
     * @param latitude    Vĩ độ
     * @param longitude   Kinh độ
     * @param bloodType   Nhóm máu (có thể null)
     * @param allergies   Dị ứng (có thể null)
     * @param chronicDiseases  Bệnh mãn tính (có thể null)
     * @param currentMedication Thuốc đang dùng (có thể null)
     */
    @Async
    public void sendSosAlert(String toEmail,
                             String victimName,
                             String victimPhone,
                             double latitude,
                             double longitude,
                             String bloodType,
                             String allergies,
                             String chronicDiseases,
                             String currentMedication) {
        try {
            String mapUrl = String.format(
                    "https://www.google.com/maps?q=%s,%s", latitude, longitude);
            String triggeredAt = VN_FORMATTER.format(Instant.now());

            boolean hasHealthInfo = bloodType != null || allergies != null
                    || chronicDiseases != null || currentMedication != null;

            Context ctx = new Context();
            ctx.setVariable("victimName", victimName);
            ctx.setVariable("victimPhone", victimPhone);
            ctx.setVariable("latitude", latitude);
            ctx.setVariable("longitude", longitude);
            ctx.setVariable("mapUrl", mapUrl);
            ctx.setVariable("triggeredAt", triggeredAt);
            ctx.setVariable("hasHealthInfo", hasHealthInfo);
            ctx.setVariable("bloodType", bloodType);
            ctx.setVariable("allergies", allergies);
            ctx.setVariable("chronicDiseases", chronicDiseases);
            ctx.setVariable("currentMedication", currentMedication);

            String html = templateEngine.process("email/sos-alert", ctx);
            sendHtmlEmail(toEmail, "🚨 SOS KHẨN CẤP — " + victimName + " cần giúp đỡ!", html);
            log.info("[EMAIL-SOS] Alert sent to {} for victim {}", toEmail, victimName);
        } catch (Exception e) {
            log.error("[EMAIL-SOS] Failed to send SOS alert to {}: {}", toEmail, e.getMessage());
        }
    }

    // ─────────────────────────────────────────────
    //  Core: Send HTML Email
    // ─────────────────────────────────────────────

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromAddress);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true); // true = HTML
        mailSender.send(message);
    }
}
