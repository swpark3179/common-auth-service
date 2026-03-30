package com.idp.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "AUDIT_LOGS")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 이벤트 유형 (LOGIN_SUCCESS, LOGIN_FAILURE, LOGOUT, TOKEN_REFRESH, ...)
     */
    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(name = "USER_ID")
    private Long userId;

    @Column(length = 100)
    private String username;

    @Column(length = 50)
    private String ipAddress;

    @Column(length = 500)
    private String userAgent;

    /**
     * 요청 리소스 URI
     */
    @Column(length = 500)
    private String resource;

    /**
     * 이벤트 상세 정보 (JSON 또는 자유 텍스트)
     */
    @Column(length = 2000)
    private String detail;

    @Column(nullable = false)
    @Builder.Default
    private boolean success = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
