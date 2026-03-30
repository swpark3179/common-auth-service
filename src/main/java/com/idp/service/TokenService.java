package com.idp.service;

import com.idp.domain.RefreshToken;
import com.idp.domain.TokenBlacklist;
import com.idp.domain.User;
import com.idp.exception.BusinessException;
import com.idp.exception.ErrorCode;
import com.idp.repository.RefreshTokenRepository;
import com.idp.repository.TokenBlacklistRepository;
import com.idp.security.JwtTokenProvider;
import com.idp.security.JwtTokenProvider.RefreshTokenResult;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;

    /**
     * Access Token + Refresh Token 쌍 발급
     */
    @Transactional
    public TokenPair issueTokenPair(User user) {
        List<String> roles = user.getRoles().stream()
                .map(r -> r.getName())
                .collect(Collectors.toList());

        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getUsername(), roles);
        RefreshTokenResult rtResult = jwtTokenProvider.generateRefreshToken(user.getId(), user.getUsername());

        RefreshToken refreshTokenEntity = RefreshToken.builder()
                .jti(rtResult.jti())
                .user(user)
                .tokenHash(hash(rtResult.token()))
                .issuedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.ofInstant(rtResult.expiresAt(), ZoneId.systemDefault()))
                .build();
        refreshTokenRepository.save(refreshTokenEntity);

        return new TokenPair(accessToken, rtResult.token(), jwtTokenProvider.getAccessTokenExpirySeconds());
    }

    /**
     * Refresh Token으로 새 토큰 쌍 발급 (Rotation)
     */
    @Transactional
    public TokenPair rotate(String rawRefreshToken, User user) {
        Claims claims = jwtTokenProvider.parseToken(rawRefreshToken);

        if (!"refresh".equals(claims.get("type"))) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String jti = claims.getId();
        RefreshToken stored = refreshTokenRepository.findByJti(jti)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN));

        if (!stored.isValid()) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (!hash(rawRefreshToken).equals(stored.getTokenHash())) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 기존 Refresh Token 폐기
        stored.revoke();

        // 새 토큰 쌍 발급
        return issueTokenPair(user);
    }

    /**
     * Access Token을 Blacklist에 등록 (로그아웃)
     */
    @Transactional
    public void blacklistAccessToken(String rawAccessToken) {
        Claims claims = jwtTokenProvider.parseTokenIgnoreExpiry(rawAccessToken);
        String jti = claims.getId();

        if (!tokenBlacklistRepository.existsByJti(jti)) {
            LocalDateTime expiresAt = LocalDateTime.ofInstant(
                    claims.getExpiration().toInstant(), ZoneId.systemDefault());
            Long userId = claims.get("userId", Long.class);

            tokenBlacklistRepository.save(TokenBlacklist.builder()
                    .jti(jti)
                    .userId(userId)
                    .expiresAt(expiresAt)
                    .build());
        }
    }

    /**
     * 해당 사용자의 모든 Refresh Token 폐기 (강제 로그아웃)
     */
    @Transactional
    public void revokeAllRefreshTokens(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId, LocalDateTime.now());
    }

    /**
     * 만료된 토큰 정기 정리 (매일 새벽 3시)
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        int blacklistDeleted = tokenBlacklistRepository.deleteExpired(now);
        int refreshDeleted = refreshTokenRepository.deleteExpired(now);
        log.info("Token cleanup: blacklist={}, refresh={}", blacklistDeleted, refreshDeleted);
    }

    private String hash(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public record TokenPair(String accessToken, String refreshToken, long expiresIn) {}
}
