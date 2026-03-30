package com.idp.service;

import com.idp.audit.AuditEventPublisher;
import com.idp.audit.AuditEventType;
import com.idp.domain.Permission;
import com.idp.dto.request.PermissionRequest;
import com.idp.dto.response.PermissionResponse;
import com.idp.exception.BusinessException;
import com.idp.exception.ErrorCode;
import com.idp.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final AuditEventPublisher auditEventPublisher;

    @Transactional(readOnly = true)
    public Page<PermissionResponse> findAll(Pageable pageable) {
        return permissionRepository.findAll(pageable).map(PermissionResponse::from);
    }

    @Transactional(readOnly = true)
    public PermissionResponse findById(Long id) {
        return PermissionResponse.from(getPermission(id));
    }

    @Transactional
    public PermissionResponse create(PermissionRequest request, Long currentUserId) {
        if (permissionRepository.existsByResourcePatternAndAction(request.getResourcePattern(), request.getAction())) {
            throw new BusinessException(ErrorCode.DUPLICATE_PERMISSION);
        }
        Permission permission = Permission.builder()
                .resourcePattern(request.getResourcePattern())
                .action(request.getAction().toUpperCase())
                .description(request.getDescription())
                .build();
        Permission saved = permissionRepository.save(permission);
        auditEventPublisher.publish(AuditEventType.PERMISSION_CREATED, currentUserId, null, true,
                saved.getResourcePattern() + " " + saved.getAction());
        return PermissionResponse.from(saved);
    }

    @Transactional
    public void delete(Long id, Long currentUserId) {
        Permission permission = getPermission(id);
        permissionRepository.delete(permission);
        auditEventPublisher.publish(AuditEventType.PERMISSION_DELETED, currentUserId, null, true,
                "Deleted permission: " + id);
    }

    private Permission getPermission(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PERMISSION_NOT_FOUND));
    }
}
