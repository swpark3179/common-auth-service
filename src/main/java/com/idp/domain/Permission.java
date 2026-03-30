package com.idp.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "PERMISSIONS",
    uniqueConstraints = @UniqueConstraint(columnNames = {"RESOURCE_PATTERN", "ACTION"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * URI 패턴 (Ant 패턴 지원, 예: /api/users/**, /api/orders/*)
     */
    @Column(name = "RESOURCE_PATTERN", nullable = false, length = 500)
    private String resourcePattern;

    /**
     * HTTP 메서드 (GET, POST, PUT, DELETE, PATCH, * = 전체)
     */
    @Column(nullable = false, length = 20)
    private String action;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
