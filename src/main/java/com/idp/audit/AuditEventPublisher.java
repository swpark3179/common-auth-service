package com.idp.audit;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
@RequiredArgsConstructor
public class AuditEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publish(AuditEventType eventType, Long userId, String username, boolean success, String detail) {
        String ip = extractIp();
        String ua = extractUserAgent();
        eventPublisher.publishEvent(
                AuditEvent.builder()
                        .source(this)
                        .eventType(eventType)
                        .userId(userId)
                        .username(username)
                        .ipAddress(ip)
                        .userAgent(ua)
                        .detail(detail)
                        .success(success)
                        .build());
    }

    public void publish(AuditEventType eventType, Long userId, String username, String resource,
                        boolean success, String detail) {
        String ip = extractIp();
        String ua = extractUserAgent();
        eventPublisher.publishEvent(
                AuditEvent.builder()
                        .source(this)
                        .eventType(eventType)
                        .userId(userId)
                        .username(username)
                        .ipAddress(ip)
                        .userAgent(ua)
                        .resource(resource)
                        .detail(detail)
                        .success(success)
                        .build());
    }

    private String extractIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String xForwarded = request.getHeader("X-Forwarded-For");
                if (xForwarded != null && !xForwarded.isBlank()) {
                    return xForwarded.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception ignored) {}
        return null;
    }

    private String extractUserAgent() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return attrs.getRequest().getHeader("User-Agent");
            }
        } catch (Exception ignored) {}
        return null;
    }
}
