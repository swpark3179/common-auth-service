package com.idp.audit;

/**
 * 감사 로그 이벤트 유형 정의
 */
public enum AuditEventType {

    // 인증
    LOGIN_SUCCESS,
    LOGIN_FAILURE,
    LOGOUT,

    // 토큰
    TOKEN_REFRESH_SUCCESS,
    TOKEN_REFRESH_FAILURE,
    TOKEN_BLACKLISTED,

    // APISIX 인가
    APISIX_AUTH_ALLOWED,
    APISIX_AUTH_DENIED,

    // 사용자 관리
    USER_CREATED,
    USER_UPDATED,
    USER_DELETED,
    USER_ENABLED,
    USER_DISABLED,
    USER_LOCKED,
    USER_UNLOCKED,

    // 역할/권한 관리
    ROLE_CREATED,
    ROLE_UPDATED,
    ROLE_DELETED,
    PERMISSION_CREATED,
    PERMISSION_UPDATED,
    PERMISSION_DELETED,

    // 역할 할당
    ROLE_ASSIGNED,
    ROLE_REVOKED
}
