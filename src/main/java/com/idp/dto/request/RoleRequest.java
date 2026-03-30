package com.idp.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class RoleRequest {

    @NotBlank(message = "역할 이름을 입력해주세요.")
    private String name;

    private String description;
}
