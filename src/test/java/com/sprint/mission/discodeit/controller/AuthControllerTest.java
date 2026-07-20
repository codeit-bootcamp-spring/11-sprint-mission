package com.sprint.mission.discodeit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.JwtRefreshResult;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.service.JwtService;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.servlet.http.Cookie;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.DefaultCsrfToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private JwtService jwtService;

  @MockitoBean
  private JpaMetamodelMappingContext jpaMappingContext;

  @Test
  @DisplayName("CSRF 토큰 요청 성공 - 200 응답")
  void getCsrfToken_success() throws Exception {
    DefaultCsrfToken csrfToken = new DefaultCsrfToken("X-CSRF-TOKEN", "_csrf", "test-token-value");

    mockMvc.perform(get("/api/auth/csrf-token")
            .requestAttr(CsrfToken.class.getName(), csrfToken))
        .andExpect(status().isOk());
  }

  @Test
  @DisplayName("권한 변경 요청 성공 - 200 응답")
  void updateRole_success() throws Exception {
    UUID userId = UUID.randomUUID();
    UserRoleUpdateRequest request = new UserRoleUpdateRequest(userId, Role.ADMIN);
    UserDto response = new UserDto(userId, "tester", "test@test.com", null, false, Role.ADMIN);

    given(userService.updateRole(any(UserRoleUpdateRequest.class))).willReturn(response);

    mockMvc.perform(put("/api/auth/role")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.role").value("ADMIN"));
  }

  @Test
  @DisplayName("토큰 갱신 요청 성공 - 200 응답 및 쿠키 확인")
  void refresh_success() throws Exception {
    String refreshToken = "valid-refresh-token";
    String newAccessToken = "new-access-token";
    String newRefreshToken = "new-refresh-token";

    UserDto userDto = new UserDto(UUID.randomUUID(), "tester", "test@test.com", null, false,
        Role.USER);
    JwtRefreshResult refreshResult = new JwtRefreshResult(newAccessToken, newRefreshToken, userDto);

    given(jwtService.refreshJwtSession(anyString())).willReturn(refreshResult);

    mockMvc.perform(post("/api/auth/refresh")
            .cookie(new Cookie(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, refreshToken)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value(newAccessToken))
        .andExpect(cookie().exists(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME))
        .andExpect(cookie().value(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, newRefreshToken))
        .andExpect(cookie().httpOnly(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, true));
  }
}