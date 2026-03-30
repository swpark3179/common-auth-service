package com.idp.dto.response;

import com.idp.domain.Permission;
import com.idp.domain.Role;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Builder
public class RoleResponse {

    private Long id;
    private String name;
    private String description;
    private Set<PermissionResponse> permissions;
    private LocalDateTime createdAt;

    public static RoleResponse from(Role role) {
        return RoleResponse.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .permissions(role.getPermissions().stream()
                        .map(PermissionResponse::from)
                        .collect(Collectors.toSet()))
                .createdAt(role.getCreatedAt())
                .build();
    }
}
