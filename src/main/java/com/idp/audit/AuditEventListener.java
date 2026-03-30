package com.idp.audit;

import com.idp.domain.AuditLog;
import com.idp.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditEventListener {

    private final AuditLogRepository auditLogRepository;

    /**
     * 트랜잭션 커밋 후 비동기 처리 (audit-executor 스레드풀)
     * - @TransactionalEventListener: 원 트랜잭션 커밋 시에만 실행 → 롤백 시 감사 로그 미기록
     * - @Async: 전용 스레드풀에서 비동기 실행 → 원 요청 응답 지연 없음
     * - REQUIRES_NEW: 별도 트랜잭션으로 감사 로그 저장 (원 트랜잭션과 독립)
     */
    @Async("auditExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAuditEvent(AuditEvent event) {
        try {
            AuditLog log = AuditLog.builder()
                    .eventType(event.getEventType().name())
                    .userId(event.getUserId())
                    .username(event.getUsername())
                    .ipAddress(event.getIpAddress())
                    .userAgent(truncate(event.getUserAgent(), 500))
                    .resource(event.getResource())
                    .detail(truncate(event.getDetail(), 2000))
                    .success(event.isSuccess())
                    .build();
            auditLogRepository.save(log);
        } catch (Exception e) {
            // 감사 로그 저장 실패는 원 서비스에 영향을 주지 않음
            AuditEventListener.log.error("Failed to save audit log for event: {}", event.getEventType(), e);
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }
}
