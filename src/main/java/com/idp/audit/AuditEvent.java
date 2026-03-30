package com.idp.audit;

import lombok.Builder;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class AuditEvent extends ApplicationEvent {

    private final AuditEventType eventType;
    private final Long userId;
    private final String username;
    private final String ipAddress;
    private final String userAgent;
    private final String resource;
    private final String detail;
    private final boolean success;

    @Builder
    public AuditEvent(Object source,
                       AuditEventType eventType,
                       Long userId,
                       String username,
                       String ipAddress,
                       String userAgent,
                       String resource,
                       String detail,
                       boolean success) {
        super(source);
        this.eventType = eventType;
        this.userId = userId;
        this.username = username;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.resource = resource;
        this.detail = detail;
        this.success = success;
    }
}
