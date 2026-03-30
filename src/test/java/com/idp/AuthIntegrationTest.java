package com.idp;

import com.idp.domain.Role;
import com.idp.domain.User;
import com.idp.dto.request.LoginRequest;
import com.idp.repository.RoleRepository;
import com.idp.repository.UserRepository;
import com.idp.security.JwtTokenProvider;
import com.idp.service.ApisixAuthService;
import com.idp.service.ApisixAuthService.AuthResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired RoleRepository roleRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtTokenProvider jwtTokenProvider;
    @Autowired ApisixAuthService apisixAuthService;

    static final String TEST_USERNAME = "testuser";
    static final String TEST_PASSWORD = "password123";

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_USER").build()));

        userRepository.save(User.builder()
                .username(TEST_USERNAME)
                .passwordHash(passwordEncoder.encode(TEST_PASSWORD))
                .email("test@example.com")
                .roles(Set.of(userRole))
                .build());
    }

    @Test
    @DisplayName("로그인 성공 시 Access/Refresh Token 반환")
    void login_success() throws Exception {
        LoginRequest req = new LoginRequest();
        // Jackson이 없으므로 직접 JSON
        String body = """
                {"username": "%s", "password": "%s"}
                """.formatted(TEST_USERNAME, TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        assertThat(response).contains("accessToken");
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인 시 401 반환")
    void login_failure_wrong_password() throws Exception {
        String body = """
                {"username": "%s", "password": "wrongpassword"}
                """.formatted(TEST_USERNAME);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("유효한 JWT로 /api/auth/me 조회 성공")
    void me_with_valid_token() throws Exception {
        // 먼저 로그인
        String loginBody = """
                {"username": "%s", "password": "%s"}
                """.formatted(TEST_USERNAME, TEST_PASSWORD);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = objectMapper.readTree(
                loginResult.getResponse().getContentAsString())
                .get("accessToken").asText();

        mockMvc.perform(get("/api/auth/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(TEST_USERNAME));
    }

    @Test
    @DisplayName("토큰 없이 /api/auth/me 요청 시 401 반환")
    void me_without_token() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("APISIX 인증 검증 - 유효한 토큰은 200 반환")
    void apisix_auth_valid_token() throws Exception {
        // 로그인 후 토큰 획득
        String loginBody = """
                {"username": "%s", "password": "%s"}
                """.formatted(TEST_USERNAME, TEST_PASSWORD);

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andReturn();

        String accessToken = objectMapper.readTree(
                loginResult.getResponse().getContentAsString())
                .get("accessToken").asText();

        // APISIX 검증 (권한 체크 없이)
        AuthResult result = apisixAuthService.verify("Bearer " + accessToken, null, null);
        assertThat(result.allowed()).isTrue();
        assertThat(result.username()).isEqualTo(TEST_USERNAME);
    }

    @Test
    @DisplayName("APISIX 인증 검증 - 토큰 없으면 401")
    void apisix_auth_no_token() {
        AuthResult result = apisixAuthService.verify(null, "/api/data", "GET");
        assertThat(result.allowed()).isFalse();
        assertThat(result.forbidden()).isFalse();
    }
}
