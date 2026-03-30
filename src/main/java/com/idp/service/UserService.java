package com.idp.service;

import com.idp.audit.AuditEventPublisher;
import com.idp.audit.AuditEventType;
import com.idp.domain.Permission;
import com.idp.domain.Role;
import com.idp.domain.User;
import com.idp.dto.request.UserCreateRequest;
import com.idp.dto.response.UserResponse;
import com.idp.exception.BusinessException;
import com.idp.exception.ErrorCode;
import com.idp.repository.RoleRepository;
import com.idp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditEventPublisher auditEventPublisher;

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return UserResponse.from(getUser(id));
    }

    @Transactional
    public UserResponse create(UserCreateRequest request, Long currentUserId) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.DUPLICATE_USERNAME);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        Set<Role> roles = new HashSet<>();
        if (request.getRoleIds() != null) {
            for (Long roleId : request.getRoleIds()) {
                roles.add(roleRepository.findById(roleId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND)));
            }
        }

        User user = User.builder()
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .roles(roles)
                .build();

        User saved = userRepository.save(user);
        auditEventPublisher.publish(AuditEventType.USER_CREATED, currentUserId, null, true,
                "Created user: " + saved.getUsername());
        return UserResponse.from(saved);
    }

    @Transactional
    public void delete(Long id, Long currentUserId) {
        User user = getUser(id);
        userRepository.delete(user);
        auditEventPublisher.publish(AuditEventType.USER_DELETED, currentUserId, null, true,
                "Deleted user: " + user.getUsername());
    }

    @Transactional
    public UserResponse setEnabled(Long id, boolean enabled, Long currentUserId) {
        User user = getUser(id);
        user.setEnabled(enabled);
        auditEventPublisher.publish(
                enabled ? AuditEventType.USER_ENABLED : AuditEventType.USER_DISABLED,
                currentUserId, null, true, "User: " + user.getUsername());
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse setLocked(Long id, boolean locked, Long currentUserId) {
        User user = getUser(id);
        user.setLocked(locked);
        auditEventPublisher.publish(
                locked ? AuditEventType.USER_LOCKED : AuditEventType.USER_UNLOCKED,
                currentUserId, null, true, "User: " + user.getUsername());
        return UserResponse.from(user);
    }

    private User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
