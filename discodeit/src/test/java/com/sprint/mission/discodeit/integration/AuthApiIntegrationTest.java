package com.sprint.mission.discodeit.integration;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.auth.LoginRequest;
import com.sprint.mission.discodeit.dto.user.UserCreateRequest;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.auth.InvalidCredentialsException;
import com.sprint.mission.discodeit.exception.common.ValidationErrorException;
import com.sprint.mission.discodeit.exception.user.UserNotFoundException;
import com.sprint.mission.discodeit.service.UserService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest
class AuthApiIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private UserService userService;

  private String username;
  private String email;
  private String password;

  @BeforeEach
  void setUp() {
    username = "tester";
    email = "tester@example.io";
    password = "qwerty";

    userService.createUser(
        new UserCreateRequest(username, email, password),
        Optional.empty()
    );
  }

  @Nested
  @DisplayName("login")
  class Login {

    @Test
    @DisplayName("success")
    void login_success() throws Exception {
      // given
      LoginRequest request = new LoginRequest(
          username,
          password
      );

      // when & then
      mockMvc.perform(post("/api/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.id").value(notNullValue()))
          .andExpect(jsonPath("$.username").value(username))
          .andExpect(jsonPath("$.email").value(email))
          .andExpect(jsonPath("$.online").value(true));
    }

    @Test
    @DisplayName("fail with user not found")
    void login_fail_user_not_found_throws_exception() throws Exception {
      // given
      String nonExistentUserName = "nonExistentUserName";
      LoginRequest request = new LoginRequest(
          nonExistentUserName,
          password
      );

      // when & then
      ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;

      mockMvc.perform(post("/api/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
          .andExpect(jsonPath("$.details.username").value(nonExistentUserName))
          .andExpect(
              jsonPath("$.exceptionType").value(UserNotFoundException.class.getSimpleName()));
    }

    @Test
    @DisplayName("fail with invalid credentials")
    void login_fail_invalid_credentials_throws_exception() throws Exception {
      // given
      String wrongPassword = "wrongPassword";
      LoginRequest request = new LoginRequest(
          username,
          wrongPassword
      );

      // when & then
      ErrorCode errorCode = ErrorCode.INVALID_CREDENTIALS;

      mockMvc.perform(post("/api/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
          .andExpect(
              jsonPath("$.exceptionType").value(InvalidCredentialsException.class.getSimpleName()));
    }

    @Test
    @DisplayName("fail with validation error")
    void login_fail_validation_error_throws_exception() throws Exception {
      // given
      LoginRequest request = new LoginRequest(
          null,
          null
      );

      // when & then
      ErrorCode errorCode = ErrorCode.VALIDATION_ERROR;

      mockMvc.perform(post("/api/auth/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(objectMapper.writeValueAsString(request)))
          .andExpect(status().is(errorCode.getHttpStatus().value()))
          .andExpect(jsonPath("$.code").value(errorCode.getCode()))
          .andExpect(jsonPath("$.message").value(errorCode.getMessage()))
          .andExpect(jsonPath("$.details.username").exists())
          .andExpect(jsonPath("$.details.password").exists())
          .andExpect(
              jsonPath("$.exceptionType").value(ValidationErrorException.class.getSimpleName()));
    }
  }
}