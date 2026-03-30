package com.idp.service;

import com.idp.audit.AuditEventPublisher;
import com.idp.audit.AuditEventType;
import com.idp.domain.Permission;
import com.idp.domain.Role;
import com.idp.dto.request.RoleRequest;
import com.idp.dto.response.RoleResponse;
import com.idp.exception.BusinessException;
import com.idp.exception.ErrorCode;
import com.idp.repository.PermissionRepository;
import com.idp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final AuditEventPublisher auditEventPublisher;

    @Transactional(readOnly = true)
    public Page<RoleResponse> findAll(Pageable pageable) {
        return roleRepository.findAll(pageable).map(RoleResponse::from);
    }

    @Transactional(readOnly = true)
    public RoleResponse findById(Long id) {
        return RoleResponse.from(getRole(id));
    }

    @Transactional
    public RoleResponse create(RoleRequest request, Long currentUserId) {
        if (roleRepository.existsByName(request.getName())) {
            throw new BusinessException(ErrorCode.DUPLICATE_ROLE);
        }
        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        Role saved = roleRepository.save(role);
        auditEventPublisher.publish(AuditEventType.ROLE_CREATED, currentUserId, null, true,
                "Created role: " + saved.getName());
        return RoleResponse.from(saved);
    }

    @Transactional
    public RoleResponse update(Long id, RoleRequest request, Long currentUserId) {
        Role role = getRole(id);
        role.setName(request.getName());
        role.setDescription(request.getDescription());
        auditEventPublisher.publish(AuditEventType.ROLE_UPDATED, currentUserId, null, true,
                "Updated role: " + role.getName());
        return RoleResponse.from(role);
    }

    @Transactional
    public void delete(Long id, Long currentUserId) {
        Role role = getRole(id);
        roleRepository.delete(role);
        auditEventPublisher.publish(AuditEventType.ROLE_DELETED, currentUserId, null, true,
                "Deleted role: " + role.getName());
    }

    @Transactional
    public RoleResponse assignPermission(Long roleId, Long permissionId, Long currentUserId) {
        Role role = getRole(roleId);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PERMISSION_NOT_FOUND));
        role.getPermissions().add(permission);
        auditEventPublisher.publish(AuditEventType.ROLE_ASSIGNED, currentUserId, null, true,
                "Assigned permission " + permissionId + " to role " + roleId);
        return RoleResponse.from(role);
    }

    @Transactional
    public RoleResponse revokePermission(Long roleId, Long permissionId, Long currentUserId) {
        Role role = getRole(roleId);
        role.getPermissions().removeIf(p -> p.getId().equals(permissionId));
        auditEventPublisher.publish(AuditEventType.ROLE_REVOKED, currentUserId, null, true,
                "Revoked permission " + permissionId + " from role " + roleId);
        return RoleResponse.from(role);
    }

    private Role getRole(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));
    }
}
