package com.sprint.mission.discodeit.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.ErrorResponse;
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
@Component
@RequiredArgsConstructor
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    response.setCharacterEncoding("UTF-8");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    if (authentication.getPrincipal() instanceof DiscodeitUserDetails userDetails) {
      response.setStatus(HttpServletResponse.SC_OK);
      UserDto.Response userDto = userDetails.getUserDto();

      log.info("로그인 성공: username={}", userDetails.getUsername());
      response.getWriter().write(objectMapper.writeValueAsString(userDto));
    } else {
      log.warn("로그인 처리 중 Principal 타입 오류 발생: {}",
          authentication.getPrincipal().getClass().getSimpleName());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

      ErrorResponse errorResponse = new ErrorResponse(ErrorCode.INVALID_CREDENTIALS,
          new RuntimeException("Invalid userDetails"));
      response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
  }
}