package com.fundspark.fundspark_backend.service;

import com.fundspark.fundspark_backend.dto.ForgotPasswordRequest;
import com.fundspark.fundspark_backend.dto.ResetPasswordRequest;
import com.fundspark.fundspark_backend.dto.VerifyResetOtpRequest;
import com.fundspark.fundspark_backend.model.User;
import com.fundspark.fundspark_backend.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PasswordResetService {

    private final JdbcTemplate jdbc;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetEmailService emailService;
    private final SecureRandom random = new SecureRandom();

    @Value("${app.password-reset.otp-expiry-minutes:5}")
    private int otpExpiryMinutes;

    @Value("${app.password-reset.token-expiry-minutes:10}")
    private int tokenExpiryMinutes;

    @Value("${app.password-reset.max-attempts:5}")
    private int maxAttempts;

    @Value("${app.password-reset.resend-cooldown-seconds:60}")
    private int resendCooldownSeconds;

    public PasswordResetService(
            JdbcTemplate jdbc,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            PasswordResetEmailService emailService) {
        this.jdbc = jdbc;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public String requestOtp(ForgotPasswordRequest request) {
        String email = normalize(request.email());

        if (userRepository.findByEmailIgnoreCase(email).isEmpty()) {
            return "If an account exists for that email, reset instructions will be sent.";
        }

        Integer recent = jdbc.queryForObject(
                "SELECT COUNT(*) FROM password_reset_otps WHERE email = ? AND created_at > ?",
                Integer.class,
                email,
                LocalDateTime.now().minusSeconds(resendCooldownSeconds)
        );

        if (recent != null && recent > 0) {
            return "If an account exists for that email, reset instructions will be sent.";
        }

        String otp = String.format("%06d", random.nextInt(1_000_000));
        LocalDateTime now = LocalDateTime.now();

        emailService.sendOtp(email, otp);

        jdbc.update(
                "UPDATE password_reset_otps SET consumed = TRUE WHERE email = ? AND consumed = FALSE",
                email
        );

        jdbc.update(
                "INSERT INTO password_reset_otps (email, otp_hash, expires_at, attempts, created_at, consumed) VALUES (?, ?, ?, 0, ?, FALSE)",
                email,
                passwordEncoder.encode(otp),
                now.plusMinutes(otpExpiryMinutes),
                now
        );

        return "If an account exists for that email, reset instructions will be sent.";
    }

    public Map<String, String> verifyOtp(VerifyResetOtpRequest request) {
        String email = normalize(request.email());
        ResetOtp row = findLatestActiveOtp(email);
        LocalDateTime now = LocalDateTime.now();

        if (row == null || row.expiresAt().isBefore(now) || row.attempts() >= maxAttempts) {
            throw invalidOtp();
        }

        if (!passwordEncoder.matches(request.otp(), row.otpHash())) {
            int attempts = row.attempts() + 1;
            jdbc.update(
                    "UPDATE password_reset_otps SET attempts = ?, consumed = CASE WHEN ? >= ? THEN TRUE ELSE consumed END WHERE id = ?",
                    attempts,
                    attempts,
                    maxAttempts,
                    row.id()
            );
            throw invalidOtp();
        }

        String resetToken = UUID.randomUUID() + UUID.randomUUID().toString().replace("-", "");

        jdbc.update(
                "UPDATE password_reset_otps SET verified_at = ?, reset_token_hash = ?, reset_token_expires_at = ? WHERE id = ? AND consumed = FALSE",
                now,
                sha256(resetToken),
                now.plusMinutes(tokenExpiryMinutes),
                row.id()
        );

        return Map.of("resetToken", resetToken);
    }

    public Map<String, String> resetPassword(ResetPasswordRequest request) {
        String tokenHash = sha256(request.resetToken());
        LocalDateTime now = LocalDateTime.now();

        ResetOtp row;
        try {
            row = jdbc.queryForObject(
                    "SELECT id, email, otp_hash, expires_at, attempts, verified_at, reset_token_hash, reset_token_expires_at, consumed FROM password_reset_otps WHERE reset_token_hash = ? AND consumed = FALSE ORDER BY id DESC LIMIT 1",
                    (rs, n) -> new ResetOtp(
                            rs.getLong("id"),
                            rs.getString("email"),
                            rs.getString("otp_hash"),
                            rs.getTimestamp("expires_at").toLocalDateTime(),
                            rs.getInt("attempts"),
                            rs.getTimestamp("verified_at") == null ? null : rs.getTimestamp("verified_at").toLocalDateTime(),
                            rs.getString("reset_token_hash"),
                            rs.getTimestamp("reset_token_expires_at") == null ? null : rs.getTimestamp("reset_token_expires_at").toLocalDateTime(),
                            rs.getBoolean("consumed")
                    ),
                    tokenHash
            );
        } catch (EmptyResultDataAccessException ex) {
            throw invalidToken();
        }

        if (row.verifiedAt() == null || row.resetTokenExpiresAt() == null || row.resetTokenExpiresAt().isBefore(now) || row.consumed()) {
            throw invalidToken();
        }

        User user = userRepository.findByEmailIgnoreCase(row.email())
                .orElseThrow(this::invalidToken);

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        jdbc.update(
                "UPDATE password_reset_otps SET consumed = TRUE, reset_token_hash = NULL, reset_token_expires_at = NULL WHERE id = ?",
                row.id()
        );

        jdbc.update(
                "UPDATE password_reset_otps SET consumed = TRUE, reset_token_hash = NULL, reset_token_expires_at = NULL WHERE email = ? AND id <> ? AND consumed = FALSE",
                row.email(),
                row.id()
        );

        return Map.of("message", "Password reset successfully.");
    }

    private ResetOtp findLatestActiveOtp(String email) {
        try {
            return jdbc.queryForObject(
                    "SELECT id, email, otp_hash, expires_at, attempts, verified_at, reset_token_hash, reset_token_expires_at, consumed FROM password_reset_otps WHERE email = ? AND consumed = FALSE ORDER BY id DESC LIMIT 1",
                    (rs, n) -> new ResetOtp(
                            rs.getLong("id"),
                            rs.getString("email"),
                            rs.getString("otp_hash"),
                            rs.getTimestamp("expires_at").toLocalDateTime(),
                            rs.getInt("attempts"),
                            rs.getTimestamp("verified_at") == null ? null : rs.getTimestamp("verified_at").toLocalDateTime(),
                            rs.getString("reset_token_hash"),
                            rs.getTimestamp("reset_token_expires_at") == null ? null : rs.getTimestamp("reset_token_expires_at").toLocalDateTime(),
                            rs.getBoolean("consumed")
                    ),
                    email
            );
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable", ex);
        }
    }

    private ResponseStatusException invalidOtp() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired OTP");
    }

    private ResponseStatusException invalidToken() {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token");
    }

    private record ResetOtp(
            Long id,
            String email,
            String otpHash,
            LocalDateTime expiresAt,
            Integer attempts,
            LocalDateTime verifiedAt,
            String resetTokenHash,
            LocalDateTime resetTokenExpiresAt,
            Boolean consumed
    ) {
    }
}
