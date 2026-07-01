package com.sprint.mission.discodeit.integration;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.dto.request.UserCreateRequest;
import com.sprint.mission.discodeit.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserService userService;

  @Test
  @DisplayName("로그인 API 통합 테스트 - 성공")
  void login_Success() throws Exception {
    // Given: 테스트 사용자 생성 (BCrypt 인코딩된 비밀번호로 저장됨)
    userService.create(
        new UserCreateRequest("loginuser", "login@example.com", "Password1!"),
        Optional.empty()
    );

    // When & Then: formLogin 방식으로 로그인 (username=&password= form params)
    mockMvc.perform(post("/api/auth/login")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", "loginuser")
            .param("password", "Password1!"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id", notNullValue()))
        .andExpect(jsonPath("$.username", is("loginuser")))
        .andExpect(jsonPath("$.email", is("login@example.com")));
  }

  @Test
  @DisplayName("로그인 API 통합 테스트 - 실패 (존재하지 않는 사용자)")
  void login_Failure_UserNotFound() throws Exception {
    // When & Then: Spring Security LoginFailureHandler → 401
    mockMvc.perform(post("/api/auth/login")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", "nonexistentuser")
            .param("password", "Password1!"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("로그인 API 통합 테스트 - 실패 (잘못된 비밀번호)")
  void login_Failure_InvalidCredentials() throws Exception {
    // Given
    userService.create(
        new UserCreateRequest("loginuser2", "login2@example.com", "Password1!"),
        Optional.empty()
    );

    // When & Then: 비밀번호 불일치 → Spring Security LoginFailureHandler → 401
    mockMvc.perform(post("/api/auth/login")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", "loginuser2")
            .param("password", "WrongPassword1!"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  @DisplayName("로그인 API 통합 테스트 - 실패 (빈 값)")
  void login_Failure_EmptyCredentials() throws Exception {
    // When & Then: 빈 값 → Spring Security 인증 실패 → 401
    mockMvc.perform(post("/api/auth/login")
            .with(csrf())
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .param("username", "")
            .param("password", ""))
        .andExpect(status().isUnauthorized());
  }
}
