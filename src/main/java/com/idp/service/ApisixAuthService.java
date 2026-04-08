package com.idp.service;

import com.idp.audit.AuditEventPublisher;
import com.idp.audit.AuditEventType;
import com.idp.domain.Permission;
import com.idp.domain.Role;
import com.idp.domain.User;
import com.idp.exception.BusinessException;
import com.idp.repository.UserRepository;
import com.idp.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.AntPathMatcher;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApisixAuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final AuditEventPublisher auditEventPublisher;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     * APISIX ext-plugin-pre-req 요청 처리
     *
     * @param bearerToken  Authorization 헤더값 (Bearer prefix 포함)
     * @param requestUri   요청 URI (X-Forwarded-Uri 헤더)
     * @param requestMethod HTTP 메서드 (X-Forwarded-Method 헤더)
     * @return AuthResult (사용자 정보 및 응답 헤더 포함)
     */
    @Transactional(readOnly = true)
    public AuthResult verify(String bearerToken, String requestUri, String requestMethod) {
        // 1. Bearer 토큰 추출
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            return AuthResult.unauthorized("Missing or invalid Authorization header");
        }
        String token = bearerToken.substring(7);

        // 2. JWT 파싱 및 검증 (만료, 서명)
        Claims claims;
        try {
            claims = jwtTokenProvider.parseToken(token);
        } catch (BusinessException e) {
            return AuthResult.unauthorized(e.getMessage());
        }

        // 3. Access Token 타입 검증
        if (!"access".equals(claims.get("type"))) {
            return AuthResult.unauthorized("Not an access token");
        }

        // 4. 사용자 조회 (활성화/잠금 확인)
        String username = claims.getSubject();
        User user = userRepository.findByUsernameWithRolesAndPermissions(username).orElse(null);
        if (user == null || !user.isEnabled() || user.isLocked()) {
            logDenied(claims, requestUri, "User unavailable");
            return AuthResult.unauthorized("User unavailable");
        }

        // 5. RBAC 권한 검증 (URI 패턴 + HTTP 메서드)
        if (requestUri != null && requestMethod != null) {
            boolean allowed = checkPermission(user, requestUri, requestMethod);
            if (!allowed) {
                logDenied(claims, requestUri, "Permission denied: " + requestMethod + " " + requestUri);
                return AuthResult.forbidden("Permission denied");
            }
        }

        // 6. 허용 — 응답 헤더에 사용자 정보 전달 (upstream으로 포워딩)
        List<String> roles = user.getRoles().stream().map(Role::getName).collect(Collectors.toList());
        auditEventPublisher.publish(AuditEventType.APISIX_AUTH_ALLOWED,
                user.getId(), username, requestUri, true, requestMethod + " " + requestUri);

        return AuthResult.allowed(user.getId(), username, roles);
    }

    private boolean checkPermission(User user, String requestUri, String requestMethod) {
        for (Role role : user.getRoles()) {
            for (Permission permission : role.getPermissions()) {
                boolean pathMatch = antPathMatcher.match(permission.getResourcePattern(), requestUri);
                boolean methodMatch = "*".equals(permission.getAction())
                        || permission.getAction().equalsIgnoreCase(requestMethod);
                if (pathMatch && methodMatch) {
                    return true;
                }
            }
        }
        return false;
    }

    private void logDenied(Claims claims, String requestUri, String detail) {
        Long userId = claims.get("userId", Long.class);
        String username = claims.getSubject();
        auditEventPublisher.publish(AuditEventType.APISIX_AUTH_DENIED,
                userId, username, requestUri, false, detail);
    }

    public record AuthResult(
            boolean allowed,
            boolean forbidden,
            Long userId,
            String username,
            List<String> roles,
            String reason
    ) {
        static AuthResult allowed(Long userId, String username, List<String> roles) {
            return new AuthResult(true, false, userId, username, roles, null);
        }

        static AuthResult unauthorized(String reason) {
            return new AuthResult(false, false, null, null, null, reason);
        }

        static AuthResult forbidden(String reason) {
            return new AuthResult(false, true, null, null, null, reason);
        }

        public Map<String, String> toHeaders() {
            return Map.of(
                    "X-User-Id", String.valueOf(userId),
                    "X-User-Name", username,
                    "X-User-Roles", String.join(",", roles)
            );
        }
    }
}
