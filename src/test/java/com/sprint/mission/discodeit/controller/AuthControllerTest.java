package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.config.SecurityTestConfig;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.dto.request.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.AuthService;
import com.sprint.mission.discodeit.service.UserService;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(SecurityTestConfig.class)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private AuthService authService;

  @Test
  @DisplayName("CSRF 토큰 요청 성공 - 204")
  void getCsrfToken_Success() throws Exception {
    mockMvc.perform(get("/api/auth/csrf-token"))
        .andExpect(status().isNoContent());
  }

  @Test
  @DisplayName("현재 사용자 조회 성공 - 200")
  void me_Success() throws Exception {
    UUID userId = UUID.randomUUID();
    UserDto userDto = new UserDto(userId, "testuser", "test@example.com", null, true, Role.USER);
    DiscodeitUserDetails userDetails = new DiscodeitUserDetails(userDto, "encodedPw");

    given(userService.find(userId)).willReturn(userDto);

    mockMvc.perform(get("/api/auth/me")
            .with(user(userDetails)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(userId.toString()))
        .andExpect(jsonPath("$.username").value("testuser"))
        .andExpect(jsonPath("$.email").value("test@example.com"))
        .andExpect(jsonPath("$.role").value("USER"));
  }

  @Test
  @DisplayName("미인증 상태에서 현재 사용자 조회 실패 - 403")
  void me_Unauthenticated() throws Exception {
    mockMvc.perform(get("/api/auth/me"))
        .andExpect(status().isForbidden());
  }

  @Test
  @DisplayName("권한 수정 성공 - 200")
  void updateRole_Success() throws Exception {
    UUID targetUserId = UUID.randomUUID();
    UserRoleUpdateRequest request = new UserRoleUpdateRequest(targetUserId, Role.CHANNEL_MANAGER);
    UserDto updatedUser = new UserDto(targetUserId, "user", "user@example.com", null, true,
        Role.CHANNEL_MANAGER);

    given(authService.updateRole(any(UserRoleUpdateRequest.class))).willReturn(updatedUser);

    mockMvc.perform(put("/api/auth/role")
            .with(user("admin").roles("ADMIN"))
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(targetUserId.toString()))
        .andExpect(jsonPath("$.role").value("CHANNEL_MANAGER"));
  }

  @Test
  @DisplayName("미인증 상태에서 권한 수정 실패 - 403")
  void updateRole_Unauthenticated() throws Exception {
    UUID targetUserId = UUID.randomUUID();
    UserRoleUpdateRequest request = new UserRoleUpdateRequest(targetUserId, Role.CHANNEL_MANAGER);

    mockMvc.perform(put("/api/auth/role")
            .with(csrf())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isForbidden());
  }
}
