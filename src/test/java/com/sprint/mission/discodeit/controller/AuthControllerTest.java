package com.sprint.mission.discodeit.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sprint.mission.discodeit.dto.data.JwtDto;
import com.sprint.mission.discodeit.dto.data.JwtRefreshResult;
import com.sprint.mission.discodeit.dto.data.UserDto;
import com.sprint.mission.discodeit.security.JwtAuthenticationFilter;
import com.sprint.mission.discodeit.security.JwtTokenProvider;
import com.sprint.mission.discodeit.service.JwtAuthService;
import com.sprint.mission.discodeit.service.UserService;
import java.util.UUID;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private JwtAuthService jwtAuthService;

  @MockitoBean
  private JwtTokenProvider jwtTokenProvider;

  @MockitoBean
  private UserService userService;

  @MockitoBean
  private JwtAuthenticationFilter jwtAuthenticationFilter;

  @Test
  @DisplayName("Access Token 재발급 성공")
  void refresh_Success() throws Exception {
    UUID userId = UUID.randomUUID();
    UserDto userDto = new UserDto(userId, "testuser", "test@example.com", null, true);
    JwtDto jwtDto = new JwtDto(userDto, "new-access-token");
    JwtRefreshResult result = new JwtRefreshResult(jwtDto, "new-refresh-token");

    given(jwtAuthService.refresh("old-refresh-token")).willReturn(result);
    given(jwtTokenProvider.createRefreshTokenCookie("new-refresh-token"))
        .willReturn(new Cookie(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, "new-refresh-token"));

    mockMvc.perform(post("/api/auth/refresh")
            .cookie(new Cookie(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, "old-refresh-token")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").value("new-access-token"))
        .andExpect(jsonPath("$.userDto.username").value("testuser"));
  }
}