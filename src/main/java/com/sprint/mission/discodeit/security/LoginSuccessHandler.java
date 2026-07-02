package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import com.sprint.mission.discodeit.exception.auth.InvalidUserDetailsException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    response.setCharacterEncoding("UTF-8");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    if (authentication.getPrincipal() instanceof DiscodeitUserDetails userDetails) {
      response.setStatus(HttpServletResponse.SC_OK);
      UserResponse userResponse = userDetails.getUser();
      response.getWriter().write(objectMapper.writeValueAsString(userResponse));
      log.info("auth login success: userId={}, username={}", userResponse.id(),
          userResponse.username());
    } else {
      log.error("auth login failed: invalid principal type={}",
          authentication.getPrincipal().getClass().getSimpleName());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      ErrorResponse errorResponse = ErrorResponse.from(
          InvalidUserDetailsException.withInvalidPrincipal()
      );
      response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
  }
}
