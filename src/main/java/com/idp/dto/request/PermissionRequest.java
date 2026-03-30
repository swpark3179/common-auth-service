package com.idp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class PermissionRequest {

    @NotBlank(message = "리소스 패턴을 입력해주세요. 예: /api/users/**")
    private String resourcePattern;

    @NotBlank(message = "HTTP 메서드를 입력해주세요. 예: GET, POST, *")
    private String action;

    private String description;
}
