package com.idp.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "E001", "잘못된 입력값입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "E002", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "E003", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "E004", "요청한 리소스를 찾을 수 없습니다."),

    // 인증
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A001", "아이디 또는 비밀번호가 올바르지 않습니다."),
    ACCOUNT_DISABLED(HttpStatus.UNAUTHORIZED, "A002", "비활성화된 계정입니다."),
    ACCOUNT_LOCKED(HttpStatus.UNAUTHORIZED, "A003", "잠긴 계정입니다."),

    // 토큰
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "T001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "T002", "만료된 토큰입니다."),
    BLACKLISTED_TOKEN(HttpStatus.UNAUTHORIZED, "T003", "폐기된 토큰입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "T004", "유효하지 않은 리프레시 토큰입니다."),

    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "U002", "이미 사용 중인 아이디입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U003", "이미 사용 중인 이메일입니다."),

    // 역할/권한
    ROLE_NOT_FOUND(HttpStatus.NOT_FOUND, "R001", "역할을 찾을 수 없습니다."),
    DUPLICATE_ROLE(HttpStatus.CONFLICT, "R002", "이미 존재하는 역할입니다."),
    PERMISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "권한을 찾을 수 없습니다."),
    DUPLICATE_PERMISSION(HttpStatus.CONFLICT, "P002", "이미 존재하는 권한입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus httpStatus, String code, String message) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }
}
