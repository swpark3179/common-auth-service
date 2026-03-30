package com.idp.api;

import com.idp.dto.request.LoginRequest;
import com.idp.dto.request.TokenRefreshRequest;
import com.idp.dto.response.LoginResponse;
import com.idp.dto.response.TokenRefreshResponse;
import com.idp.dto.response.UserResponse;
import com.idp.security.UserDetailsImpl;
import com.idp.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인증", description = "로그인/로그아웃/토큰 관리")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "로그인", description = "아이디/비밀번호로 로그인하여 Access + Refresh 토큰을 발급합니다.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새 Access + Refresh 토큰 쌍을 발급합니다 (Rotation).")
    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refresh(@Valid @RequestBody TokenRefreshRequest request) {
        return ResponseEntity.ok(authService.refresh(request));
    }

    @Operation(summary = "로그아웃", description = "현재 Access Token을 블랙리스트에 등록하고 Refresh Token을 폐기합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @AuthenticationPrincipal UserDetailsImpl currentUser) {
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            authService.logout(authHeader.substring(7), currentUser);
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내 정보 조회", description = "현재 인증된 사용자의 정보를 조회합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal UserDetailsImpl currentUser) {
        return ResponseEntity.ok(UserResponse.from(currentUser.getUser()));
    }
}
