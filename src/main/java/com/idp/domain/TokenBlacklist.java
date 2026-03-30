package com.idp.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "TOKEN_BLACKLIST")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenBlacklist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 폐기된 Access Token의 JTI (UUID)
     */
    @Column(nullable = false, unique = true, length = 36)
    private String jti;

    /**
     * 토큰 소유자 (참조 목적)
     */
    @Column(name = "USER_ID")
    private Long userId;

    /**
     * 원래 토큰 만료 시각 — 이 시각 이후에는 정리 가능
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
