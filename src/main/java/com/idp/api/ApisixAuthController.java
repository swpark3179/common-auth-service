package com.idp.api;

import com.idp.service.ApisixAuthService;
import com.idp.service.ApisixAuthService.AuthResult;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * APISIX ext-plugin-pre-req 전용 External Auth 엔드포인트.
 * 이 엔드포인트는 외부에서 직접 접근 금지 (APISIX에서만 호출).
 * 방화벽 또는 SecurityConfig에서 허용 IP를 제한하세요.
 */
@Hidden  // Swagger 문서에서 숨김
@Slf4j
@RestController
@RequestMapping("/apisix")
@RequiredArgsConstructor
public class ApisixAuthController {

    private final ApisixAuthService apisixAuthService;

    /**
     * APISIX가 각 요청에 대해 이 엔드포인트를 호출합니다.
     *
     * APISIX ext-plugin-pre-req 설정 예시:
     * conf:
     *   - name: "ext-plugin-pre-req"
     *     value:
     *       conf:
     *         - name: "auth"
     *           value: '{"url":"http://auth-service:8080/apisix/auth"}'
     */
    @PostMapping("/auth")
    public ResponseEntity<Void> verifyRequest(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestHeader(value = "X-Forwarded-Uri", required = false) String forwardedUri,
            @RequestHeader(value = "X-Forwarded-Method", required = false) String forwardedMethod) {

        log.debug("APISIX auth request: {} {}", forwardedMethod, forwardedUri);

        AuthResult result = apisixAuthService.verify(authorization, forwardedUri, forwardedMethod);

        if (result.allowed()) {
            HttpHeaders headers = new HttpHeaders();
            result.toHeaders().forEach(headers::set);
            return ResponseEntity.ok().headers(headers).build();
        } else if (result.forbidden()) {
            return ResponseEntity.status(403).build();
        } else {
            return ResponseEntity.status(401).build();
        }
    }
}
