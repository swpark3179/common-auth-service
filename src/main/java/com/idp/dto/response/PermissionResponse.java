package com.idp.dto.response;

import com.idp.domain.Permission;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PermissionResponse {

    private Long id;
    private String resourcePattern;
    private String action;
    private String description;
    private LocalDateTime createdAt;

    public static PermissionResponse from(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .resourcePattern(permission.getResourcePattern())
                .action(permission.getAction())
                .description(permission.getDescription())
                .createdAt(permission.getCreatedAt())
                .build();
    }
}
