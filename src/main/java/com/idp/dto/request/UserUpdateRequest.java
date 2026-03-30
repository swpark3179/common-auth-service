package com.idp.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.Set;

@Getter
public class UserUpdateRequest {

    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;

    @Email(message = "올바른 이메일 형식을 입력해주세요.")
    private String email;

    private Set<Long> roleIds;
}
