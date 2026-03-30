package com.idp.service;

import com.idp.audit.AuditEventPublisher;
import com.idp.audit.AuditEventType;
import com.idp.domain.User;
import com.idp.dto.request.LoginRequest;
import com.idp.dto.request.TokenRefreshRequest;
import com.idp.dto.response.LoginResponse;
import com.idp.dto.response.TokenRefreshResponse;
import com.idp.dto.response.UserResponse;
import com.idp.exception.BusinessException;
import com.idp.exception.ErrorCode;
import com.idp.repository.UserRepository;
import com.idp.security.JwtTokenProvider;
import com.idp.security.UserDetailsImpl;
import com.idp.service.TokenService.TokenPair;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final AuditEventPublisher auditEventPublisher;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            UserDetailsImpl userDetails = (UserDetailsImpl) auth.getPrincipal();
            User user = userDetails.getUser();

            TokenPair pair = tokenService.issueTokenPair(user);
            auditEventPublisher.publish(AuditEventType.LOGIN_SUCCESS, user.getId(), user.getUsername(), true, null);

            return LoginResponse.of(pair.accessToken(), pair.refreshToken(), pair.expiresIn(),
                    UserResponse.from(user));

        } catch (BadCredentialsException e) {
            auditEventPublisher.publish(AuditEventType.LOGIN_FAILURE, null, request.getUsername(), false, "잘못된 자격증명");
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        } catch (DisabledException e) {
            auditEventPublisher.publish(AuditEventType.LOGIN_FAILURE, null, request.getUsername(), false, "비활성화된 계정");
            throw new BusinessException(ErrorCode.ACCOUNT_DISABLED);
        } catch (LockedException e) {
            auditEventPublisher.publish(AuditEventType.LOGIN_FAILURE, null, request.getUsername(), false, "잠긴 계정");
            throw new BusinessException(ErrorCode.ACCOUNT_LOCKED);
        }
    }

    @Transactional
    public TokenRefreshResponse refresh(TokenRefreshRequest request) {
        try {
            // Refresh Token에서 username 추출 (만료 검증 포함)
            Claims claims = jwtTokenProvider.parseToken(request.getRefreshToken());
            String username = claims.getSubject();

            User user = userRepository.findByUsernameWithRolesAndPermissions(username)
                    .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

            TokenPair pair = tokenService.rotate(request.getRefreshToken(), user);
            auditEventPublisher.publish(AuditEventType.TOKEN_REFRESH_SUCCESS, user.getId(), username, true, null);

            return TokenRefreshResponse.of(pair.accessToken(), pair.refreshToken(), pair.expiresIn());

        } catch (BusinessException e) {
            auditEventPublisher.publish(AuditEventType.TOKEN_REFRESH_FAILURE, null, null, false, e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void logout(String rawAccessToken, UserDetailsImpl currentUser) {
        tokenService.blacklistAccessToken(rawAccessToken);
        if (currentUser != null) {
            tokenService.revokeAllRefreshTokens(currentUser.getUserId());
            auditEventPublisher.publish(AuditEventType.LOGOUT, currentUser.getUserId(),
                    currentUser.getUsername(), true, null);
        }
    }
}
